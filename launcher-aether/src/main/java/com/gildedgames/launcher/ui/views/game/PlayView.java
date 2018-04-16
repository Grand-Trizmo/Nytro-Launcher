package com.gildedgames.launcher.ui.views.game;

import com.gildedgames.launcher.launch.LaunchProcessHandler;
import com.gildedgames.launcher.launch.LaunchSupervisor;
import com.gildedgames.launcher.ui.IListeningView;
import com.gildedgames.launcher.ui.LauncherFrame;
import com.gildedgames.launcher.ui.animations.Animation;
import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.components.UserIndicator;
import com.gildedgames.launcher.ui.panels.BannerPanel;
import com.gildedgames.launcher.ui.panels.ImagePanel;
import com.gildedgames.launcher.ui.panels.NewsFeedPanel;
import com.gildedgames.launcher.ui.panels.ProgressIndicatorPanel;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;
import com.gildedgames.launcher.ui.resources.NewsFeedManager;
import com.gildedgames.launcher.ui.styles.FlatScrollbarUI;
import com.gildedgames.launcher.ui.views.account.AccountListView;
import com.gildedgames.launcher.ui.views.account.AccountRefreshView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.InstanceList;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.*;
import com.skcraft.launcher.launch.LaunchListener;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.update.Updater;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.Getter;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import static com.skcraft.launcher.util.SharedLocale.tr;

public class PlayView extends JPanel implements IListeningView {
	@Getter
	private final InstanceTable instancesTable = new InstanceTable();

	private final InstanceTableModel instancesModel;

	@Getter
	private final JScrollPane instanceScroll = new JScrollPane(this.instancesTable);

	private FlatButton launchButton;

	private FlatButton switchUserButton;

	private UserIndicator userIndicator;

	private ProgressIndicatorPanel progressIndicatorPanel;

	private BannerPanel bannerPanel;

	private NewsFeedPanel news;

	@Getter
	private ConsolePanel consolePanel;

	private Launcher launcher;

	private LauncherFrame frame;

	private boolean isUpdating = false;

	private LaunchSupervisor launchSupervisor;

	public PlayView(Launcher launcher, LauncherFrame frame) {
		this.launchSupervisor = frame.getLaunchSupervisor();

		this.userIndicator = new UserIndicator(frame.getAvatarManager());
		this.instancesModel = new InstanceTableModel(launcher.getInstances());
		this.launcher = launcher;
		this.frame = frame;

		this.setLayout(new BorderLayout());

		this.init();

		SwingUtilities.invokeLater(() -> PlayView.this.refresh(false));
	}

