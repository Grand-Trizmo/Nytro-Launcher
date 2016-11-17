package com.gildedgames.launcher.util;

import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ProgressObservable;

public interface IProgressReporter {
	void beginReporting(ListenableFuture<?> future, ProgressObservable observable, String title);
}
