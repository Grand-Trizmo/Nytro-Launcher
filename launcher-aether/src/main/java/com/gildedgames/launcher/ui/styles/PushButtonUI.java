package com.gildedgames.launcher.ui.styles;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class PushButtonUI extends BasicButtonUI {
	private static final Color UNSELECTED_HOVER_FILL = new Color(0x666666),
			UNSELECTED_FILL = new Color(0x555555);

	private static final Color SELECTED_HOVER_FILL = new Color(0x0a1488),
			SELECTED_FILL = new Color(0x0a7cd2);

	private static final Color KNOB_FILL = new Color(0xffffff);

	private boolean hovered;

	@Override
	public void installUI(JComponent component) {
		super.installUI(component);

		component.setOpaque(false);
		component.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				PushButtonUI.this.hovered = true;

				component.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				PushButtonUI.this.hovered = false;

				component.repaint();
			}
		});
	}
	
	@Override
	public void paint(Graphics g, JComponent c) {
		JCheckBox checkbox = (JCheckBox) c;

		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int y = (c.getHeight() / 2) - 11;

		RoundRectangle2D track = new RoundRectangle2D.Double(2, y + 2, 42, 18, 18, 18);

		if (checkbox.isSelected()) {
			g2.setColor(this.hovered ? SELECTED_HOVER_FILL : SELECTED_FILL);
			g2.fill(track);
		} else {
			g2.setColor(this.hovered ? UNSELECTED_HOVER_FILL : UNSELECTED_FILL);
			g2.fill(track);
		}

		Ellipse2D knob = new Ellipse2D.Double(checkbox.isSelected() ? 30 : 6, y + 6, 10, 10);

		g2.setColor(KNOB_FILL);
		g2.fill(knob);

		g2.setColor(c.getForeground());
		g2.setFont(c.getFont());
		g2.drawString(((JCheckBox) c).getText(), 55, y + 16);
	}

	@Override
	public Dimension getPreferredSize(JComponent c) {
		JCheckBox checkbox = (JCheckBox) c;

		int height = 22;
		int width = 55 + c.getFontMetrics(c.getFont()).stringWidth(checkbox.getText()) + 2;

		return new Dimension(width, height);
	}
}
