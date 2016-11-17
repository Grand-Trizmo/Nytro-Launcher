/*
 * SK's Minecraft Launcher
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com> and contributors
 * Please see LICENSE.txt for license information.
 */

package com.gildedgames.launcher.ui.dialogs;

import com.gildedgames.launcher.ui.components.PlaceholderTextField;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.dialog.AboutDialog;
import com.skcraft.launcher.dialog.ConsoleFrame;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.*;
import com.skcraft.launcher.util.SharedLocale;
import lombok.NonNull;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to modify configuration options.
 */
public class NewConfigurationDialog extends JDialog {

    private final Configuration config;
    private final ObjectSwingMapper mapper;

    private final JPanel tabContainer = new JPanel(new BorderLayout());
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private final FormPanel javaSettingsPanel = new FormPanel();
    private final JTextField jvmPathText = new PlaceholderTextField(SharedLocale.tr("options.jvmPath.placeholder"));
    private final JTextField jvmArgsText = new PlaceholderTextField(SharedLocale.tr("options.jvmArguments.placeholder"));
    private final JCheckBox jvmAutoMem = new JCheckBox(SharedLocale.tr("options.autoManageMemory"));
    private final JSpinner minMemorySpinner = new JSpinner();
    private final JSpinner maxMemorySpinner = new JSpinner();
    private final JSpinner youngGenSpinner = new JSpinner();
    private final JSpinner[] memSpinners = new JSpinner[] {this.minMemorySpinner, this.maxMemorySpinner, this.youngGenSpinner};
    private final FormPanel gameSettingsPanel = new FormPanel();
    private final JSpinner widthSpinner = new JSpinner();
    private final JSpinner heightSpinner = new JSpinner();
    private final FormPanel proxySettingsPanel = new FormPanel();
    private final JCheckBox useProxyCheck = new JCheckBox(SharedLocale.tr("options.useProxyCheck"));
    private final JTextField proxyHostText = new JTextField();
    private final JSpinner proxyPortText = new JSpinner();
    private final JTextField proxyUsernameText = new JTextField();
    private final JPasswordField proxyPasswordText = new JPasswordField();
    private final FormPanel generalPanel = new FormPanel();
    private final JTextField gameKeyText = new JTextField();
    private final LinedBoxPanel buttonsPanel = new LinedBoxPanel(true);
    private final JButton okButton = new JButton(SharedLocale.tr("button.ok"));
    private final JButton cancelButton = new JButton(SharedLocale.tr("button.cancel"));
    private final JButton aboutButton = new JButton(SharedLocale.tr("options.about"));
    private final JButton logButton = new JButton(SharedLocale.tr("options.launcherConsole"));

