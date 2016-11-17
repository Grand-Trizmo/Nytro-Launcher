package com.gildedgames.launcher.ui.styles;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class FlatScrollbarUI extends BasicScrollBarUI {
	private boolean isHovered;

	private boolean isPressed;

	private boolean vertical;

	public FlatScrollbarUI(JScrollBar bar) {
		this.vertical = bar.getOrientation() == JScrollBar.VERTICAL;

		bar.setOpaque(false);

		bar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				FlatScrollbarUI.this.isPressed = true;
				bar.repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				FlatScrollbarUI.this.isPressed = false;
				bar.repaint();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				FlatScrollbarUI.this.isHovered = true;
				bar.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				FlatScrollbarUI.this.isHovered = false;
				bar.repaint();
			}
		});
	}

	@Override
	public void installDefaults() {
		super.installDefaults();

		this.scrollBarWidth = 16;
	}

	@Override
	protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
		g.setColor(new Color(255, 255, 255, 60));

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (this.vertical) {
			g2.fill(new RoundRectangle2D.Float(trackBounds.x + 8, trackBounds.y, trackBounds.width - 8, trackBounds.height, 8, 8));
		} else {
			g2.fill(new RoundRectangle2D.Float(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height - 8, 8, 8));
		}
	}

	@Override
	protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
		g.setColor(this.isHovered || this.isPressed ? new Color(255, 255, 255, 160) : new Color(255, 255, 255, 90));

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		if (this.vertical) {
			g2.fill(new RoundRectangle2D.Float(thumbBounds.x + 8, thumbBounds.y, thumbBounds.width - 8, thumbBounds.height, 8, 8));
		} else {
			g2.fill(new RoundRectangle2D.Float(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height - 8, 8, 8));
		}
	}

	@Override
	protected JButton createDecreaseButton(int orientation) {
		JButton b = new JButton();
		b.setVisible(false);
		b.setPreferredSize(new Dimension(0, 0));
		b.setMaximumSize(new Dimension(0, 0));

		return b;
	}

	@Override
	protected JButton createIncreaseButton(int orientation) {
		JButton b = new JButton();
		b.setVisible(false);
		b.setPreferredSize(new Dimension(0, 0));
		b.setMaximumSize(new Dimension(0, 0));

		return b;
	}

	@Override
	protected void configureScrollBarColors() {

	}
}