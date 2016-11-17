package com.gildedgames.launcher.launch;


import com.gildedgames.launcher.ui.panels.MessageLog;
import com.gildedgames.launcher.ui.views.game.ConsolePanel;
import com.gildedgames.launcher.ui.views.game.PlayView;
import com.google.common.base.Function;
import lombok.NonNull;
import lombok.extern.java.Log;

import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

@Log
public class LaunchProcessHandler implements Function<Process, ConsolePanel> {
	private final PlayView view;

	private ConsolePanel console;

	public LaunchProcessHandler(@NonNull PlayView view) {
		this.view = view;
	}

	@Override
	public ConsolePanel apply(final Process process) {
		log.info("Watching process " + process);

		try {
			SwingUtilities.invokeAndWait(() -> {
				this.console = this.view.getConsolePanel();
				this.console.setProcess(process);
				this.console.setVisible(true);

				MessageLog messageLog = this.console.getLog();
				messageLog.consume(process.getInputStream());
				messageLog.consume(process.getErrorStream());
			});

			// Wait for the process to end
			process.waitFor();
		} catch (InterruptedException e) {
			// Orphan process
		} catch (InvocationTargetException e) {
			log.log(Level.WARNING, "Unexpected failure", e);
		}

		log.info("Process ended, re-showing launcher...");

		// Restore the launcher
		SwingUtilities.invokeLater(() -> {
			if (this.console != null) {
				this.console.setProcess(null);
				this.console.requestFocus();
			}
		});

		return this.console;
	}

}
