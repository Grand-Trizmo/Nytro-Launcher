package com.gildedgames.launcher.ui.views;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.components.FlatProgressbar;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherStyles;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SwingExecutor;
import net.miginfocom.swing.MigLayout;

import javax.annotation.Nullable;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

public class ProgressView extends JPanel {
	private final LauncherFrame frame;

	private final ListenableFuture<?> future;

	private final ProgressObservable observable;

	private final Timer updateTimer;

	private final String title;

	private final boolean canCancel;

	private JLabel statusLabel;

	private FlatProgressbar progressbar;

	public ProgressView(LauncherFrame frame, ListenableFuture<?> future, ProgressObservable observable, String title, boolean canCancel) {
		this.frame = frame;

		this.future = future;
		this.observable = observable;
		this.canCancel = canCancel;

		this.title = title;

		this.setLayout(new MigLayout("fill, insets 0", "push[]push", "push[]6[]6[]20[]push"));

		this.init();

		this.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);

		this.updateTimer = new Timer();
		this.updateTimer.scheduleAtFixedRate(new UpdateProgressTask(this), 10, 250);

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
				ProgressView.this.updateTimer.cancel();
				ProgressView.this.frame.getLauncherLayout().back();
			}
		}, SwingExecutor.INSTANCE);
	}

	private void init() {
		JLabel titleLabel = new JLabel(this.title);
		titleLabel.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f));
		titleLabel.setForeground(Color.WHITE);

		this.add(titleLabel, "wrap");

		this.progressbar = new FlatProgressbar();
		this.progressbar.setPreferredSize(new Dimension(500, 5));
		this.progressbar.setMinimum(0);
		this.progressbar.setMaximum(100);
		this.progressbar.setValue(0);

		this.add(this.progressbar, "height 5, wrap");

		this.statusLabel = new JLabel(this.observable.getStatus());
		this.statusLabel.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		this.statusLabel.setForeground(new Color(0x666666));

		this.add(this.statusLabel, "wrap");

		FlatButton cancelButton = new FlatButton("Cancel", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		cancelButton.setStyle(FlatButton.ButtonStyle.LIGHT);
		cancelButton.setPreferredSize(new Dimension(100, 32));
		cancelButton.addActionListener(e -> {
			boolean cancel = SwingHelper.confirmDialog(this.frame, "Are you sure you want to cancel this task?", "Confirmation");

			if (!cancel) {
				return;
			}

			this.future.cancel(true);
		});
		cancelButton.setVisible(this.canCancel);

		this.add(cancelButton);
	}

	private class UpdateProgressTask extends TimerTask {
		private final ProgressView view;

		public UpdateProgressTask(ProgressView view) {
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
