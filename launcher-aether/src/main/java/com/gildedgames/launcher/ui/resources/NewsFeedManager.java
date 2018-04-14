package com.gildedgames.launcher.ui.resources;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.util.concurrent.*;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Getter;
import lombok.extern.java.Log;

import javax.annotation.Nullable;
import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

@Log
public class NewsFeedManager extends ImageCache {
	private static final String METADATA_URL = "https://launcher.triz.moe/minecraft/launcher/News/latest.json",
			IMAGE_PROVIDER_URL = "https://launcher.triz.moe/minecraft/launcher/News/Images/";

	private static final int CACHE_TIME = 1000 * 60 * 20;

	private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

	@JsonProperty
	@Getter
	private NewsFeed feed = new NewsFeed();

	private Path imageCacheDir;

	public NewsFeedManager() {

	}

	public static NewsFeedManager load(Launcher launcher) {
		NewsFeedManager manager = Persistence.load(new File(launcher.getCacheDir(), "news_feed.json"), NewsFeedManager.class);
		manager.imageCacheDir = Paths.get(launcher.getCacheDir().getAbsolutePath(), "images/news");

		return manager;
	}

	public ListenableFuture<NewsFeed> refresh(boolean force) {
		if (!force && this.feed != null && this.feed.expires > System.currentTimeMillis()) {
			return Futures.immediateFuture(this.feed);
		}

		ListenableFuture<NewsFeed> future = this.executor.submit(() -> {
			log.info("Fetching latest news feed from " + METADATA_URL);

			NewsFeed feed = HttpRequest.get(HttpRequest.url(METADATA_URL))
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asJson(NewsFeed.class);

			feed.expires = System.currentTimeMillis() + CACHE_TIME;

			return feed;
		});

		Futures.addCallback(future, new FutureCallback<NewsFeed>() {
			@Override
			public void onSuccess(@Nullable NewsFeed result) {
				NewsFeedManager.this.feed = result;
				NewsFeedManager.this.save();
			}

			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
			}
		});

		return future;
	}

	public ListenableFuture<Image> getImage(String resource) {
		String remote = IMAGE_PROVIDER_URL + resource;

		return this.obtain(resource, remote, false);
	}

	private void save() {
		Persistence.commitAndForget(this);
	}

	public void clear() {
		this.feed = null;

		this.clearImageCache();
	}

	@Override
	protected long getMaxCacheTime() {
		return CACHE_TIME;
	}

	@Override
	protected Path getImageCacheFolder() {
		return this.imageCacheDir;
	}

	public static class NewsFeed {
		@JsonProperty
		@Getter
		private List<NewsSection> sections;

		@JsonProperty
		private long expires;
	}

	public static class NewsSection {
		@JsonProperty
		@Getter
		private String title;

		@JsonProperty
		@Getter
		private int color;

		@JsonProperty
		@JsonManagedReference
		@Getter
		private List<NewsPost> posts;
	}

	public static class NewsPost {
		@JsonProperty
		@Getter
		private String title;

		@JsonProperty
		@Getter
		private Date date;

		@JsonProperty
		@Getter
		private Map<String, String> images;

		@JsonProperty
		@Getter
		private String href;

		@JsonBackReference
		@Getter
		private NewsSection section;
	}
}
