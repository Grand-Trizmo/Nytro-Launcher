package com.gildedgames.launcher.ui.components;

import com.gildedgames.launcher.ui.resources.AvatarManager;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.launcher.auth.Account;
import com.skcraft.launcher.util.SwingExecutor;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import java.awt.*;

public class UserIndicator extends JComponent {
	private static final Font FONT_HEADER = LauncherFonts.OPEN_SANS_REGULAR.deriveFont(10.0f),
			FONT_USERNAME = LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f);

	private final AvatarManager avatarManager;

	private Account account;

	private Image avatar;

	public UserIndicator(AvatarManager avatarManager) {
		this.avatarManager = avatarManager;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(120, 50);
	}

	@Override
	public Dimension getMinimumSize() {
		return this.getPreferredSize();
	}

	public void setAccount(Account account) {
		this.account = account;

		if (account == null) {
			this.avatar = null;
		} else {
			ListenableFuture<Image> image = this.avatarManager.getAvatar(account);

			Futures.addCallback(image, new FutureCallback<Image>() {
				@Override
				public void onSuccess(@Nullable Image result) {
					UserIndicator.this.avatar = result;

					UserIndicator.this.repaint();
				}

				@Override
				public void onFailure(Throwable t) {

				}
			}, SwingExecutor.INSTANCE);
		}
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Image avatar = AvatarManager.DEFAULT_HEAD.getImage();

		if (this.avatar != null) {
			avatar = this.avatar;
		}

		g.drawImage(avatar, 7, 10, null);

		g2.setFont(FONT_HEADER);
		g2.setColor(Color.WHITE);
		g2.drawString("LOGGED IN AS", 56, 23);

		String username = this.account == null ? "nobody" : this.account.toString();
		g2.setFont(FONT_USERNAME);
		g2.drawString(username, 56, 40);
	}
}