	private void init() {
		this.launchButton = new FlatButton(SharedLocale.tr("launcher.launch"), LauncherFonts.OPEN_SANS_REGULAR.deriveFont(20.0f));
		this.launchButton.setStyle(FlatButton.ButtonStyle.HIGHLIGHTED);
		this.launchButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel right = new JPanel(new BorderLayout());
		right.setOpaque(false);

		this.switchUserButton = new FlatButton("Switch user", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.switchUserButton.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		this.switchUserButton.setButtonIcon(LauncherIcons.SWITCH_USER);
		this.switchUserButton.setAlign(FlatButton.AlignState.LEFT);
		this.switchUserButton.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

		this.launcher.getAccounts().addListener(account -> {
			if (account == null) {
				return;
			}

			this.updateUserIndicator(account);
		});

		this.updateUserIndicator(this.launcher.getAccounts().getActiveAccount());

		FlatButton optionsButton = new FlatButton("Options", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		optionsButton.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));
		optionsButton.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		optionsButton.setAlign(FlatButton.AlignState.LEFT);
		optionsButton.setButtonIcon(LauncherIcons.GEAR);
		optionsButton.addActionListener(e -> this.frame.showOptions());

		JLabel profilesLabel = new JLabel("PROFILES");
		profilesLabel.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f));
		profilesLabel.setForeground(new Color(200, 200, 200));
		profilesLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		FlatButton refreshButton = new FlatButton("Refresh", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		refreshButton.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		refreshButton.setAlign(FlatButton.AlignState.LEFT);
		refreshButton.setButtonIcon(LauncherIcons.REFRESH);
		refreshButton.addActionListener(e -> this.refresh(true));

		JPanel profilesHeader = new JPanel(new BorderLayout());
		profilesHeader.setOpaque(false);
		profilesHeader.add(refreshButton, BorderLayout.EAST);
		profilesHeader.add(profilesLabel, BorderLayout.WEST);

		JScrollPane instancesScroller = new JScrollPane(this.instancesTable);
		instancesScroller.getViewport().setOpaque(false);
		instancesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		instancesScroller.getVerticalScrollBar().setUI(new FlatScrollbarUI(instancesScroller.getVerticalScrollBar()));
		instancesScroller.setBorder(BorderFactory.createEmptyBorder());
		instancesScroller.setBackground(new Color(0x283038));

		JPanel left = new JPanel(new MigLayout("fill, insets 0", "[fill]", "[]4[]0[]12[]0[]0[]0"));
		left.add(this.userIndicator, "wrap");
		left.add(this.switchUserButton, "wrap");
		left.add(optionsButton, "wrap");
		left.add(profilesHeader, "wrap");
		left.add(instancesScroller, "grow, push, wrap");
		left.add(this.launchButton, "wrap");
		left.setBackground(new Color(0x424242));

		this.launcher.getUpdateManager().addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("pendingUpdate")) {
				boolean available = (Boolean) evt.getNewValue();

				if (available) {

					Timer timer = new Timer();
					timer.schedule(new TimerTask() {
						@Override
						public void run() {
							PlayView.this.installUpdates();
						}
					}, 1000);
				}
			}
		});

		if (this.launcher.getUpdateManager().getPendingUpdate()) {
			this.installUpdates();
		}

		this.instancesTable.setModel(this.instancesModel);
		this.instancesTable.setRowHeight(47);
		this.instancesTable.setBackground(new Color(0x262626));
		this.instancesTable.setForeground(Color.WHITE);
		this.instancesTable.setSelectionForeground(Color.WHITE);
		this.instancesTable.setSelectionBackground(new Color(0x0a1488));
		this.instancesTable.setBorder(BorderFactory.createEmptyBorder());
		this.instancesTable.setFocusable(false);
		this.instancesTable.getSelectionModel().addListSelectionListener(e -> {
			int row = this.instancesTable.getSelectedRow();

			if (row < 0) {
				return;
			}

			Instance instance = this.launcher.getInstances().get(row);

			this.selectionChanged(instance);
		});

		this.consolePanel = new ConsolePanel();

		this.news = new NewsFeedPanel();
		this.news.setOpaque(false);

		right.add(this.news, BorderLayout.CENTER);

		this.progressIndicatorPanel = new ProgressIndicatorPanel();

		right.add(this.progressIndicatorPanel, BorderLayout.SOUTH);

		this.bannerPanel = new BannerPanel();

		right.add(this.bannerPanel, BorderLayout.NORTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		splitPane.setResizeWeight(0.0D);
		splitPane.setDividerLocation(250);
		splitPane.setDividerSize(0);
		splitPane.setOpaque(false);

		JPanel container = new ImagePanel("com/gildedgames/assets/images/background.png");
		container.setLayout(new BorderLayout());
		container.add(splitPane, BorderLayout.CENTER);

		this.add(container, BorderLayout.CENTER);

		this.instancesModel.addTableModelListener(e -> {
			if (this.instancesTable.getRowCount() > 0) {
				this.instancesTable.setRowSelectionInterval(0, 0);
			}
		});

		this.instancesTable.addMouseListener(new DoubleClickToButtonAdapter(this.launchButton));

		this.switchUserButton.addActionListener(e -> {
			AccountListView view = new AccountListView(this.launcher, this.frame);
			view.setCallback(value -> {
				if (value != null) {
					this.updateUserIndicator(value);
				}
			});

			this.frame.getLauncherLayout().show(view);
		});

		this.launchButton.addActionListener(e -> this.launch());

		this.instancesTable.addMouseListener(new PopupMouseAdapter() {
			@Override
			protected void showPopup(MouseEvent e) {
				int index = PlayView.this.instancesTable.rowAtPoint(e.getPoint());
				Instance selected = null;
				if (index >= 0) {
					PlayView.this.instancesTable.setRowSelectionInterval(index, index);
					selected = PlayView.this.launcher.getInstances().get(index);
				}
				PlayView.this.popupInstanceMenu(e.getComponent(), e.getX(), e.getY(), selected);
			}
		});
	}

	private void installUpdates() {
		ObservableFuture<File> future = this.launcher.getUpdateManager().performUpdate(this.frame);
	}

	private void layoutNewsTiles(NewsFeedManager.NewsFeed feed) {
		SwingUtilities.invokeLater(() -> {
			this.news.repopulate(this.frame.getNewsFeedManager(), feed);

			this.news.revalidate();
			this.news.repaint();
		});
	}

	public void updateUserIndicator(Account account) {
		this.userIndicator.setAccount(account);
	}

	private void popupInstanceMenu(Component component, int x, int y, final Instance selected) {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem menuItem;

		if (selected != null) {
			menuItem = new JMenuItem(!selected.isLocal() ? tr("instance.install") : tr("instance.launch"));
			menuItem.addActionListener(e -> this.launch());
			popup.add(menuItem);

			if (selected.isLocal()) {
				popup.addSeparator();

				menuItem = new JMenuItem(SharedLocale.tr("instance.openFolder"));
				menuItem.addActionListener(ActionListeners.browseDir(this, selected.getContentDir(), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.openSaves"));
				menuItem.addActionListener(ActionListeners.browseDir(this, new File(selected.getContentDir(), "saves"), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.openResourcePacks"));
				menuItem.addActionListener(ActionListeners.browseDir(this, new File(selected.getContentDir(), "resourcepacks"), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.openScreenshots"));
				menuItem.addActionListener(ActionListeners.browseDir(this, new File(selected.getContentDir(), "screenshots"), true));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.copyAsPath"));
				menuItem.addActionListener(e -> {
					File dir = selected.getContentDir();
					dir.mkdirs();
					SwingHelper.setClipboard(dir.getAbsolutePath());
				});
				popup.add(menuItem);

				popup.addSeparator();

				if (!selected.isUpdatePending()) {
					menuItem = new JMenuItem(SharedLocale.tr("instance.forceUpdate"));
					menuItem.addActionListener(e -> {
						selected.setUpdatePending(true);
						this.launch();
						this.refreshInstances();
					});
					popup.add(menuItem);
				}

				menuItem = new JMenuItem(SharedLocale.tr("instance.hardForceUpdate"));
				menuItem.addActionListener(e -> this.confirmHardUpdate(selected));
				popup.add(menuItem);

				menuItem = new JMenuItem(SharedLocale.tr("instance.deleteFiles"));
				menuItem.addActionListener(e -> this.confirmDelete(selected));
				popup.add(menuItem);
			}

			popup.addSeparator();
		}

		menuItem = new JMenuItem(SharedLocale.tr("launcher.refreshList"));
		menuItem.addActionListener(e -> PlayView.this.refresh(true));
		popup.add(menuItem);

		popup.show(component, x, y);
	}

	private void confirmDelete(Instance instance) {
		if (!SwingHelper.confirmDialog(this, tr("instance.confirmDelete", instance.getTitle()), SharedLocale.tr("confirmTitle"))) {
			return;
		}

		ObservableFuture<Instance> future = this.launcher.getInstanceTasks().delete(this.frame, instance);

		// Update the list of instances after updating
		future.addListener(() -> PlayView.this.refresh(true), SwingExecutor.INSTANCE);
	}

	private void confirmHardUpdate(Instance instance) {
		if (!SwingHelper.confirmDialog(this, SharedLocale.tr("instance.confirmHardUpdate"), SharedLocale.tr("confirmTitle"))) {
			return;
		}

		ObservableFuture<Instance> future = this.launcher.getInstanceTasks().hardUpdate(this.frame, instance);

		// Update the list of instances after updating
		future.addListener(() -> {
			this.launch();
			this.refreshInstances();
		}, SwingExecutor.INSTANCE);
	}

	private void refresh(boolean force) {
		ObservableFuture<InstanceList> refreshInstancesFuture = this.launcher.getInstanceTasks().reloadInstances();

		this.bannerPanel.update(LauncherIcons.REFRESH, "Checking for the latest updates", BannerPanel.BannerType.INFO);

		Futures.addCallback(refreshInstancesFuture, new FutureCallback<InstanceList>() {
			@Override
			public void onSuccess(@Nullable InstanceList result) {

			}

			@Override
			public void onFailure(Throwable t) {
				PlayView.this.bannerPanel.update(LauncherIcons.WARN, "There was a problem checking for updates.", BannerPanel.BannerType.ERROR);
				PlayView.this.bannerPanel.bindActionHandler(LauncherIcons.REFRESH, "Refresh", () -> PlayView.this.refresh(force));
			}
		});

		refreshInstancesFuture.addListener(() -> {
			this.refreshInstances();

			if (this.instancesTable.getRowCount() > 0) {
				this.instancesTable.setRowSelectionInterval(0, 0);
			}

			this.requestFocus();

			this.refreshNews(force);
		}, SwingExecutor.INSTANCE);
	}

	private void refreshNews(boolean force) {
		ListenableFuture<NewsFeedManager.NewsFeed> refreshNewsFuture = this.frame.getNewsFeedManager().refresh(force);

		Futures.addCallback(refreshNewsFuture, new FutureCallback<NewsFeedManager.NewsFeed>() {
			@Override
			public void onSuccess(@Nullable NewsFeedManager.NewsFeed result) {
				PlayView.this.bannerPanel.close();
				PlayView.this.layoutNewsTiles(result);
			}

			@Override
			public void onFailure(Throwable t) {
				PlayView.this.bannerPanel.update(LauncherIcons.WARN, "There was a problem fetching the latest news.", BannerPanel.BannerType.INFO);
				PlayView.this.bannerPanel.bindActionHandler(LauncherIcons.REFRESH, "Refresh", () -> PlayView.this.refresh(force));
			}
		});
	}

	private void launch() {
		if (this.isUpdating) {
			return;
		}

		final Instance instance = this.launcher.getInstances().get(this.instancesTable.getSelectedRow());

		if (!instance.isInstalled() || instance.isUpdatePending()) {
			this.update(instance);

			return;
		}

		final Account account = this.launcher.getAccounts().getActiveAccount();

		if (account == null) {
			AccountListView view = new AccountListView(this.launcher, this.frame);
			view.setCallback(value -> {
				if (value == null) {
					return;
				}

				this.launch();
			});

			this.frame.getLauncherLayout().show(view);
		} else {
			this.setBusyUpdating(true);

			BestEffortLoginCallable callable = new BestEffortLoginCallable(account);

			ObservableFuture<StoredSession> future = new ObservableFuture<>(this.launcher.getExecutor().submit(callable), callable);

			Futures.addCallback(future, new FutureCallback<StoredSession>() {
				@Override
				public void onSuccess(StoredSession result) {
					PlayView.this.launchSession(result, instance);
				}

				@Override
				public void onFailure(Throwable t) {
					PlayView.this.setBusyUpdating(false);

					PlayView.this.tryRelog(account);
				}
			}, SwingExecutor.INSTANCE);

			this.progressIndicatorPanel.beginReporting(future, future, "Refreshing token");
		}
	}

	private void update(Instance instance) {
		Updater updater = new Updater(this.launcher, instance);
		updater.setOnline(true);

		PlayView.this.setBusyUpdating(true);

		ObservableFuture<Instance> future = new ObservableFuture<>(this.launcher.getExecutor().submit(updater), updater);

		this.progressIndicatorPanel.beginReporting(future, future, "Installing updates");

		SwingHelper.addErrorDialogCallback(this.frame, future);

		// Update the list of instances after updating
		future.addListener(this::refreshInstances, SwingExecutor.INSTANCE);

		future.addListener(() -> PlayView.this.setBusyUpdating(false), SwingExecutor.INSTANCE);
	}

	private void setBusyUpdating(boolean value) {
		this.launchButton.setStyle(value ? FlatButton.ButtonStyle.DISABLED : FlatButton.ButtonStyle.HIGHLIGHTED);

		this.isUpdating = value;
		this.frame.setUpdating(value);

		this.launchButton.repaint();
	}

	private void launchSession(Session session, Instance instance) {
		this.launchSupervisor.launch(this.progressIndicatorPanel, instance, session, new LaunchListenerImpl(this), new LaunchProcessHandler(this))
				.addListener(() -> PlayView.this.setBusyUpdating(false), SwingExecutor.INSTANCE);

	}

	private void refreshInstances() {
		int row = this.instancesTable.getSelectedRow();

		final Instance instance = row > 0 && row < this.launcher.getInstances().size() ? this.launcher.getInstances().get(row) : null;

		this.instancesModel.update();

		if (instance != null) {
			for (int i = 0; i < this.launcher.getInstances().getInstances().size(); i++) {
				if (instance.getName().equals(this.launcher.getInstances().get(i).getName())) {
					this.instancesTable.setRowSelectionInterval(i, i);

					this.selectionChanged(instance);
				}
			}
		}
	}

	private void selectionChanged(Instance instance) {
		if (!instance.isLocal()) {
			this.launchButton.setText("Install");
		} else if (instance.isUpdatePending()) {
			this.launchButton.setText("Update");
		} else {
			this.launchButton.setText("Play");
		}
	}

	private void tryRelog(Account account) {
		AccountRefreshView view = new AccountRefreshView(this.frame, this.launcher, account);
		view.setLoginCallback(value -> {
			if (value == null) {
				return;
			}

			this.launch();
		});
		view.setCancelable(true);

		this.frame.getLauncherLayout().show(view);
	}

	@Override
	public void reload() {
		this.refresh(true);
	}

	private static class LaunchListenerImpl implements LaunchListener {
		private final WeakReference<PlayView> viewRef;

		private final Launcher launcher;

		private LaunchListenerImpl(PlayView frame) {
			this.viewRef = new WeakReference<>(frame);
			this.launcher = frame.launcher;
		}

		@Override
		public void instancesUpdated() {
			PlayView view = this.viewRef.get();
			if (view != null) {
				view.refreshInstances();
			}
		}

		@Override
		public void gameStarted() {
			PlayView view = this.viewRef.get();
			if (view != null) {
				view.frame.dispose();
			}
			Animation.stopAll();
		}

		@Override
		public void gameClosed() {
			this.launcher.showLauncherWindow();
		}
	}

	private class BestEffortLoginCallable implements Callable<StoredSession>, ProgressObservable {
		private final Account account;

		private BestEffortLoginCallable(Account account) {
			this.account = account;
		}

		@Override
		public StoredSession call() throws IOException, InterruptedException, AuthenticationException {
			LoginService service = PlayView.this.launcher.getLoginService();

			Session session = service.refreshSession(this.account);

			this.account.setSession((StoredSession) session);
			this.account.setLastUsed(new Date());

			Persistence.commitAndForget(PlayView.this.launcher.getAccounts());

			return (StoredSession) session;
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
