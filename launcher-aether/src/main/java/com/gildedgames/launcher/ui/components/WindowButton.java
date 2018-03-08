package com.gildedgames.launcher.ui.components;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WindowButton extends JButton {
	private final ImageIcon icon;

	private boolean hovered, pressed;

	public WindowButton(ImageIcon icon) {
		this.icon = icon;
		this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				WindowButton.this.hovered = true;

				SwingUtilities.invokeLater(WindowButton.this::repaint);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				WindowButton.this.hovered = false;

				SwingUtilities.invokeLater(WindowButton.this::repaint);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				WindowButton.this.pressed = true;

				SwingUtilities.invokeLater(WindowButton.this::repaint);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				WindowButton.this.pressed = false;

				SwingUtilities.invokeLater(WindowButton.this::repaint);
			}
		});

		this.setOpaque(false);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		int width = this.icon.getIconWidth();
		int height = this.icon.getIconHeight();

		if (this.hovered || this.pressed) {
			g2.setColor(new Color(0, 0, 0, this.pressed ? 50 : 20));
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		}

		AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, this.pressed ? 1.0f : this.hovered ? 0.8f : 0.4f);

		g2.setComposite(composite);
		g2.drawImage(this.icon.getImage(), (this.getWidth() / 2) - (width / 2), (this.getHeight() / 2) - (height / 2), null);
	}
}
