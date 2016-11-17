package com.gildedgames.launcher.util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

public class BrowserUtil {
	public static void openPage(String url) {
		try {
			Desktop.getDesktop().browse(URI.create(url));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
