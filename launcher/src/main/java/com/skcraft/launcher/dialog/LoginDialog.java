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
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.auth.*;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.util.SharedLocale;
import com.skcraft.launcher.util.SwingExecutor;
import lombok.Getter;
import lombok.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * The login dialog.
 */
public class LoginDialog extends JDialog {

    private final Launcher launcher;
    @Getter private final AccountList accounts;
    @Getter private Account account;
    @Getter private Session session;

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordText = new JPasswordField();
    private final JButton loginButton = new JButton(SharedLocale.tr("login.login"));
    private final LinkButton recoverButton = new LinkButton(SharedLocale.tr("login.recoverAccount"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
    private final FormPanel formPanel = new FormPanel();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);

    /**
     * Create a new login dialog.
     *
     * @param owner the owner
     * @param launcher the launcher
     */
    public LoginDialog(Window owner, @NonNull Launcher launcher) {
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
                LoginDialog.this.removeListeners();
                LoginDialog.this.dispose();
            }
        });
    }

    public void setRefreshingAccount(Account account) {
        this.usernameField.setText(account.getMojangId());
        this.usernameField.setEditable(false);

        this.account = account;
    }

    @SuppressWarnings("unchecked")
    private void removeListeners() {

    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        this.usernameField.setEditable(true);

        this.loginButton.setFont(this.loginButton.getFont().deriveFont(Font.BOLD));

        this.formPanel.addRow(new JLabel(SharedLocale.tr("login.idEmail")), this.usernameField);
        this.formPanel.addRow(new JLabel(SharedLocale.tr("login.password")), this.passwordText);
        this.buttonsPanel.setBorder(BorderFactory.createEmptyBorder(26, 13, 13, 13));

        this.buttonsPanel.addElement(this.recoverButton);
        this.buttonsPanel.addGlue();
        this.buttonsPanel.addElement(this.loginButton);
        this.buttonsPanel.addElement(this.cancelButton);

        this.add(this.formPanel, BorderLayout.CENTER);
        this.add(this.buttonsPanel, BorderLayout.SOUTH);

        this.getRootPane().setDefaultButton(this.loginButton);

        this.passwordText.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

        this.recoverButton.addActionListener(
                ActionListeners.openURL(this.recoverButton, this.launcher.getProperties().getProperty("resetPasswordUrl")));

        this.loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginDialog.this.prepareLogin();
            }
        });

        this.cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                LoginDialog.this.removeListeners();
                LoginDialog.this.dispose();
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void prepareLogin() {
        String id = this.usernameField.getText();

        if (id != null && id.length() > 4) {
            String password = this.passwordText.getText();

            if (password == null || password.isEmpty()) {
                SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noPasswordError"), SharedLocale.tr("login.noPasswordTitle"));
            } else {
                this.attemptLogin(id, password);
            }
        } else {
            SwingHelper.showErrorDialog(this, SharedLocale.tr("login.noLoginError"), SharedLocale.tr("login.noLoginTitle"));
        }
    }

    private void attemptLogin(final String id, String password) {
        LoginCallable callable = new LoginCallable(id, password);
        ObservableFuture<Session> future = new ObservableFuture<>(this.launcher.getExecutor().submit(callable), callable);

        Futures.addCallback(future, new FutureCallback<Session>() {
            @Override
            public void onSuccess(Session result) {
                LoginDialog.this.session = result;

                Account account = LoginDialog.this.account;

                if (account == null) {
                    account = new Account();
                    account.setLastUsed(new Date());
                    account.setMojangId(id);

                    LoginDialog.this.account = account;
                }

                account.setSession((StoredSession) result);

                LoginDialog.this.session = result;

                LoginDialog.this.returnResult();
            }

            @Override
            public void onFailure(Throwable t) {

            }
        }, SwingExecutor.INSTANCE);

        ProgressDialog.showProgress(this, future, SharedLocale.tr("login.loggingInTitle"), SharedLocale.tr("login.loggingInStatus"));
        SwingHelper.addErrorDialogCallback(this, future);
    }

    private void returnResult() {
        this.removeListeners();
        this.dispose();
    }

    public static Session showLoginRequest(Window owner, Launcher launcher) {
        if (launcher.getAccounts().getSize() > 0) {
            SelectAccountDialog dialog = new SelectAccountDialog(owner, launcher);
            dialog.setVisible(true);

            return dialog.getSession();
        } else {
            LoginDialog dialog = new LoginDialog(owner, launcher);
            dialog.setVisible(true);

            return dialog.getSession();
        }
    }

    private class LoginCallable implements Callable<Session>,ProgressObservable {
        private final String id;
        private final String password;

        private LoginCallable(String id, String password) {
            this.id = id;
            this.password = password;
        }

        @Override
        public Session call() throws AuthenticationException, IOException, InterruptedException {
            LoginService service = LoginDialog.this.launcher.getLoginService();
            List<? extends YggdrasilLoginService.Profile> identities = service.login(LoginDialog.this.launcher.getProperties().getProperty("agentName"), this.id, this.password);

            // The list of identities (profiles in Mojang terms) corresponds to whether the account
            // owns the game, so we need to check that
            if (identities.size() > 0) {
                // Set offline enabled flag to true
                Configuration config = LoginDialog.this.launcher.getConfig();

                if (!config.isOfflineEnabled()) {
                    config.setOfflineEnabled(true);

                    Persistence.commitAndForget(config);
                }

                return identities.get(0).createSession();
            } else {
                throw new AuthenticationException("Minecraft not owned", SharedLocale.tr("login.minecraftNotOwnedError"));
            }
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
