package com.gildedgames.launcher.ui.panels;

import com.gildedgames.launcher.ui.components.FlatProgressbar;
import com.gildedgames.launcher.ui.components.FlatSpinner;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherStyles;
import com.gildedgames.launcher.util.IProgressReporter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.util.SwingExecutor;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressIndicatorPanel extends JPanel implements IProgressReporter {
	private ListenableFuture<?> future;

	private ProgressObservable observable;

	private Timer updateTimer;

	private String title;

	private JLabel titleLabel, statusLabel;

	private FlatProgressbar progressbar;

	public ProgressIndicatorPanel() {
		this.setLayout(new MigLayout("fill, insets 0", "12[fill]12", "12[]6[]6[]4"));

		this.init();

		this.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);

		this.setVisible(false);
	}

	@Override
	public void beginReporting(ListenableFuture<?> future, ProgressObservable observable, String title) {
		if (future.isDone()) {
			return;
		}

		this.future = future;
		this.observable = observable;
		this.title = title;

		Futures.addCallback(this.future, new FutureCallback<Object>() {
			@Override
			public void onSuccess(@Nullable Object result) {
				this.end();
			}

			@Override
			public void onFailure(Throwable t) {
				this.end();
			}

			private void end() {
				if (ProgressIndicatorPanel.this.future.isDone()) {
					if (ProgressIndicatorPanel.this.updateTimer != null) {
						ProgressIndicatorPanel.this.updateTimer.cancel();
						ProgressIndicatorPanel.this.updateTimer = null;
					}

					ProgressIndicatorPanel.this.setVisible(false);
				}
			}
		}, SwingExecutor.INSTANCE);

		SwingUtilities.invokeLater(() -> {
			if (ProgressIndicatorPanel.this.future.isDone()) {
				return;
			}

			this.updateTimer = new Timer();
			this.updateTimer.scheduleAtFixedRate(new ProgressIndicatorPanel.UpdateProgressTask(this), 10, 30);

			this.setVisible(true);

			this.titleLabel.setText(title);
			this.progressbar.setValue(0);
			this.statusLabel.setText(this.observable.getStatus());

			this.revalidate();
			this.repaint();
		});
	}

	private void init() {
		JPanel header = new JPanel(new BorderLayout());
		header.setOpaque(false);

		FlatSpinner spinner = new FlatSpinner(16);
		spinner.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

		header.add(spinner, BorderLayout.WEST);

		this.titleLabel = new JLabel();
		this.titleLabel.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f));
		this.titleLabel.setForeground(Color.WHITE);

		header.add(this.titleLabel, BorderLayout.CENTER);

		this.add(header, "wrap");

		this.progressbar = new FlatProgressbar();
		this.progressbar.setPreferredSize(new Dimension(500, 5));
		this.progressbar.setMinimum(0);
		this.progressbar.setMaximum(100);
		this.progressbar.setValue(0);

		this.add(this.progressbar, "height 5, wrap");

		this.statusLabel = new JLabel();
		this.statusLabel.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.statusLabel.setForeground(new Color(0x666666));

		this.add(this.statusLabel, "wrap");

//		FlatButton cancelButton = new FlatButton("Cancel", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
//		cancelButton.setStyle(FlatButton.ButtonStyle.LIGHT);
//		cancelButton.setPreferredSize(new Dimension(100, 32));
//		cancelButton.addActionListener(e -> {
//			boolean cancel = SwingHelper.confirmDialog(this.frame, "Are you sure you want to cancel this task?", "Confirmation");
//
//			if (!cancel) {
//				return;
//			}
//
//			this.future.cancel(true);
//		});
//
//		this.add(cancelButton);
	}

	private class UpdateProgressTask extends TimerTask {
		private final ProgressIndicatorPanel view;

		public UpdateProgressTask(ProgressIndicatorPanel view) {
			this.view = view;
		}

		@Override
		public void run() {
			double progress = this.view.observable.getProgress();

			int value = (int) Math.floor(Math.max(0, progress * this.view.progressbar.getMaximum()));

			String status = this.view.observable.getStatus();

			int newline = status.indexOf("\n");

			String concat = newline <= 0 ? status : status.substring(0, newline);

			SwingUtilities.invokeLater(() -> {
				this.view.progressbar.setValue(value);
				this.view.progressbar.revalidate();
				this.view.progressbar.repaint();

				this.view.statusLabel.setText(concat);
				this.view.statusLabel.revalidate();
				this.view.statusLabel.repaint();
			});
		}
	}
}
