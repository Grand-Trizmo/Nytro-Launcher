package com.gildedgames.launcher.ui.resources;

import com.gildedgames.launcher.ui.LauncherFrame;
import lombok.extern.java.Log;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

@Log
public class LauncherIcons {
	public static final ImageIcon GEAR;

	public static final ImageIcon ADD, REMOVE;

	public static final ImageIcon REFRESH;

	public static final ImageIcon SWITCH_USER;

	public static final ImageIcon BUG, WARN;

	public static final ImageIcon PATREON, GITLAB, WEB, TRIZMO, GRIFFIN, KKMC, PLAYERCOUNT;

	public static final ImageIcon WINDOW_MINIMIZE, WINDOW_CLOSE, WINDOW_MAXIMIZE;

	public static final ImageIcon WINDOW_ICON;

	public static final ImageIcon INPUT_VALID, INPUT_INVALID;


    static {
		GEAR = loadIcon("com/gildedgames/assets/icons/16/gear.png");
		ADD = loadIcon("com/gildedgames/assets/icons/16/add.png");
		REMOVE = loadIcon("com/gildedgames/assets/icons/16/remove.png");
		REFRESH = loadIcon("com/gildedgames/assets/icons/16/refresh.png");

		SWITCH_USER = loadIcon("com/gildedgames/assets/icons/16/switch_user.png");
		BUG = loadIcon("com/gildedgames/assets/icons/16/bug.png");
		WARN = loadIcon("com/gildedgames/assets/icons/16/warn.png");

		PLAYERCOUNT = loadIcon("com/gildedgames/assets/icons/16/playercount.png");
		PATREON = loadIcon("com/gildedgames/assets/icons/16/patreon.png");
		GITLAB = loadIcon("com/gildedgames/assets/icons/16/gitlab.png");
		TRIZMO = loadIcon("com/gildedgames/assets/icons/48/trizmo.png");
        GRIFFIN = loadIcon("com/gildedgames/assets/icons/48/griffin.png");
		WEB = loadIcon("com/gildedgames/assets/icons/16/web.png");
		KKMC = loadIcon("com/gildedgames/assets/icons/48/kkmc.png");

		WINDOW_MINIMIZE = loadIcon("com/gildedgames/assets/icons/16/minimize.png");
		WINDOW_CLOSE = loadIcon("com/gildedgames/assets/icons/16/close.png");
		WINDOW_MAXIMIZE = loadIcon("com/gildedgames/assets/icons/16/maximize.png");

		WINDOW_ICON = loadIcon("com/gildedgames/assets/titlebar/window-icon.png");

		INPUT_VALID = loadIcon("com/gildedgames/assets/icons/16/valid.png");
		INPUT_INVALID = loadIcon("com/gildedgames/assets/icons/16/invalid.png");

	}

	public static ImageIcon loadIcon(String path) {
		Image image = load(path);

		if (image == null) {
			return null;
		}

		return new ImageIcon(image);
	}


	public static Image load(String path) {
		try {
			InputStream stream = LauncherFrame.class.getClassLoader().getResourceAsStream(path);

			if (stream == null) {
				throw new IOException("Couldn't open stream");
			}

			return ImageIO.read(stream);
		} catch (IOException e) {
			e.printStackTrace();

			log.severe("Couldn't load image " + path);
		}

		return null;
	}
}
