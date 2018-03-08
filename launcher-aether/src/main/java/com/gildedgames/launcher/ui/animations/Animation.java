package com.gildedgames.launcher.ui.animations;
import lombok.Getter;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.util.Timer;
import java.util.TimerTask;
public class Animation {
	private static final Timer timer = new Timer();
	private JComponent component;
	private TimerTask task;
	@Getter
	private double progress;
	private double progressPerTick;
	public Animation(JComponent comp) {
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
				Animation.this.progress += Animation.this.progressPerTick;
				double progress = Animation.this.progress;
				if (progress <= 0.0D) {
					Animation.this.progress = 0.0D;
					this.cancel();
				} else if (progress >= 1.0D) {
					Animation.this.progress = 1.0D;
					this.cancel();
				}
				SwingUtilities.invokeLater(() -> Animation.this.component.repaint());
			}
		};
		timer.scheduleAtFixedRate(task, 0, 10);
	}
}