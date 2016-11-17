package com.gildedgames.launcher.ui.components;

import com.gildedgames.launcher.ui.animations.LoopingAnimation;

import javax.swing.JComponent;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class FlatSpinner extends JComponent {
	private final LoopingAnimation animation = new LoopingAnimation(this);

	private int size;

	public FlatSpinner(int size) {
		this.setVisible(true);
		this.setForeground(Color.WHITE);

		this.size = size;
	}

	@Override
	public Dimension getPreferredSize() {
		Insets insets = this.getBorder().getBorderInsets(this);

		return new Dimension(this.size + insets.left + insets.right, this.size + insets.top + insets.bottom);
	}

	@Override
	public void setVisible(boolean val) {
		if (!val) {
			this.animation.stop();
		} else {
			this.animation.run(75);
		}

		super.setVisible(val);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(this.getForeground());

		Insets b = this.getInsets();

		int barRectWidth  = this.getWidth() - b.right - b.left;
		int barRectHeight = this.getHeight() - b.top - b.bottom;

		if (barRectWidth <= 0 || barRectHeight <= 0) {
			return;
		}

		double degree = 360 - (360 * this.animation.getProgress());

		double sz = Math.min(barRectWidth, barRectHeight);

		double cx = b.left + barRectWidth * .5;
		double cy = b.top + barRectHeight * .5;
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
