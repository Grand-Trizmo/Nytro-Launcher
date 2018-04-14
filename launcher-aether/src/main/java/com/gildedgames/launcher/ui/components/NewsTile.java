package com.gildedgames.launcher.ui.components;

import com.gildedgames.launcher.ui.animations.TimedAnimation;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;
import com.gildedgames.launcher.ui.resources.NewsFeedManager.NewsPost;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.util.SwingExecutor;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class NewsTile extends JComponent {
	private static final Color TRANSPARENT = new Color(0, 0, 0, 1),
			BACKGROUND = new Color(40, 40, 40),
			BORDER = new Color(255, 255, 255, 60);

	private static final Font FONT_TITLE = LauncherFonts.OPEN_SANS_REGULAR.deriveFont(18.0f),
			FONT_DATE = LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f),
			FONT_ERROR = LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f);

	private static final Image WARNING_ICON = LauncherIcons.load("com/gildedgames/assets/icons/64/warn.png");

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd, yyyy");

	private final NewsPost post;

	private TimedAnimation hoverAnimation;

	private boolean hovered;

	private boolean failed;

	private Image image;

	public NewsTile(NewsPost post, ListenableFuture<Image> image) {
		this.post = post;

		this.hoverAnimation = new TimedAnimation(this);

		this.setPreferredSize(new Dimension(320, 160));
		this.setOpaque(false);

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Desktop.getDesktop().browse(URI.create(NewsTile.this.post.getHref()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				NewsTile.this.setCursor(new Cursor(Cursor.HAND_CURSOR));
				NewsTile.this.setHoverState(true);

				NewsTile.this.hoverAnimation.run(12, true);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				NewsTile.this.setCursor(Cursor.getDefaultCursor());
				NewsTile.this.setHoverState(false);

				NewsTile.this.hoverAnimation.run(8, false);
			}
		});

		Futures.addCallback(image, new FutureCallback<Image>() {
			@Override
			public void onSuccess(@Nullable Image result) {
				if (result == null) {
					return;
				}

				NewsTile.this.image = result;
			}

			@Override
			public void onFailure(Throwable t) {
				NewsTile.this.failed = true;
			}
		});

		image.addListener(this::repaint, SwingExecutor.INSTANCE);
	}

	private void setHoverState(boolean hovered) {
		if (this.hovered == hovered) {
			return;
		}

		this.hovered = hovered;
		this.repaint();
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color color = new Color(this.post.getSection().getColor());

		// Draw background
		if (this.image != null) {
			double scale = this.hoverAnimation.getProgress() * 0.3D;

			double aspect = (double) this.getWidth() / (double) this.getHeight();

			int x = (int) (-16.0D * scale * aspect);
			int y = (int) (-16.0D * scale);

			int width = this.getWidth() + Math.abs(x * 2);
			int height = this.getHeight() + Math.abs(y * 2);

			g2.drawImage(this.image, x, y, width, height, null);
		} else {
			g2.setColor(BACKGROUND);
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());

			if (this.failed) {
				Composite prevComposite = g2.getComposite();

				AlphaComposite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
				g2.setComposite(composite);

				g2.setFont(FONT_ERROR);

				FontMetrics f = g2.getFontMetrics();

				String str = "Failed to load image";

				int width = f.stringWidth(str);

				g2.drawImage(WARNING_ICON, (this.getWidth() / 2) - 32, (this.getHeight() / 2) - 32, null);
				g2.setColor(Color.WHITE);

				g2.drawString(str, (this.getWidth() / 2) - (width / 2), (this.getHeight() / 2) + 40);

				g2.setComposite(prevComposite);
			}
		}

		// Hover highlight
		if (this.hoverAnimation.getProgress() > 0.0D) {
			g2.setColor(new Color(255, 255, 255, (int) (this.hoverAnimation.getProgress() * 50)));
			g2.fillRect(0, 0, this.getWidth(), this.getHeight());
		}

		// Card bottom leaf
		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 220));
		g2.fillRect(0, this.getHeight() - 0, this.getWidth(), 48);

		// Draw title
		g2.setFont(FONT_TITLE);
		g2.setColor(Color.WHITE);
		g2.drawString(this.post.getTitle(), 6, this.getHeight() - 26);

		// Draw date
//		g2.setFont(FONT_DATE);
//		g2.setColor(Color.LIGHT_GRAY);
//		g2.drawString(DATE_FORMAT.format(this.post.getDate()), 6, this.getHeight() - 8);

		// Draw lined border
		g2.setColor(BORDER);
		g2.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		g2.drawLine(0, this.getHeight() - 0, this.getWidth(), this.getHeight() - 48);
	}
}