    /**
     * Create a new configuration dialog.
     *
     * @param owner the window owner
     * @param launcher the launcher
     */
    public NewConfigurationDialog(Window owner, @NonNull final Launcher launcher) {
        super(owner, ModalityType.DOCUMENT_MODAL);

        this.config = launcher.getConfig();
        this.mapper = new ObjectSwingMapper(this.config);

        this.setTitle(SharedLocale.tr("options.title"));
        this.initComponents();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setSize(new Dimension(720, 480));
        this.setResizable(false);
        this.setLocationRelativeTo(owner);

        this.mapper.map(this.jvmPathText, "jvmPath");
        this.mapper.map(this.jvmArgsText, "jvmArgs");
        this.mapper.map(this.jvmAutoMem, "memoryManaged");
        this.mapper.map(this.minMemorySpinner, "minMemory");
        this.mapper.map(this.maxMemorySpinner, "maxMemory");
        this.mapper.map(this.youngGenSpinner, "youngGen");
        this.mapper.map(this.widthSpinner, "windowWidth");
        this.mapper.map(this.heightSpinner, "widowHeight");
        this.mapper.map(this.useProxyCheck, "proxyEnabled");
        this.mapper.map(this.proxyHostText, "proxyHost");
        this.mapper.map(this.proxyPortText, "proxyPort");
        this.mapper.map(this.proxyUsernameText, "proxyUsername");
        this.mapper.map(this.proxyPasswordText, "proxyPassword");
        this.mapper.map(this.gameKeyText, "gameKey");

        this.mapper.copyFromObject();

        for (JSpinner spinner : NewConfigurationDialog.this.memSpinners) {
            spinner.setEnabled(!NewConfigurationDialog.this.jvmAutoMem.isSelected());
        }

        this.jvmAutoMem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                boolean enabled = NewConfigurationDialog.this.jvmAutoMem.isSelected();

                if (enabled) {
                    launcher.setOptimizedMemoryConfig();
                }

                for (JSpinner spinner : NewConfigurationDialog.this.memSpinners) {
                    spinner.setEnabled(!enabled);
                }

                NewConfigurationDialog.this.maxMemorySpinner.setValue(config.getMaxMemory());
                NewConfigurationDialog.this.minMemorySpinner.setValue(config.getMinMemory());
                NewConfigurationDialog.this.youngGenSpinner.setValue(config.getYoungGen());

            }
        });

        this.minMemorySpinner.setModel(new SpinnerNumberModel(this.config.getMinMemory(), 128, Launcher.is32BitJava() ? 1024 : 16384, 64));
        this.maxMemorySpinner.setModel(new SpinnerNumberModel(this.config.getMaxMemory(), 512, Launcher.is32BitJava() ? 1024 : 16384, 64));

        this.youngGenSpinner.setModel(new SpinnerNumberModel(this.config.getYoungGen(), 32, Launcher.is32BitJava() ? 1024 : 16384, 64));

        SwingHelper.removeFocusBorder(this.tabbedPane, this.javaSettingsPanel, this.gameSettingsPanel, this.proxySettingsPanel, this.generalPanel);
    }

    private void initComponents() {
        this.javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmPath")), this.jvmPathText);
        this.javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.jvmArguments")), this.jvmArgsText);
        this.javaSettingsPanel.addRow(Box.createVerticalStrut(15));
        if (Launcher.is32BitJava()) {
			this.javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.32BitJavaWarning")));
			this.javaSettingsPanel.addRow(Box.createVerticalStrut(6));
		}

        this.javaSettingsPanel.addRow(this.jvmAutoMem);
        this.javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.maxMemory")), this.maxMemorySpinner);
        this.javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.minMemory")), this.minMemorySpinner);
        this.javaSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.youngGen")), this.youngGenSpinner);

        SwingHelper.removeOpaqueness(this.javaSettingsPanel);

        this.gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowWidth")), this.widthSpinner);
        this.gameSettingsPanel.addRow(new JLabel(SharedLocale.tr("options.windowHeight")), this.heightSpinner);
        SwingHelper.removeOpaqueness(this.gameSettingsPanel);

        this.proxySettingsPanel.addRow(this.useProxyCheck);
        this.proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyHost")), this.proxyHostText);
        this.proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPort")), this.proxyPortText);
        this.proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyUsername")), this.proxyUsernameText);
        this.proxySettingsPanel.addRow(new JLabel(SharedLocale.tr("options.proxyPassword")), this.proxyPasswordText);
        SwingHelper.removeOpaqueness(this.proxySettingsPanel);

        this.generalPanel.addRow(new JLabel(SharedLocale.tr("options.gameKey")), this.gameKeyText);
        SwingHelper.removeOpaqueness(this.generalPanel);

        this.buttonsPanel.addElement(this.logButton);
        this.buttonsPanel.addElement(this.aboutButton);
        this.buttonsPanel.addGlue();
        this.buttonsPanel.addElement(this.cancelButton);
        this.buttonsPanel.addElement(this.okButton);

        this.tabbedPane.addTab(SharedLocale.tr("options.generalTab"), SwingHelper.alignTabbedPane(this.generalPanel));
        this.tabbedPane.addTab(SharedLocale.tr("options.javaTab"), SwingHelper.alignTabbedPane(this.javaSettingsPanel));
//        this.tabbedPane.addTab(SharedLocale.tr("options.proxyTab"), SwingHelper.alignTabbedPane(this.proxySettingsPanel));
//        this.tabbedPane.addTab(SharedLocale.tr("options.minecraftTab"), SwingHelper.alignTabbedPane(this.gameSettingsPanel));

        this.tabContainer.add(this.tabbedPane, BorderLayout.CENTER);
        this.tabContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(this.tabContainer, BorderLayout.CENTER);
        this.add(this.buttonsPanel, BorderLayout.SOUTH);

        this.okButton.setPreferredSize(new Dimension(60, 32));

        this.aboutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.showAboutDialog(NewConfigurationDialog.this);
            }
        });
        this.aboutButton.setPreferredSize(new Dimension(80, 32));

        this.logButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConsoleFrame.showMessages();
            }
        });
        this.logButton.setPreferredSize(new Dimension(80, 32));

        this.cancelButton.addActionListener(ActionListeners.dispose(this));
        this.cancelButton.setPreferredSize(new Dimension(70, 32));

        this.okButton.setPreferredSize(new Dimension(70, 32));
        this.okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewConfigurationDialog.this.save();
            }
        });

        SwingHelper.equalWidth(this.okButton, this.cancelButton);
    }

    /**
     * Save the configuration and close the dialog.
     */
    public void save() {
        this.mapper.copyFromSwing();
        Persistence.commitAndForget(this.config);
        this.dispose();
    }
}
