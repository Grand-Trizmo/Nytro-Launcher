package com.gildedgames.launcher.ui;

import com.gildedgames.launcher.launch.LaunchSupervisor;
import com.gildedgames.launcher.ui.panels.TitlebarPanel;
import com.gildedgames.launcher.ui.resources.AvatarManager;
import com.gildedgames.launcher.ui.resources.NewsFeedManager;
import com.gildedgames.launcher.ui.views.OptionsView;
import com.gildedgames.launcher.ui.views.account.AccountAddView;
import com.gildedgames.launcher.ui.views.game.PlayView;
import com.gildedgames.launcher.user.GameKeyManager;
import com.gildedgames.launcher.util.ComponentResizer;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.java.Log;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import java.awt.*;

import static com.skcraft.launcher.util.SharedLocale.tr;

@Log
public class LauncherFrame extends JFrame {
	public static final int RESIZE_BORDER = 5;

	private final Launcher launcher;

	@Getter
	private final LaunchSupervisor launchSupervisor;

	private TitlebarPanel titlebar;

	private LauncherLayout layout = new LauncherLayout();

	@Getter
	private AvatarManager avatarManager;

	@Getter
	private NewsFeedManager newsFeedManager;

	@Getter
	private GameKeyManager keyManager;

	@Setter
	@Getter
	private boolean isUpdating;

	private JPanel contentPanel;

	private ComponentResizer resizer;

	private Rectangle boundsBeforeMaximize;

	@Getter
	private boolean maximized;

	public LauncherFrame(@NonNull Launcher launcher) {
		super(tr("launcher.title", launcher.getVersion()));

		this.launcher = launcher;

		this.avatarManager = AvatarManager.load(launcher);
		this.newsFeedManager = NewsFeedManager.load(launcher);
		this.keyManager = new GameKeyManager(this.launcher);

		this.launchSupervisor = new LaunchSupervisor(this.launcher);

		this.setUndecorated(true);
		this.setBackground(new Color(0, 0, 0, 0));

		Dimension maxSize = this.getMaxSize();

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setMinimumSize(new Dimension(960, 580));
		this.setPreferredSize(new Dimension(1150, 800));
		this.setMaximumSize(maxSize);

		this.initComponents();
		this.pack();
		this.setLocationRelativeTo(null);

		SwingHelper.removeFocusBorder(this.getContentPane());

		SwingHelper.setFrameIcon(this, Launcher.class, "icon.png");

		this.setVisible(true);

		this.getRootPane().setBorder(new LineBorder(new Color(10, 10, 10, 1), RESIZE_BORDER));

		this.resizer = new ComponentResizer();
		this.resizer.registerComponent(this);
		this.resizer.setMaximumSize(maxSize);
	}

	private void initComponents() {
		this.titlebar = new TitlebarPanel(this.launcher, this);

		this.contentPanel = new JPanel(new BorderLayout());
		this.contentPanel.setOpaque(false);
		this.contentPanel.add(this.titlebar, BorderLayout.NORTH);
		this.contentPanel.add(this.layout.getRoot(), BorderLayout.CENTER);
		this.contentPanel.setBorder(new LineBorder(new Color(10, 10, 10), 1));

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(this.contentPanel, BorderLayout.CENTER);

		PlayView view = new PlayView(this.launcher, this);

		this.layout.show(view);

		if (this.launcher.getAccounts().getSize() <= 0) {
			AccountAddView accountAddView = new AccountAddView(this, this.launcher);
			accountAddView.setLoginCallback(account -> {
				this.launcher.getAccounts().add(account);
				this.launcher.getAccounts().setSelectedAccount(account);

				Persistence.commitAndForget(LauncherFrame.this.launcher.getAccounts());

				view.updateUserIndicator(account);
			});

			this.layout.show(accountAddView);
		}
	}

	public void showOptions() {
		OptionsView view = new OptionsView(this.launcher, this);

		this.getLauncherLayout().show(view);
	}

	public LauncherLayout getLauncherLayout() {
		return this.layout;
	}

	public void toggleMaximize() {
		if (this.maximized) {
			this.unmaximize();
		} else {
			this.maximize();
		}
	}

	public void maximize() {
		this.maximized = true;
		this.resizer.setDisabled(true);

		GraphicsConfiguration gc = this.getGC(this.getMousePosition());

		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

		Rectangle bounds = gc.getBounds();
		bounds.x += screenInsets.left - RESIZE_BORDER;
		bounds.y += screenInsets.top - RESIZE_BORDER;
		bounds.height -= screenInsets.bottom + screenInsets.top - (RESIZE_BORDER * 2);
		bounds.width -= screenInsets.left + screenInsets.right - (RESIZE_BORDER * 2);

		this.boundsBeforeMaximize = this.getBounds();

		this.setBounds(bounds);
	}

	public void unmaximize() {
		this.unmaximize(this.boundsBeforeMaximize);
	}

	public void unmaximize(Rectangle bounds) {
		this.maximized = false;
		this.resizer.setDisabled(false);

		if (bounds != null) {
			this.setBounds(bounds);
		}
	}

	private GraphicsConfiguration getGC(Point point) {
		for (GraphicsDevice gd : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()) {
			if (gd.getDefaultConfiguration().getBounds().contains(point)) {
				return gd.getDefaultConfiguration();
			}
		}

		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	}

	private Dimension getMaxSize() {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

		return new Dimension(width + (RESIZE_BORDER * 2), height + (RESIZE_BORDER * 2));
	}
}
