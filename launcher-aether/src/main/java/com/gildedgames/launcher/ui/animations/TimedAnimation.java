package com.gildedgames.launcher.ui.animations;

import lombok.Getter;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.TimerTask;

public class TimedAnimation extends Animation {
	private JComponent component;

	private TimerTask task;

	@Getter
	private double progress;

	private double progressPerTick;

	public TimedAnimation(JComponent comp) {
		this.component = comp;
	}

	public void run(int ticks, boolean forwards) {
		double perTick = 1.0D / ticks;
		perTick *= (forwards ? 1 : -1);

		this.progressPerTick = perTick;

		this.startTimer();
	}

	private void startTimer() {
		if (this.task != null) {
			this.task.cancel();
		}

		this.task = new TimerTask() {
			@Override
			public void run() {
				TimedAnimation.this.progress += TimedAnimation.this.progressPerTick;

				double progress = TimedAnimation.this.progress;

				if (progress <= 0.0D) {
					TimedAnimation.this.progress = 0.0D;
					this.cancel();
				} else if (progress >= 1.0D) {
					TimedAnimation.this.progress = 1.0D;
					this.cancel();
				}

				SwingUtilities.invokeLater(() -> TimedAnimation.this.component.repaint());
			}
		};

		this.queue(this.task, 10);
	}
}
