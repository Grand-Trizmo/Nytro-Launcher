/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.skcraft.launcher.dialog;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.concurrency.ProgressObservable;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.FormPanel;
import com.skcraft.launcher.swing.LinedBoxPanel;
import com.skcraft.launcher.swing.PopupMouseAdapter;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * The login dialog.
 */
public class SelectAccountDialog extends JDialog {

    private final Launcher launcher;
    @Getter private final AccountList accounts;
    @Getter private Session session;

    private final JComboBox idCombo = new JComboBox();
    private final JButton loginButton = new JButton(SharedLocale.tr("login.login"));
    private final JButton offlineButton = new JButton(SharedLocale.tr("login.playOffline"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
    private final FormPanel formPanel = new FormPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);

    /**
     * Create a new login dialog.
     *
     * @param owner the owner
     * @param launcher the launcher
     */
    public SelectAccountDialog(Window owner, @NonNull Launcher launcher) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.launcher = launcher;
        this.accounts = launcher.getAccounts();

        this.setTitle(SharedLocale.tr("login.title"));
        this.initComponents();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setMinimumSize(new Dimension(420, 0));
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(owner);

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                SelectAccountDialog.this.removeListeners();
                SelectAccountDialog.this.dispose();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void removeListeners() {
        this.idCombo.setModel(new DefaultComboBoxModel());
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        this.idCombo.setModel(this.getAccounts());

        this.loginButton.setFont(this.loginButton.getFont().deriveFont(Font.BOLD));

        this.formPanel.addRow(new JLabel(SharedLocale.tr("login.idEmail")), this.idCombo);

        this.buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));

        if (this.launcher.getConfig().isOfflineEnabled()) {
            this.buttonsPanel.addElement(this.offlineButton);
            this.buttonsPanel.addElement(Box.createHorizontalStrut(2));
        }
        this.buttonsPanel.addGlue();
        this.buttonsPanel.addElement(this.loginButton);
        this.buttonsPanel.addElement(this.cancelButton);

        this.add(this.formPanel, BorderLayout.CENTER);
        this.add(this.buttonsPanel, BorderLayout.SOUTH);

        this.getRootPane().setDefaultButton(this.loginButton);

        this.idCombo.getEditor().getEditorComponent().addMouseListener(new PopupMouseAdapter() {
            @Override
            protected void showPopup(MouseEvent e) {
                SelectAccountDialog.this.popupManageMenu(e.getComponent(), e.getX(), e.getY());
            }
        });

        this.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectAccountDialog.this.prepareLogin();
            }
        });

        this.offlineButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectAccountDialog.this.setResult(new OfflineSession(SelectAccountDialog.this.launcher.getProperties().getProperty("offlinePlayerName")));
                SelectAccountDialog.this.removeListeners();
                SelectAccountDialog.this.dispose();
            }
        });

        this.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SelectAccountDialog.this.removeListeners();
                SelectAccountDialog.this.dispose();
            }
        });
    }

    private void popupManageMenu(Component component, int x, int y) {
        Object selected = this.idCombo.getSelectedItem();
        JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem;

        if (selected != null && selected instanceof Account) {
            final Account account = (Account) selected;

            menuItem = new JMenuItem(SharedLocale.tr("login.forgetUser"));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SelectAccountDialog.this.accounts.remove(account);
                    Persistence.commitAndForget(SelectAccountDialog.this.accounts);
                }
            });
            popup.add(menuItem);
        }

        menuItem = new JMenuItem(SharedLocale.tr("login.forgetAllPasswords"));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (SwingHelper.confirmDialog(SelectAccountDialog.this,
                        SharedLocale.tr("login.confirmForgetAllPasswords"),
                        SharedLocale.tr("login.forgetAllPasswordsTitle"))) {
                    SelectAccountDialog.this.accounts.forgetSessions();
                    Persistence.commitAndForget(SelectAccountDialog.this.accounts);
                }
            }
        });

        popup.add(menuItem);

        popup.show(component, x, y);
    }

    @SuppressWarnings("deprecation")
    private void prepareLogin() {
        Object selected = this.idCombo.getSelectedItem();

        if (selected != null && selected instanceof Account) {
            Account account = (Account) selected;

            account.setLastUsed(new Date());

            Persistence.commitAndForget(this.accounts);

            this.attemptRefresh(account);
        } else {
            SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noLoginError"), SharedLocale.tr("login.noLoginTitle"));
        }
    }

    private void attemptRefresh(final Account account) {
        LoginCallable callable = new LoginCallable(account);
        ObservableFuture<StoredSession> future = new ObservableFuture<>(
                this.launcher.getExecutor().submit(callable), callable);

        Futures.addCallback(future, new FutureCallback<StoredSession>() {
            @Override
            public void onSuccess(StoredSession result) {
                SelectAccountDialog.this.setResult(result);

                account.setSession(result);
                account.setLastUsed(new Date());

                Persistence.commitAndForget(SelectAccountDialog.this.accounts);
            }

            @Override
            public void onFailure(Throwable t) {
            }
        }, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"), SharedLocale.tr("login.loggingInStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void setResult(Session session) {
        this.session = session;
        this.removeListeners();
        this.dispose();
    }

    public static Session showLoginRequest(Window owner, Launcher launcher) {
        SelectAccountDialog dialog = new SelectAccountDialog(owner, launcher);
        dialog.setVisible(true);
        return dialog.getSession();
    }

    private class LoginCallable implements Callable<StoredSession>, ProgressObservable {
        private final Account account;

        private LoginCallable(Account account) {
            this.account = account;
        }

        @Override
        public StoredSession call() throws AuthenticationException, IOException, InterruptedException {
            LoginService service = SelectAccountDialog.this.launcher.getLoginService();

            return service.refreshSession(this.account);
        }

        @Override
        public double getProgress() {
            return -1;
        }

        @Override
        public String getStatus() {
            return SharedLocale.tr("login.loggingInStatus");
        }
    }

}
