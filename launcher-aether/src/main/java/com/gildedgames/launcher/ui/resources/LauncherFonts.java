package com.gildedgames.launcher.ui.resources;

import lombok.extern.java.Log;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

@Log
public class LauncherFonts {
	public static final Font OPEN_SANS_REGULAR;

	public static final Font OPEN_SANS_BOLD;

	public static final Font OSWALD_NORMAL;

	static {
		OPEN_SANS_BOLD = load("com/gildedgames/assets/fonts/OpenSans-Bold.ttf");
		OPEN_SANS_REGULAR = load("com/gildedgames/assets/fonts/OpenSans-Regular.ttf");
		OSWALD_NORMAL = load("com/gildedgames/assets/fonts/Oswald-Regular.ttf");
	}

	private static Font load(String path) {
		try {
			InputStream stream = LauncherFonts.class.getClassLoader().getResourceAsStream(path);

			return Font.createFont(Font.TRUETYPE_FONT, stream);
		} catch (IOException | FontFormatException e) {
			log.severe("Couldn't read font " + path);

			e.printStackTrace();
		}

		return Font.getFont(Font.SANS_SERIF);
	}
}
