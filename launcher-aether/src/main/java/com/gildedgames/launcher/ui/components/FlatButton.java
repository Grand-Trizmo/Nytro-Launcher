package com.gildedgames.launcher.ui.components;

import lombok.Getter;
import lombok.Setter;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FlatButton extends JButton {
	public enum ButtonStyle {
		NORMAL(new Color(0x333d47), new Color(0x4d5c6b), new Color(0x333d47), Color.WHITE),
		DISABLED(new Color(0x777777), new Color(0x777777), new Color(0x777777), new Color(0x444444)),
		LIGHT(new Color(0x333d47), new Color(0x4d5c6b), new Color(0x333d47), Color.WHITE),
		HIGHLIGHTED(new Color(0x3078c2), new Color(0x3d8ee0), new Color(0x2f68a2), Color.WHITE),
		TRANSPARENT(null, null, new Color(0, 0, 0, 40), Color.WHITE);

		private final Color bgNormal, bgHover, bgPressed;

		@Getter
		private final Color text;

		ButtonStyle(Color bgNormal, Color bgHover, Color bgPressed, Color text) {
			this.bgNormal = bgNormal;
			this.bgHover = bgHover;
			this.bgPressed = bgPressed;
			this.text = text;
		}

		public float getTextOpacity(boolean isHovered, boolean isPressed) {
			if (this == TRANSPARENT) {
				return isPressed ? 0.9f : isHovered ? 0.8f : 0.5f;
			}

			return 1.0f;
		}

		public Color getBackground(boolean isHovered, boolean isPressed) {
			return isPressed ? this.bgPressed : (isHovered ? this.bgHover : this.bgNormal);
		}
	}

	public enum AlignState {
		LEFT, CENTER, RIGHT
	}

	@Getter
	@Setter
	private ImageIcon buttonIcon;

	@Getter
	@Setter
	private ButtonStyle style = ButtonStyle.NORMAL;

	@Getter
	@Setter
	private AlignState align = AlignState.CENTER;

	private boolean hovered;

	private boolean pressed;

	public FlatButton(String text, Font font) {
		this(text);

		this.setFont(font);
		this.setText(text);
	}

	public FlatButton(String text) {
		this.setText(text);

		this.setMargin(new Insets(0, 0, 0, 0));
		this.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				FlatButton.this.hovered = true;
				FlatButton.this.repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				FlatButton.this.hovered = false;
				FlatButton.this.pressed = false;

				FlatButton.this.repaint();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				FlatButton.this.pressed = true;
				FlatButton.this.hovered = false;

				FlatButton.this.repaint();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				FlatButton.this.pressed = false;
				FlatButton.this.hovered = false;

				FlatButton.this.repaint();
			}
		});

		this.setOpaque(false);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		Color color = this.getStyle().getBackground(this.hovered, this.pressed);

		if (color != null) {
			g2.setColor(color);
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		}

		g2.setColor(this.style.getText());

		FontMetrics fontMetrics = g.getFontMetrics();
		int textWidth = fontMetrics.stringWidth(this.getText());
		int textHeight = fontMetrics.getHeight();

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		float textOpacity = this.getStyle().getTextOpacity(this.hovered, this.pressed);

		if (textOpacity < 1.0f) {
			AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, textOpacity);
			g2.setComposite(composite);
		}

		if (this.getButtonIcon() != null) {
			int x = 0;

			if (this.getAlign() == AlignState.LEFT) {
				x = 12;
			} else if (this.getAlign() == AlignState.RIGHT) {
				x = this.getWidth() - textWidth - 36;
			} else if (this.getAlign() == AlignState.CENTER) {
				x = (this.getWidth() / 2) - ((this.getButtonIcon().getIconWidth() + textWidth + 24) / 2) + 6;
			}

			this.drawIcon(g2, x, (this.getHeight() / 2) - (this.getButtonIcon().getIconHeight() / 2));

			if (this.getText() != null) {
				g2.setFont(this.getFont());
				g2.drawString(this.getText(), x + 24, (this.getHeight() / 2) - (textHeight / 2) + fontMetrics.getAscent());
			}
		} else {
			int x = 0;

			if (this.getAlign() == AlignState.LEFT) {
				x = 12;
			} else if (this.getAlign() == AlignState.CENTER) {
				x = (this.getWidth() / 2) - (textWidth / 2);
			} else if (this.getAlign() == AlignState.RIGHT) {
				x = this.getWidth() - textWidth - 24;
			}

			if (this.getText() != null) {
				g2.setFont(this.getFont());
				g2.drawString(this.getText(), x, (this.getHeight() / 2) - (textHeight / 2) + fontMetrics.getAscent());
			}
		}
	}

	protected void drawIcon(Graphics2D g2, int x, int y) {
		g2.drawImage(this.getButtonIcon().getImage(), x, y, null);
	}

	@Override
	public void setText(String text) {
		super.setText(text.toUpperCase());
	}
}
