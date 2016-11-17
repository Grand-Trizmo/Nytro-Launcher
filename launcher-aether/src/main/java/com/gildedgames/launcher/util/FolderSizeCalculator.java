package com.gildedgames.launcher.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class FolderSizeCalculator implements Callable<Long> {
	private final Path[] folders;

	public FolderSizeCalculator(Path... folders) {
		this.folders = folders;
	}

	@Override
	public Long call() throws Exception {
		long size = 0L;

		for (Path path : this.folders) {
			size += this.sizeFolder(path);
		}

		return size;
	}

	private long sizeFolder(Path directory) throws IOException {
		long size = 0L;

		try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
			for (Path entry : stream) {
				if (Files.isDirectory(entry)) {
					size += this.sizeFolder(entry);
				} else {
					size += Files.size(entry);
				}
			}
		}

		return size;
	}
}
