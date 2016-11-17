package com.gildedgames.launcher.ui.animations;

import lombok.Getter;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.TimerTask;

public class LoopingAnimation extends Animation {
	private JComponent component;

	private TimerTask task;

	@Getter
	private double progress;

	private double progressPerTick;

	public LoopingAnimation(JComponent comp) {
		this.component = comp;
	}

	public void run(int ticks) {
		this.progressPerTick = 1.0D / ticks;

		this.startTimer();
	}

	public void stop() {
		this.task.cancel();

		SwingUtilities.invokeLater(() -> this.component.repaint());
	}

	private void startTimer() {
		if (this.task != null) {
			this.stop();
		}

		this.task = new TimerTask() {
			@Override
			public void run() {
				LoopingAnimation.this.progress += LoopingAnimation.this.progressPerTick;

				double progress = LoopingAnimation.this.progress;

				if (progress >= 1.0D) {
					LoopingAnimation.this.progress = 0.0D;
				}

				LoopingAnimation.this.component.repaint();

				if (!LoopingAnimation.this.component.isVisible()) {
					LoopingAnimation.this.stop();
				}
			}
		};

		this.queue(this.task, 10);
	}
}
