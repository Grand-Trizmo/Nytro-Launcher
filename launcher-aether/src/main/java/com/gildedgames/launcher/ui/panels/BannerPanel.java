package com.gildedgames.launcher.ui.panels;

import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BannerPanel extends JPanel {
	public enum BannerType {
		INFO(new Color(0x0a7cd2)),
		ERROR(new Color(0xc62828));

		private final Color color;

		BannerType(Color color) {
			this.color = color;
		}
	}

	private boolean closeable = false;

	private JLabel label, icon;

	private FlatButton buttonAction;

	private Runnable onClickRunnable;

	public BannerPanel() {
		this.initComponents();
	}

	private void initComponents() {
		this.setLayout(new MigLayout("fill, insets 10 14 14 10", "[]8[]push[]", "[grow]"));

		this.label = new JLabel();
		this.label.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.label.setForeground(Color.WHITE);

		this.icon = new JLabel();

		this.buttonAction = new FlatButton("Close", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.buttonAction.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
		this.buttonAction.setPreferredSize(new Dimension(120, 24));
		this.buttonAction.setAlign(FlatButton.AlignState.RIGHT);
		this.buttonAction.setBorder(BorderFactory.createEmptyBorder());
		this.buttonAction.addActionListener(e -> {
			if (this.onClickRunnable != null) {
				this.onClickRunnable.run();
			}
		});

		this.add(this.icon);
		this.add(this.label);
		this.add(this.buttonAction);
	}

	public void update(ImageIcon icon, String text, BannerType type) {
		this.label.setText(text);
		this.icon.setIcon(icon);

		this.setBackground(type.color);

		this.bindActionHandler(null, "Close", null);

		this.setVisible(true);
	}

	public void bindActionHandler(ImageIcon icon, String label, Runnable clickHandler) {
		this.onClickRunnable = clickHandler;

		this.buttonAction.setText(label);
		this.buttonAction.setButtonIcon(icon);
		this.buttonAction.setVisible(this.onClickRunnable != null);
	}

	public void close() {
		this.setVisible(false);
	}
}
