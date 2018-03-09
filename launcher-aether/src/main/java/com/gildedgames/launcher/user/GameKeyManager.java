package com.gildedgames.launcher.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Getter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class GameKeyManager {
	private static final String VERIFY_URL = "https://launcher.triz.moe/minecraft/launcher/_upload/packages.php?key=%s";

	private final Launcher launcher;

	public GameKeyManager(Launcher launcher) {
		this.launcher = launcher;
	}

	public ListenableFuture<KeyVerificationResponse> validate(String key) {

		return this.launcher.getExecutor().submit(() -> {
			String url = String.format(VERIFY_URL, key);

			return HttpRequest.get(HttpRequest.url(url))
					.execute()
					.expectResponseCode(200, 301)
					.returnContent()
					.asJson(KeyVerificationResponse.class);
		});
	}

	public static class KeyVerificationResponse {
		@JsonProperty
		@Getter
		private int minimumVersion;

		@JsonProperty
		@Getter
		private List<Packages> packages;

		private static class Packages {
			@JsonProperty
			@Getter
			private String name;

			@JsonProperty
			@Getter
			private String title;

			@JsonProperty
			@Getter
			private String version;

			@JsonProperty
			@Getter
			private int priority;

			@JsonProperty
			@Getter
			private String location;
		}

		public boolean packagesContainsKey(String key) {
			AtomicBoolean yes = new AtomicBoolean(false);
			packages.forEach(packages1 -> {
				if (packages1.name.equals(key)) yes.set(true);
			});
			return yes.get();
		}
	}
}
