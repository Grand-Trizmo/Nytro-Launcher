package com.gildedgames.launcher.ui.components;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class FlatTextField extends JTextField {
	private final String placeholder;

	public FlatTextField(String placeholder) {
		this.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		this.setEditable(true);

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

	@Override
	public void setEditable(boolean isEditable) {
		super.setEditable(isEditable);

		if (isEditable) {
			this.setBackground(Color.WHITE);
			this.setForeground(new Color(30, 30, 30));
		} else {
			this.setBackground(new Color(140, 140, 140));
			this.setForeground(new Color(60, 60, 60));
		}
	}
}
