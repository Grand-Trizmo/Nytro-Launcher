package com.gildedgames.launcher.ui.resources;


import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Account;
import com.skcraft.launcher.persistence.Persistence;
import lombok.extern.java.Log;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log
public class AvatarManager extends ImageCache {
	public static final ImageIcon DEFAULT_HEAD = LauncherIcons.loadIcon("com/gildedgames/assets/images/heads/default.png");

	private static final String SERVICE_URL = "https://crafatar.com/renders/head/%s?scale=2";

	private static final int CACHE_TIME = 1000 * 60 * 30;

	private Path cachePath;

	public ListenableFuture<Image> getAvatar(Account account) {
		if (account.getSession() == null) {
			return Futures.immediateFuture(DEFAULT_HEAD.getImage());
		}

		String uuid = account.getSession().getUuid();

		String remote = String.format(SERVICE_URL, uuid);

		return this.obtain(uuid + ".png", remote, false);
	}

	public static AvatarManager load(Launcher launcher) {
		AvatarManager manager = Persistence.load(new File(launcher.getCacheDir(), "avatar_cache.json"), AvatarManager.class);
		manager.cachePath = Paths.get(launcher.getCacheDir().getAbsolutePath(), "avatars/heads");

		return manager;
	}

	public void clear() {
		this.clearImageCache();
	}

	@Override
	protected long getMaxCacheTime() {
		return CACHE_TIME;
	}

	@Override
	protected Path getImageCacheFolder() {
		return this.cachePath;
	}
}
