package com.gildedgames.launcher.ui.components;

import javax.swing.JTextField;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class PlaceholderTextField extends JTextField {
	private final String placeholder;

	public PlaceholderTextField(String placeholder) {
		this.placeholder = placeholder;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.getText().length() > 0) {
			return;
		}

		final Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(this.getDisabledTextColor());
		g2.drawString(this.placeholder, this.getInsets().left, g.getFontMetrics().getMaxAscent() + this.getInsets().top);
	}
}
