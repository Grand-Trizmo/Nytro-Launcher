package com.gildedgames.launcher.ui.components;

import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.styles.FlatScrollbarUI;
import com.gildedgames.launcher.util.Pair;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class FlatTabPane extends JPanel {
	private final List<Pair<FlatTabButton, JComponent>> tabs = new ArrayList<>();

	private final JPanel layoutRoot = new JPanel();

	private final JScrollPane scroller = new JScrollPane(this.layoutRoot);

	private final JPanel buttonContainer = new JPanel();

	public FlatTabPane() {
		this.setOpaque(false);
		this.setLayout(new BorderLayout());

		this.initComponents();
	}

	private void initComponents() {
		this.layoutRoot.setLayout(new BorderLayout());
		this.layoutRoot.setOpaque(false);
		this.layoutRoot.setBorder(BorderFactory.createEmptyBorder(4, 20, 4, 20));

		this.scroller.setOpaque(false);
		this.scroller.getViewport().setOpaque(false);
		this.scroller.setBorder(BorderFactory.createEmptyBorder());
		this.scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		this.scroller.getVerticalScrollBar().setUI(new FlatScrollbarUI(this.scroller.getVerticalScrollBar()));
		this.scroller.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

		FlowLayout buttonLayout = new FlowLayout();
		buttonLayout.setAlignment(FlowLayout.LEFT);

		this.buttonContainer.setLayout(buttonLayout);
		this.buttonContainer.setOpaque(false);
		this.buttonContainer.add(Box.createHorizontalStrut(12));

		this.add(this.buttonContainer, BorderLayout.NORTH);
		this.add(this.scroller, BorderLayout.CENTER);
	}

	public void addTab(String title, JComponent comp) {
		FlatTabButton button = new FlatTabButton(title);
		button.addActionListener(e -> this.setActive(comp));

		this.buttonContainer.add(button);
		this.tabs.add(new Pair<>(button, comp));
	}

	public void setActive(JComponent comp) {
		this.layoutRoot.removeAll();
		this.layoutRoot.add(comp, BorderLayout.NORTH);

		this.layoutRoot.revalidate();
		this.layoutRoot.repaint();

		for (Pair<FlatTabButton, JComponent> pair : this.tabs) {
			FlatTabButton button = pair.getLeft();
			button.setSelected(pair.getRight() == comp);
			button.revalidate();
			button.repaint();
		}
	}

	private class FlatTabButton extends JButton {
		private final Font normalFont = LauncherFonts.OPEN_SANS_REGULAR.deriveFont(15.0f),
			selectedFont = LauncherFonts.OPEN_SANS_BOLD.deriveFont(15.0f);

		private boolean hovered;

		@Setter
		private boolean selected;

		public FlatTabButton(String text) {
			this.setText(text.toUpperCase());
			this.setForeground(Color.WHITE);

			this.setMargin(new Insets(0, 0, 0, 0));
			this.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					FlatTabButton.this.hovered = true;
				}

				@Override
				public void mouseExited(MouseEvent e) {
					FlatTabButton.this.hovered = false;
				}
			});

			this.setOpaque(false);
		}

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			float textOpacity = (this.selected ? 1.0f : (this.hovered ? 0.85f : 0.6f));

			if (textOpacity < 1.0f) {
				AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textOpacity);
				g2.setComposite(composite);
			}

			FontMetrics fontMetrics = g.getFontMetrics();
			int textHeight = fontMetrics.getHeight();

			g2.setFont(this.selected ? this.selectedFont : this.normalFont);
			g2.setColor(this.getForeground());

			g2.drawString(this.getText(), 0, (this.getHeight() / 2) - (textHeight / 2) + fontMetrics.getAscent());
		}
	}
}
