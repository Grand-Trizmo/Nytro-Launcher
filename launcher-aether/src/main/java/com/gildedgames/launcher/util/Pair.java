package com.gildedgames.launcher.util;

import lombok.Getter;

public class Pair<L, R> {
	@Getter
	private final L left;

	@Getter
	private final R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
}
