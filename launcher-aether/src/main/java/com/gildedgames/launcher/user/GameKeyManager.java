package com.gildedgames.launcher.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Getter;

public class GameKeyManager {
	private static final String VERIFY_URL = "http://launcher.triz.moe/minecraft/launcher/packages.php%s";

	private final Launcher launcher;

	public GameKeyManager(Launcher launcher) {
		this.launcher = launcher;
	}

	public ListenableFuture<KeyVerificationResponse> validate(String key) {
		if (key.length() != 36) {
			return Futures.immediateFuture(KeyVerificationResponse.tooShort());
		}

		return this.launcher.getExecutor().submit(() -> {
			String url = String.format(VERIFY_URL, key);

			return HttpRequest.get(HttpRequest.url(url))
					.execute()
					.expectResponseCode(200)
					.returnContent()
					.asJson(KeyVerificationResponse.class);
		});
	}

	public static class KeyVerificationResponse {
		@JsonProperty
		@Getter
		private boolean valid;

		@JsonProperty
		@Getter
		private boolean revoked;

		@JsonProperty
		@Getter
		private String revocationReason;

		private static KeyVerificationResponse tooShort() {
			KeyVerificationResponse response = new KeyVerificationResponse();
			response.valid = false;

			return response;
		}
	}
}
