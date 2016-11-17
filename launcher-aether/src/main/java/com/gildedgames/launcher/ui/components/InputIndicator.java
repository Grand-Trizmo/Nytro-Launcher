package com.gildedgames.launcher.ui.components;

import com.gildedgames.launcher.ui.animations.LoopingAnimation;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class InputIndicator extends JComponent {
	private final LoopingAnimation animation = new LoopingAnimation(this);

	private int size = 16;

	private boolean isChecking = false;

	private boolean isValid = true;

	private String text = "";

	private Image icon;

	public InputIndicator() {
		this.setVisible(true);
		this.setForeground(Color.WHITE);

		this.setBorder(BorderFactory.createEmptyBorder(12, 0, 6, 6));
	}

	@Override
	public Dimension getPreferredSize() {
		Insets insets = this.getBorder().getBorderInsets(this);

		return new Dimension(this.size + insets.left + insets.right + 350, this.size + insets.top + insets.bottom);
	}

	public void setValid(boolean val) {
		this.icon = val ? LauncherIcons.INPUT_VALID.getImage() : LauncherIcons.INPUT_INVALID.getImage();

		SwingUtilities.invokeLater(this::repaint);
	}

	public void setText(String text) {
		this.text = text;

		SwingUtilities.invokeLater(this::repaint);
	}

	public void setChecking(boolean val) {
		this.isChecking = val;

		if (!val) {
			this.animation.stop();
		} else {
			this.animation.run(75);
		}

		SwingUtilities.invokeLater(this::repaint);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();

		Insets insets = this.getBorder().getBorderInsets(this);

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g2.setColor(Color.WHITE);

		g2.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		g2.drawString(this.text, insets.left + 24, (this.getHeight() / 2) + (g2.getFontMetrics().getHeight() / 2));

		if (this.isChecking) {
			this.drawSpinner(insets.left, insets.top, g2);
		} else if (this.icon != null) {
			this.drawIcon(insets.left, insets.top, g2);
		}
	}

	private void drawIcon(int x, int y, Graphics2D g2) {
		g2.drawImage(this.icon, x, y, null);
	}

	private void drawSpinner(int x, int y, Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(this.getForeground());

		int barRectWidth = 16;
		int barRectHeight = 16;

		double degree = 360 - (360 * this.animation.getProgress());

		double sz = Math.min(barRectWidth, barRectHeight);

		double cx = x + barRectWidth * .5;
		double cy = y + barRectHeight * .5;
		double or = sz * .5;
		double ir = or * .7;

		Shape inner = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2, ir * 2);
		Shape outer = new Arc2D.Double(cx - or, cy - or, sz, sz, degree, 150, Arc2D.PIE);

		Area area = new Area(outer);
		area.subtract(new Area(inner));

		g2.fill(area);
		g2.dispose();
	}
}
