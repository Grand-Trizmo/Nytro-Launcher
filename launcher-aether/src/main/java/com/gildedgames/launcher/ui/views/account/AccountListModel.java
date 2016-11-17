package com.gildedgames.launcher.ui.views.account;

import com.gildedgames.launcher.ui.resources.AvatarManager;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import com.skcraft.launcher.auth.Account;
import com.skcraft.launcher.auth.AccountList;

import javax.annotation.Nullable;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.Image;
import java.util.HashMap;

public class AccountListModel extends AbstractTableModel {
	private final AccountList accounts;

	private final AvatarManager avatarManager;

	private final HashMap<Account, ImageIcon> avatars = new HashMap<>();

	public AccountListModel(AccountList accounts, AvatarManager avatarManager) {
		this.accounts = accounts;
		this.avatarManager = avatarManager;

		for (int i = 0; i < this.accounts.getSize(); i++) {
			Account account = this.accounts.getElementAt(i);

			final int row = i;

			Futures.addCallback(avatarManager.getAvatar(account), new FutureCallback<Image>() {
				@Override
				public void onSuccess(@Nullable Image result) {
					if (result == null) {
						return;
					}

					AccountListModel.this.avatars.put(account, new ImageIcon(result));

					SwingUtilities.invokeLater(() -> {
						AccountListModel.this.fireTableRowsUpdated(row, row);
					});
				}

				@Override
				public void onFailure(Throwable t) {

				}
			}, MoreExecutors.sameThreadExecutor());
		}
	}

	@Override
	public int getRowCount() {
		return this.accounts.getSize();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex == 0) {
			return "Skin";
		} else if (columnIndex == 1) {
			return "Username";
		}

		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return ImageIcon.class;
		} else if (columnIndex == 1) {
			return String.class;
		}

		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Account account = this.accounts.getElementAt(rowIndex);

		if (account == null) {
			return null;
		}

		if (columnIndex == 0) {
			ImageIcon avatar = this.avatars.get(account);

			if (avatar == null) {
				return AvatarManager.DEFAULT_HEAD;
			} else {
				return avatar;
			}
		} else if (columnIndex == 1) {
			return account.getSession() == null ? account.getMojangId() : account.getSession().getName();
		}

		return null;
	}
}
