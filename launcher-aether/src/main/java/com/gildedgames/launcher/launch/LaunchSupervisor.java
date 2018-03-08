package com.gildedgames.launcher.launch;

import com.gildedgames.launcher.util.IProgressReporter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Instance;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.launch.LaunchListener;
import com.skcraft.launcher.launch.Runner;
import com.skcraft.launcher.swing.SwingHelper;
import lombok.Getter;
import lombok.extern.java.Log;
import org.apache.commons.io.FileUtils;

import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

@Log
public class LaunchSupervisor {
	private final Launcher launcher;

	@Getter
	private boolean isBusy;

	public LaunchSupervisor(Launcher launcher) {
		this.launcher = launcher;
	}

	public ObservableFuture<Process> launch(IProgressReporter reporter, Instance instance, Session session, LaunchListener listener, LaunchProcessHandler handler) {
		this.isBusy = true;

		final File extractDir = this.launcher.createExtractDir();

		// Get the process
		Runner task = new Runner(this.launcher, instance, session, extractDir);

		ObservableFuture<Process> processFuture = new ObservableFuture<>(this.launcher.getExecutor().submit(task), task);

		// Show process for the process retrieval
		reporter.beginReporting(processFuture, processFuture, "Launching");

		// If the process is started, get rid of this window
		Futures.addCallback(processFuture, new FutureCallback<Process>() {
			@Override
			public void onSuccess(Process result) {
				SwingUtilities.invokeLater(listener::gameStarted);
			}

			@Override
			public void onFailure(Throwable t) {
			}
		});

		// Watch the created process
		ListenableFuture<?> future = Futures.transform(processFuture, handler, this.launcher.getExecutor());

		SwingHelper.addErrorDialogCallback(null, future);

		// Clean up at the very end
		future.addListener(() -> {
			try {
				log.info("Process ended; cleaning up " + extractDir.getAbsolutePath());

				FileUtils.deleteDirectory(extractDir);
			} catch (IOException e) {
				log.log(Level.WARNING, "Failed to clean up " + extractDir.getAbsolutePath(), e);
			}

			this.isBusy = false;

			SwingUtilities.invokeLater(listener::gameClosed);
		}, MoreExecutors.sameThreadExecutor());

		return processFuture;
	}
}
