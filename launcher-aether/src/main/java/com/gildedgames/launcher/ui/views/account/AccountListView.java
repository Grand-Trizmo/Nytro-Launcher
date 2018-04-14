package com.gildedgames.launcher.ui.views.account;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;
import com.gildedgames.launcher.ui.resources.LauncherStyles;
import com.gildedgames.launcher.ui.views.ProgressView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.Callback;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.DefaultTable;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;

public class AccountListView extends JPanel {
	private final Launcher launcher;

	private final LauncherFrame frame;

	private JTable table;

	private Callback<Account> callback;

	public AccountListView(Launcher launcher, LauncherFrame frame) {
		this.launcher = launcher;
		this.frame = frame;

		this.init();

		this.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);
	}

	public void setCallback(Callback<Account> callback) {
		this.callback = callback;
	}

	private void init() {
		this.setLayout(new MigLayout("fill, insets 120", "[fill]", "[]8[]12[]2[]12[]"));

		JLabel label = new JLabel("Switch accounts");
		label.setForeground(Color.WHITE);
		label.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(20.0f));
		label.setPreferredSize(new Dimension(200, 40));
		this.add(label, "wrap");

		JLabel label2 = new JLabel("This will change how you appear in-game.");
		label2.setForeground(new Color(180, 180, 180));
		label2.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f));
		label2.setPreferredSize(new Dimension(200, 40));
		this.add(label2, "wrap");

		TableModel model = new AccountListModel(this.launcher.getAccounts(), this.frame.getAvatarManager());

		this.table = new DefaultTable();
		this.table.setModel(model);
		this.table.setRowHeight(48);
		this.table.getColumnModel().getColumn(0).setMaxWidth(54);
		this.table.setTableHeader(null);
		this.table.setBackground(new Color(0x424242));
		this.table.setBorder(BorderFactory.createEmptyBorder());
		this.table.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.table.setForeground(Color.WHITE);
		this.table.setSelectionBackground(new Color(0x0a7cd2));
		this.table.setRowSelectionInterval(0, 0);

		JScrollPane scroller = new JScrollPane(this.table);
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setPreferredSize(new Dimension(500, 320));
		scroller.setBackground(new Color(0x283038));
		scroller.setBorder(BorderFactory.createEmptyBorder());

		JPanel accountActions = new JPanel();
		accountActions.setOpaque(false);
		accountActions.setLayout(new MigLayout("insets 0", "push[]12[]"));

		SwingHelper.removeFocusBorder(this.table, scroller);

		FlatButton removeAccount = new FlatButton("Remove account", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		removeAccount.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		removeAccount.addActionListener(e -> {
			AccountList list = AccountListView.this.launcher.getAccounts();

			Account account = AccountListView.this.getSelectedAccount();

			if (list.getSize() <= 1) {
				SwingHelper.showMessageDialog(AccountListView.this.frame, "You cannot remove the only account. Please add another account, then try removing this account again.",
						"Cannot delete account", null, JOptionPane.WARNING_MESSAGE);

				return;
			}

			boolean result = SwingHelper.confirmDialog(AccountListView.this.frame, "Are you sure you want to delete the account " + account.getMojangId() + "?\n\n" +
					"You will need to sign-in again if you choose to use this account in the future.", "Confirm account deletion");

			if (result) {
				list.remove(account);

				Persistence.commitAndForget(list);
			}
		});
		removeAccount.setButtonIcon(LauncherIcons.REMOVE);

		accountActions.add(removeAccount);

		FlatButton addAccount = new FlatButton("Add account", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		addAccount.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		addAccount.addActionListener(e -> this.addAccount());
		addAccount.setButtonIcon(LauncherIcons.ADD);

		accountActions.add(addAccount);

		JPanel uiActions = new JPanel();
		uiActions.setLayout(new MigLayout("fill, insets 0", "[]push[]"));
		uiActions.setOpaque(false);

		FlatButton cancel = new FlatButton("Cancel", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		cancel.addActionListener(e -> this.returnToParent(null));

		uiActions.add(cancel);

		FlatButton selectAccount = new FlatButton("Switch", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		selectAccount.setStyle(FlatButton.ButtonStyle.HIGHLIGHTED);
		selectAccount.addActionListener(e -> {
			Account account = this.getSelectedAccount();

			if (account.getSession() == null) {
				this.tryRelog(account);
			} else {
				this.tryRefresh(account);
			}
		});

		uiActions.add(selectAccount);

		this.add(scroller, "wrap");
		this.add(accountActions, "wrap");
		this.add(uiActions);
	}

	private Account getSelectedAccount() {
		return this.launcher.getAccounts().getElementAt(this.table.getSelectedRow());
	}

	private void tryRefresh(final Account account) {
		RefreshCallable callable = new RefreshCallable(account);

		ObservableFuture<StoredSession> future = new ObservableFuture<>(AccountListView.this.launcher.getExecutor().submit(callable), callable);

		Futures.addCallback(future, new FutureCallback<StoredSession>() {
			@Override
			public void onSuccess(StoredSession result) {
				account.setLastUsed(new Date());
				account.setSession(result);

				AccountListView.this.launcher.getAccounts().setSelectedAccount(account);

				Persistence.commitAndForget(AccountListView.this.launcher.getAccounts());

				AccountListView.this.returnToParent(account);
			}

			@Override
			public void onFailure(Throwable t) {
				AccountListView.this.tryRelog(account);
			}
		}, SwingExecutor.INSTANCE);

		ProgressView progressView = new ProgressView(this.frame, future, future, "Refreshing token", true);

		this.frame.getLauncherLayout().show(progressView);
	}

	private void tryRelog(Account account) {
		AccountRefreshView view = new AccountRefreshView(this.frame, this.launcher, account);
		view.setCancelable(true);
		view.setLoginCallback(value -> {
			AccountListView.this.launcher.getAccounts().setSelectedAccount(value);

			Persistence.commitAndForget(AccountListView.this.launcher.getAccounts());

			AccountListView.this.returnToParent(value);
		});

		this.frame.getLauncherLayout().show(view);
	}

	private void addAccount() {
		AccountAddView view = new AccountAddView(this.frame, this.launcher);
		view.setCancelable(true);
		view.setLoginCallback(result -> {
			if (result == null) {
				return;
			}

			for (Account account : this.launcher.getAccounts().getAllAccounts()) {
				if (account.getSession() != null && account.getMojangId().equals(result.getMojangId())) {
					SwingHelper.showMessageDialog(this.frame, "The account " + result.getMojangId() + " is already added.", "Couldn't add account", null, JOptionPane.WARNING_MESSAGE);

					return;
				}
			}

			this.launcher.getAccounts().add(result);
			this.launcher.getAccounts().setSelectedAccount(result);

			Persistence.commitAndForget(this.launcher.getAccounts());
		});

		this.frame.getLauncherLayout().show(view);
	}

	private void returnToParent(Account result) {
		this.frame.getLauncherLayout().back();

		if (this.callback != null) {
			this.callback.handle(result);
		}
	}

	private class RefreshCallable implements Callable<StoredSession>, ProgressObservable {
		private final Account account;

		private RefreshCallable(Account account) {
			this.account = account;
		}

		@Override
		public StoredSession call() throws AuthenticationException, IOException, InterruptedException {
			LoginService service = AccountListView.this.launcher.getLoginService();

			return service.refreshSession(this.account);
		}

		@Override
		public double getProgress() {
			return -1;
		}

		@Override
		public String getStatus() {
			return SharedLocale.tr("login.loggingInStatus");
		}
	}
}
