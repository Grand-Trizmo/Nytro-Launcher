package com.gildedgames.launcher.ui.animations;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Animation {
	private static final Timer TIMER = new Timer();

	private static final List<TimerTask> tasks = new ArrayList<>();

	public void queue(TimerTask task, int ticks) {
		TIMER.scheduleAtFixedRate(task, 0, ticks);
		TIMER.purge();

		tasks.add(task);
	}

	public static void stopAll() {
		for (TimerTask task : tasks) {
			task.cancel();
		}

		tasks.clear();
	}
}
