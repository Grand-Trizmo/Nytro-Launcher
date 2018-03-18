package com.gildedgames.launcher.ui.views.account;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.components.FlatPasswordField;
import com.gildedgames.launcher.ui.components.FlatTextField;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherStyles;
import com.gildedgames.launcher.ui.views.ProgressView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.Callback;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

public class AccountAddView extends JPanel {
	protected final LauncherFrame frame;

	protected final Launcher launcher;

	protected FlatButton loginButton, cancelButton;

	protected JLabel label1, label2;

	protected JTextField usernameField;

	protected JPasswordField passwordField;

	protected Callback<Account> callback;

	private boolean cancellable = false;

	public AccountAddView(LauncherFrame frame, Launcher launcher) {
		this.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);

		this.frame = frame;
		this.launcher = launcher;

		this.setLayout(new BorderLayout());
		this.add(Box.createVerticalStrut(170), BorderLayout.NORTH);
		this.add(Box.createHorizontalStrut(250), BorderLayout.EAST);
		this.add(Box.createHorizontalStrut(250), BorderLayout.WEST);
		this.add(Box.createVerticalStrut(200), BorderLayout.SOUTH);

		this.init();
	}

	public void setLoginCallback(Callback<Account> callback) {
		this.callback = callback;
	}

	private void init() {
		JPanel form = new JPanel();
		form.setLayout(new GridBagLayout());
		form.setPreferredSize(new Dimension(440, 200));
		form.setOpaque(false);

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.fill = GridBagConstraints.HORIZONTAL;

		this.label1 = new JLabel(this.getTitle());
		this.label1.setOpaque(false);
		this.label1.setForeground(Color.WHITE);
		this.label1.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(20.0f));

		form.add(this.label1, c);

		c.gridy = 1;

		this.label2 = new JLabel(this.getHeader());
		this.label2.setOpaque(false);
		this.label2.setForeground(new Color(200, 200, 200));
		this.label2.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.label2.setPreferredSize(new Dimension(420, 30));

		form.add(this.label2, c);

		c.gridy = 2;

		form.add(Box.createVerticalStrut(10), c);

		c.gridy = 3;
		c.gridwidth = 2;

		InputChangeHandler usernameInputHandler = new InputChangeHandler();

		this.usernameField = new FlatTextField("Username");
		this.usernameField.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f));
		this.usernameField.setPreferredSize(new Dimension(300, 30));
		this.usernameField.setOpaque(true);
		this.usernameField.addKeyListener(usernameInputHandler);
		this.usernameField.addActionListener(usernameInputHandler);

		form.add(this.usernameField, c);

		InputChangeHandler passwordInputHandler = new InputChangeHandler();

		this.passwordField = new FlatPasswordField("Password");
		this.passwordField.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f));
		this.passwordField.setPreferredSize(new Dimension(300, 30));
		this.passwordField.setOpaque(true);
		this.passwordField.addKeyListener(passwordInputHandler);
		this.passwordField.addActionListener(passwordInputHandler);

		c.gridy = 4;
		form.add(Box.createVerticalStrut(10), c);

		c.gridy = 5;
		c.gridwidth = 2;
		c.gridheight = 1;

		form.add(this.passwordField, c);

		c.gridy = 6;
		form.add(Box.createVerticalStrut(10), c);

		c.gridy = 7;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1.0D;
		c.weighty = 1.0D;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;

		this.cancelButton = new FlatButton("Cancel", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.cancelButton.setStyle(FlatButton.ButtonStyle.LIGHT);
		this.cancelButton.setPreferredSize(new Dimension(100, 35));
		this.cancelButton.addActionListener(e -> AccountAddView.this.onCancelled());

		form.add(this.cancelButton, c);

		if (!this.cancellable) {
			this.cancelButton.setEnabled(false);
			this.cancelButton.setVisible(false);
		}

		this.loginButton = new FlatButton("Login", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.loginButton.setStyle(FlatButton.ButtonStyle.DISABLED);
		this.loginButton.setPreferredSize(new Dimension(150, 35));

		c.gridx = 1;
		c.gridy = 7;
		c.anchor = GridBagConstraints.EAST;

		form.add(this.loginButton, c);

		this.loginButton.addActionListener(e -> AccountAddView.this.tryLogin());

		this.add(form, BorderLayout.CENTER);
	}

	protected String getTitle() {
		return "Add your Mojang account";
	}

	protected String getHeader() {
		return "The KKMC Launcher is a mod for Minecraft, which requires a Mojang account to play.";
	}

	public void setCancelable(boolean cancel) {
		this.cancellable = cancel;

		this.cancelButton.setEnabled(cancel);
		this.cancelButton.setVisible(cancel);
	}

	private void onCancelled() {
		this.frame.getLauncherLayout().back();
	}

	private void tryLogin() {
		if (AccountAddView.this.loginButton.getStyle() == FlatButton.ButtonStyle.DISABLED) {
			return;
		}

		final String username = AccountAddView.this.usernameField.getText();
		String password = new String(AccountAddView.this.passwordField.getPassword());

		AccountAddView.LoginCallable callable = new AccountAddView.LoginCallable(username, password);
		ObservableFuture<Session> future = new ObservableFuture<>(AccountAddView.this.launcher.getExecutor().submit(callable), callable);

		Futures.addCallback(future, this.getSessionCallback(), SwingExecutor.INSTANCE);

		ProgressView progressView = new ProgressView(this.frame, future, future, "Logging you in", true);

		this.frame.getLauncherLayout().show(progressView);

		SwingHelper.addErrorDialogCallback(AccountAddView.this.frame, future);
	}

	protected FutureCallback<Session> getSessionCallback() {
		return new FutureCallback<Session>() {
			@Override
			public void onSuccess(Session result) {
				Account account = new Account();
				account.setLastUsed(new Date());
				account.setSession((StoredSession) result);
				account.setMojangId(AccountAddView.this.usernameField.getText());

				AccountAddView.this.returnResult(account);
			}

			@Override
			public void onFailure(Throwable t) {

			}
		};
	}

	protected void returnResult(Account result) {
		if (this.callback != null) {
			this.callback.handle(result);
		}

		this.frame.getLauncherLayout().back();
	}

	private class InputChangeHandler implements KeyListener, ActionListener {
		@Override
		public void keyTyped(KeyEvent e) {

		}

		@Override
		public void keyPressed(KeyEvent e) {
			EventQueue.invokeLater(() -> {
				if (AccountAddView.this.usernameField.getText().length() > 3 &&
						AccountAddView.this.passwordField.getPassword().length > 3) {
					AccountAddView.this.loginButton.setStyle(FlatButton.ButtonStyle.HIGHLIGHTED);
				} else {
					AccountAddView.this.loginButton.setStyle(FlatButton.ButtonStyle.DISABLED);
				}

				AccountAddView.this.loginButton.revalidate();
				AccountAddView.this.loginButton.repaint();
			});
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			AccountAddView.this.tryLogin();
		}
	}

	private class LoginCallable implements Callable<Session>, ProgressObservable {
		private final String id;
		private final String password;

		private LoginCallable(String id, String password) {
			this.id = id;
			this.password = password;
		}

		@Override
		public Session call() throws AuthenticationException, IOException, InterruptedException {
			LoginService service = AccountAddView.this.launcher.getLoginService();
			List<? extends YggdrasilLoginService.Profile> identities = service.login(AccountAddView.this.launcher.getProperties().getProperty("agentName"), this.id, this.password);

			// The list of identities (profiles in Mojang terms) corresponds to whether the account
			// owns the game, so we need to check that
			if (identities.size() > 0) {
				// Set offline enabled flag to true
				Configuration config = AccountAddView.this.launcher.getConfig();

				if (!config.isOfflineEnabled()) {
					config.setOfflineEnabled(true);

					Persistence.commitAndForget(config);
				}

				return identities.get(0).createSession();
			} else {
				throw new AuthenticationException("Minecraft not owned", SharedLocale.tr("login.minecraftNotOwnedError"));
			}
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
