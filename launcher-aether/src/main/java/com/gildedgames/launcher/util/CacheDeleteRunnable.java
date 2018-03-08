package com.gildedgames.launcher.util;

import com.skcraft.concurrency.ProgressObservable;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class CacheDeleteRunnable implements Callable<Void>, ProgressObservable {
	private final Path folder;

	private String currentFile;

	public CacheDeleteRunnable(Path folder) {
		this.folder = folder;
	}

	@Override
	public Void call() throws Exception {
		this.deleteFolder(this.folder);

		return null;
	}

	private void deleteFolder(Path directory) throws IOException {
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					this.deleteFolder(entry);
				} else {
					this.currentFile = entry.toString();

					Files.delete(entry);
				}
			}
		}
	}

	@Override
	public double getProgress() {
		return -1;
	}

	@Override
	public String getStatus() {
		return "Deleting " + this.currentFile;
	}
}
