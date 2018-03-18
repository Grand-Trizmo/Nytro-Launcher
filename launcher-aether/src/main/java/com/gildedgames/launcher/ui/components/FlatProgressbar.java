package com.gildedgames.launcher.ui.components;

import javax.swing.JProgressBar;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class FlatProgressbar extends JProgressBar {
	private static final Color COLOR_UNFILLED = new Color(119, 119, 119);

	private static final Color COLOR_FILLED = new Color(0x651FFF);

	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		g2.clearRect(0, 2, this.getWidth(), this.getHeight() - 4);

		g2.setColor(COLOR_UNFILLED);
		g2.fillRect(0, 2, this.getWidth(), this.getHeight() - 4);

		int fill = (int) Math.floor(((float) this.getWidth() / (float) this.getMaximum()) * this.getValue());

		g2.setColor(COLOR_FILLED);
		g2.fillRect(0, 2, fill, this.getHeight() - 4);
	}
}
