package com.gildedgames.launcher.ui.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gildedgames.launcher.util.Pair;
import com.google.common.util.concurrent.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

@Log
public abstract class ImageCache {
	private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(3));

	@JsonProperty
	private Map<String, CachedEntry> cachedImages = new HashMap<>();

	protected ListenableFuture<Image> obtain(String resource, String remote, boolean forceRefresh) {
		Path fsPath = Paths.get(this.getImageCacheFolder().toString(), resource);

		CachedEntry entry;

		boolean refresh;

		if (this.cachedImages.containsKey(resource)) {
			entry = this.cachedImages.get(resource);

			if (entry.getLoadedImage() != null) {
				return Futures.immediateFuture(entry.getLoadedImage());
			}

			refresh = forceRefresh || entry.getLastRefreshed() + this.getMaxCacheTime() < System.currentTimeMillis();
		} else {
			entry = null;

			refresh = true;
		}

		ListenableFuture<Image> future = this.executor.submit(() -> {
			Image image = null;

			if (!refresh) {
				image = this.load(fsPath);
			}

			if (image == null) {
				Pair<Image, byte[]> result = this.download(remote);
				image = result.getLeft();

				this.save(fsPath, result.getRight());
			}

			return image;
		});

		Futures.addCallback(future, new FutureCallback<Image>() {
			@Override
			public void onSuccess(@Nullable Image result) {
				CachedEntry entry2 = entry;

				long now = System.currentTimeMillis();

				if (entry2 == null) {
					entry2 = new CachedEntry();
					entry2.lastAccessed = now;
					entry2.lastRefreshed = now;

					ImageCache.this.cachedImages.put(resource, entry2);
				} else {
					entry2.lastAccessed = now;

					if (refresh) {
						entry2.lastRefreshed = now;
					}
				}

				entry2.setLoadedImage(result);

				Persistence.commitAndForget(ImageCache.this);
			}

			@Override
			public void onFailure(Throwable t) {

			}
		}, MoreExecutors.sameThreadExecutor());

		return future;
	}

	private Pair<Image, byte[]> download(String remote) throws IOException, InterruptedException {
		log.info("Retrieving asset from " + remote);

		byte[] data = HttpRequest.get(HttpRequest.url(remote))
				.execute()
				.expectResponseCode(200)
				.returnContent()
				.asBytes();

		try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
			Image image = ImageIO.read(in);

			return new Pair<>(image, data);
		}
	}

	private Image load(Path local) throws IOException {
		if (!Files.exists(local)) {
			return null;
		}

		try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(local.toFile()))) {
			return ImageIO.read(input);
		}
	}

	private void save(Path local, byte[] data) throws IOException {
		Files.createDirectories(local.getParent());

		ByteArrayInputStream in = new ByteArrayInputStream(data);

		try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(local.toFile()))) {
			IOUtils.copy(in, out);
		}
	}

	protected void clearImageCache() {
		this.cachedImages.clear();
	}

	protected abstract long getMaxCacheTime();

	protected abstract Path getImageCacheFolder();

	public static class CachedEntry {
		@JsonProperty
		@Getter
		private long lastAccessed;

		@JsonProperty
		@Getter
		private long lastRefreshed;

		@JsonIgnore
		@Getter
		@Setter
		private Image loadedImage;
	}
}
