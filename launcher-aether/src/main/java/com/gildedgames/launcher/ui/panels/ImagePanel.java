package com.gildedgames.launcher.ui.panels;

import com.gildedgames.launcher.ui.animations.TimedAnimation;
import com.google.common.util.concurrent.*;
import com.skcraft.launcher.util.SwingExecutor;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.Executors;

public class ImagePanel extends JPanel {
	private static final ListeningExecutorService service = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

	private Image image;

	private TimedAnimation fadeIn;

	public ImagePanel(String path) {
		this.tryLoadImage(path);

		this.fadeIn = new TimedAnimation(this);
	}

	private void onImageLoaded() {
		this.fadeIn.run(80, true);
	}

	private void tryLoadImage(String path) {
		ListenableFuture<Image> future = service.submit(() -> {
			BufferedImage image = null;

			try {
				image = ImageIO.read(ImagePanel.class.getClassLoader().getResourceAsStream(path));
			} catch (IOException e) {
				e.printStackTrace();
			}

			return image;
		});

		Futures.addCallback(future, new FutureCallback<Image>() {
			@Override
			public void onSuccess(@Nullable Image result) {
				ImagePanel.this.image = result;
				ImagePanel.this.onImageLoaded();
			}

			@Override
			public void onFailure(Throwable t) {

			}
		}, SwingExecutor.INSTANCE);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (this.image == null) {
			g.setColor(new Color(47, 53, 59));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			return;
		}

		Graphics2D g2 = (Graphics2D) g;

		Dimension imgBounds = new Dimension(this.image.getWidth(null), this.image.getHeight(null));

		Dimension panelBounds = new Dimension(this.getWidth(), this.getHeight());

		double scale = ((1.0D - this.fadeIn.getProgress()) * 4.0D);

		double aspect = (double) this.getWidth() / (double) this.getHeight();

		int x = (int) (-16.0D * scale * aspect);
		int y = (int) (-16.0D * scale);

		Dimension renderBounds = this.getScaledDimension(imgBounds, panelBounds);

		int width = renderBounds.width + Math.abs(x * 2);
		int height = renderBounds.height + Math.abs(y * 2);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(this.image, x, y, width, height, null);

		g2.setColor(new Color(47, 53, 59, (int) Math.floor((1.0D - this.fadeIn.getProgress()) * 255.0D)));
		g2.fillRect(0, 0, this.getWidth(), this.getHeight());
	}

	private Dimension getScaledDimension(Dimension image, Dimension target) {
		int oWidth = image.width;
		int oHeight = image.height;
		int bWidth = target.width;
		int bHeight = target.height;
		int nWidth = oWidth;
		int nHeight = oHeight;

		if (oWidth < bWidth) {
			nWidth = bWidth;
			nHeight = (nWidth * oHeight) / oWidth;
		}

		if (nHeight < bHeight) {
			nHeight = bHeight;
			nWidth = (nHeight * oWidth) / oHeight;
		}

		return new Dimension(nWidth, nHeight);
	}
}
