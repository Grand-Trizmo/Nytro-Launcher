package com.gildedgames.launcher.ui.components.updater;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.swing.SwingHelper;

import javax.swing.JOptionPane;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateButton extends FlatButton {
	private boolean isUpdateInstalling;

	private boolean isUpdatePendingRestart;

	private Timer timer;

	private int ticks;

	public UpdateButton(Launcher launcher, LauncherFrame frame) {
		super("Updating...", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));

		this.setStyle(ButtonStyle.TRANSPARENT);
		this.setButtonIcon(LauncherIcons.REFRESH);

		launcher.getUpdateManager().addPropertyChangeListener(evt -> {
			if (evt.getPropertyName().equals("pendingUpdate")) {
				this.updateState((Boolean) evt.getNewValue());
			}
		});

		this.addActionListener(e -> {
			if (this.isUpdatePendingRestart) {
				SwingHelper.showMessageDialog(frame, "Please restart your launcher to install the latest updates.", "Update notification", null, JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}

	public void updateState(boolean updateAvailable) {
		if (this.timer != null) {
			this.timer.cancel();
		}

		if (updateAvailable) {
			this.setVisible(true);
		}

		if (this.isUpdateInstalling) {
			this.isUpdatePendingRestart = true;
		}

		this.isUpdateInstalling = updateAvailable;

		if (this.isUpdateInstalling) {
			this.setText("Updating...");
			this.setButtonIcon(LauncherIcons.REFRESH);
			this.setStyle(ButtonStyle.TRANSPARENT);
		} else if (this.isUpdatePendingRestart) {
			this.setText("Updates available");
			this.setButtonIcon(LauncherIcons.WARN);
//			this.setStyle(ButtonStyle.HIGHLIGHTED);
		}

		this.setVisible(this.isUpdatePendingRestart || this.isUpdateInstalling);

		if (this.isUpdateInstalling) {
			this.ticks = 0;

			this.timer = new Timer();
			this.timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					UpdateButton.this.ticks += 15;
					UpdateButton.this.repaint();
				}
			}, 0, 50);
		}
	}

	@Override
	protected void drawIcon(Graphics2D g2, int x, int y) {
		if (!this.isUpdateInstalling) {
			super.drawIcon(g2, x, y);

			return;
		}

		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		AffineTransform identity = new AffineTransform();
		identity.translate(x, y);

		AffineTransform trans = new AffineTransform();
		trans.setTransform(identity);
		trans.rotate(Math.toRadians(this.ticks % 360), 8, 8);

		g2.drawImage(LauncherIcons.REFRESH.getImage(), trans, null);
	}
}
