package com.gildedgames.launcher.ui.views.account;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.google.common.util.concurrent.FutureCallback;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.Account;
import com.skcraft.launcher.auth.Session;
import com.skcraft.launcher.auth.StoredSession;

import java.util.Date;

public class AccountRefreshView extends AccountAddView {
	private final Account account;

	public AccountRefreshView(LauncherFrame frame, Launcher launcher, Account account) {
		super(frame, launcher);

		this.account = account;

		this.usernameField.setText(account.getMojangId());
		this.usernameField.setEditable(false);
	}

	protected FutureCallback<Session> getSessionCallback() {
		return new FutureCallback<Session>() {
			@Override
			public void onSuccess(Session result) {
				Account account = AccountRefreshView.this.account;
				account.setLastUsed(new Date());
				account.setSession((StoredSession) result);

				AccountRefreshView.this.returnResult(account);
			}

			@Override
			public void onFailure(Throwable t) {

			}
		};
	}

	@Override
	protected String getTitle() {
		return "Welcome back";
	}

	@Override
	protected String getHeader() {
		return "We ran into an issue while logging you in. Please enter your details again.";
	}
}