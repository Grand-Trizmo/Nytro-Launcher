package com.gildedgames.launcher.ui.panels;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.components.WindowButton;
import com.gildedgames.launcher.ui.components.updater.UpdateButton;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;
import com.gildedgames.launcher.ui.resources.LauncherStyles;
import com.gildedgames.launcher.util.BrowserUtil;
import com.skcraft.launcher.Launcher;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class TitlebarPanel extends JPanel {
	private static final String SUPPORT_URL = "https://discord.gg/UgeRyED";

	private static final String SHOP_URL = "http://shop.nytro.co";

	private static final String PLAYER_COUNT_URL = "http://nytro.co";

	private final LauncherFrame frame;

	public TitlebarPanel(Launcher launcher, final LauncherFrame frame) {
		this.frame = frame;

		Font font = LauncherFonts.OPEN_SANS_REGULAR;

		this.setLayout(new MigLayout("align center, insets 0", "[][]16[]push[]12[]12[][][]"));
		this.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);

		JLabel icon = new JLabel();
		icon.setIcon(LauncherIcons.WINDOW_ICON);
		icon.setForeground(new Color(200, 200, 200, 255));
		icon.setFont(font.deriveFont(16.0f));
		icon.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));

		this.add(icon);

		JLabel title = new JLabel(this.frame.getTitle());
		title.setForeground(new Color(200, 200, 200, 255));
		title.setFont(font.deriveFont(16.0f));

		this.add(title);

		UpdateButton updateButton = new UpdateButton(launcher, frame);
		updateButton.updateState(launcher.getUpdateManager().getPendingUpdate());
		updateButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));

		launcher.getUpdateManager().addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("pendingUpdate")) {
				boolean available = (Boolean) evt.getNewValue();

				updateButton.updateState(available);
			}
		});

		this.add(updateButton);
/*
		FlatButton players = new FlatButton("Current Players on Nytro", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		players.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		players.setButtonIcon(LauncherIcons.KKMC);
		players.addActionListener(e -> BrowserUtil.openPage(PLAYER_COUNT_URL));
		players.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		this.add(players);
*/

		FlatButton shop = new FlatButton("   Donate to support Nytro", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(13.5f));
		shop.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		shop.setButtonIcon(LauncherIcons.SHOP);
		shop.addActionListener(e -> BrowserUtil.openPage(SHOP_URL));
		shop.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		this.add(shop);

		FlatButton bug = new FlatButton("   Support", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(13.5f));
		bug.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		bug.setButtonIcon(LauncherIcons.BUG);
		bug.addActionListener(e -> BrowserUtil.openPage(SUPPORT_URL));
		bug.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		this.add(bug);

		WindowButton minimizeButton = new WindowButton(LauncherIcons.WINDOW_MINIMIZE);
		minimizeButton.addActionListener(e -> frame.setState(JFrame.ICONIFIED));

		this.add(minimizeButton);

		WindowButton maximizeButton = new WindowButton(LauncherIcons.WINDOW_MAXIMIZE);
		maximizeButton.addActionListener(e -> frame.toggleMaximize());

		this.add(maximizeButton);

		WindowButton closeButton = new WindowButton(LauncherIcons.WINDOW_CLOSE);
		closeButton.addActionListener(e -> frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING)));

		this.add(closeButton);

		MouseAdapter mouseAdapter = new MouseAdapter() {
			private int lastDragX, lastDragY;

			private boolean isDragging;

			private long lastClick;

			private LauncherFrame frame = TitlebarPanel.this.frame;

			@Override
			public void mouseClicked(MouseEvent e) {

			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (this.frame.isMaximized()) {
					return;
				}

				this.isDragging = true;

				this.lastDragX = e.getX();
				this.lastDragY = e.getY();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				this.isDragging = false;

				long now = System.currentTimeMillis();

				long since = now - this.lastClick;

				if (since <= 150) {
					this.frame.toggleMaximize();
				}

				this.lastClick = now;
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (this.isDragging) {
					Point cursor = e.getLocationOnScreen();

					this.frame.setLocation(cursor.x - this.lastDragX - LauncherFrame.RESIZE_BORDER, cursor.y - this.lastDragY - LauncherFrame.RESIZE_BORDER);
				}
			}
		};

		this.addMouseListener(mouseAdapter);
		this.addMouseMotionListener(mouseAdapter);
	}
}
