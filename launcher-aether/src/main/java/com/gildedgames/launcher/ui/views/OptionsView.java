package com.gildedgames.launcher.ui.views;

import com.gildedgames.launcher.ui.LauncherFrame;
import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.components.FlatTabPane;
import com.gildedgames.launcher.ui.components.FlatTextField;
import com.gildedgames.launcher.ui.components.InputIndicator;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherIcons;
import com.gildedgames.launcher.ui.resources.LauncherStyles;
import com.gildedgames.launcher.ui.styles.PushButtonUI;
import com.gildedgames.launcher.user.GameKeyManager.KeyVerificationResponse;
import com.gildedgames.launcher.util.BrowserUtil;
import com.gildedgames.launcher.util.FolderDeleteRunnable;
import com.gildedgames.launcher.util.FolderSizeCalculator;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.concurrency.ObservableFuture;
import com.skcraft.launcher.Configuration;
import com.skcraft.launcher.Launcher;
import com.skcraft.launcher.persistence.Persistence;
import com.skcraft.launcher.swing.SwingHelper;
import com.skcraft.launcher.util.SwingExecutor;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class OptionsView extends JPanel {
	private final Set<SettingsPanel> savable = new HashSet<>();

	private final FlatTabPane pane = new FlatTabPane();

	private final Launcher launcher;

	private final LauncherFrame frame;

	public OptionsView(Launcher launcher, LauncherFrame frame) {
		this.launcher = launcher;
		this.frame = frame;

		this.setLayout(new BorderLayout());

		this.initComponents();

		this.load();
	}

	private void initComponents() {
		GeneralPanel general = new GeneralPanel(this.launcher);

		this.savable.clear();

		this.pane.addTab("General", general);
		this.savable.add(general);

		MinecraftPanel minecraft = new MinecraftPanel(this.launcher);

		this.pane.addTab("Minecraft", minecraft);
		this.savable.add(minecraft);

		AboutPanel about = new AboutPanel();

		this.pane.addTab("About", about);

		this.pane.setActive(general);

		this.add(this.pane, BorderLayout.CENTER);

		FlowLayout bottomLayout = new FlowLayout();
		bottomLayout.setAlignment(FlowLayout.RIGHT);
		bottomLayout.setHgap(10);

		JPanel bottomButtons = new JPanel();
		bottomButtons.setLayout(bottomLayout);
		bottomButtons.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		bottomButtons.setOpaque(false);

		FlatButton saveButton = new FlatButton("Save", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		saveButton.setStyle(FlatButton.ButtonStyle.HIGHLIGHTED);
		saveButton.setPreferredSize(new Dimension(100, 32));
		saveButton.addActionListener(e -> {
			this.save();

			this.frame.getLauncherLayout().back();
		});

		FlatButton closeButton = new FlatButton("Back", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		closeButton.setStyle(FlatButton.ButtonStyle.LIGHT);
		closeButton.setPreferredSize(new Dimension(100, 32));
		closeButton.addActionListener(e -> this.frame.getLauncherLayout().back());

		bottomButtons.add(closeButton);
		bottomButtons.add(saveButton);

		this.add(bottomButtons, BorderLayout.SOUTH);

		this.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);
	}

	private void load() {
		for (SettingsPanel panel : this.savable) {
			panel.load(this.launcher, this.launcher.getConfig());
		}
	}

	private void save() {
		if (!this.validateSettings()) {
			return;
		}

		for (SettingsPanel panel : this.savable) {
			panel.save(this.launcher, this.launcher.getConfig());
		}

		Persistence.commitAndForget(this.launcher.getConfig());
	}

	private boolean validateSettings() {
		try {
			for (SettingsPanel panel : this.savable) {
				panel.validate(this.launcher, this.launcher.getConfig());
			}
		} catch (ConfigurationValidationException e) {
			SwingHelper.showMessageDialog(this.frame, e.getMessage(), "Configuration problem", null, JOptionPane.WARNING_MESSAGE);

			return false;
		}

		return true;
	}

	private class GeneralPanel extends SettingsPanel {
		private final JTextField keyInput;

		private final JLabel cacheSizeLabel;

		private final InputIndicator keyInputIndicator;

		private final FlatButton verifyKeyButton;

		public GeneralPanel(Launcher launcher) {
			super();

			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.weightx = 1.0D;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.NORTHWEST;

			c.gridy = 0;

			this.addLabel("Storage", c, 16.0f);

			c.gridy = 1;
			c.ipady = 20;

			JLabel dataDirectoryString = new JLabel();
			dataDirectoryString.setText("<html>The current data directory is<br><b>" + launcher.getBaseDir().getAbsolutePath() + "</b></html>");
			dataDirectoryString.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
			dataDirectoryString.setForeground(Color.WHITE);

			this.add(dataDirectoryString, c);

			c.ipady = 0;
			c.gridx = 0;
			c.gridy = 2;
			c.weightx = 0.0D;
			c.weighty = 1.0D;
			c.anchor = GridBagConstraints.WEST;

			FlatButton dataDirectoryButton = new FlatButton("Open data directory", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
			dataDirectoryButton.addActionListener(e -> {
				try {
					Desktop.getDesktop().open(launcher.getBaseDir());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});

			this.add(dataDirectoryButton, c);

			c.weightx = 1.0D;
			c.weighty = 1.0D;
			c.gridx = 0;
			c.gridy = 3;

			this.add(Box.createVerticalStrut(20), c);

			c.gridy = 4;

			this.addLabel("Temporary files", c, 16.0f);

			c.gridy = 5;

			this.add(Box.createVerticalStrut(12), c);

			c.gridy = 6;

			this.addLabel("The current size of the cache is", c, 12.0f);

			c.gridy = 7;

			this.cacheSizeLabel = new JLabel();
			this.cacheSizeLabel.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(14.0f));
			this.cacheSizeLabel.setForeground(Color.LIGHT_GRAY);

			this.refreshCacheSize();

			this.add(this.cacheSizeLabel, c);

			c.gridy = 8;

			this.add(Box.createVerticalStrut(12), c);

			c.gridy = 9;

			FlatButton clearCacheButton = new FlatButton("Clear cache", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
			clearCacheButton.setStyle(FlatButton.ButtonStyle.LIGHT);
			clearCacheButton.setEnabled(!OptionsView.this.frame.getLaunchSupervisor().isBusy() && !OptionsView.this.frame.isUpdating());

			clearCacheButton.addActionListener(e -> {
				boolean result = SwingHelper.confirmDialog(OptionsView.this.frame, "Clearing the cache usually serves no benefit, and may cause issues.\n\nOnly do this if you're absolutely certain, or a developer has asked you to.",
						"Are you sure?");

				if (!result) {
					return;
				}

				Path[] paths = new Path[] {
						OptionsView.this.launcher.getCacheDir().toPath(),
						OptionsView.this.launcher.getTemporaryDir().toPath()
				};

				FolderDeleteRunnable clearTask = new FolderDeleteRunnable(paths);

				ListenableFuture<Void> clearFuture = launcher.getExecutor().submit(clearTask);

				Futures.addCallback(clearFuture, new FutureCallback<Void>() {
					@Override
					public void onSuccess(@Nullable Void result) {
						GeneralPanel.this.refreshCacheSize();

						OptionsView.this.frame.getNewsFeedManager().clear();
						OptionsView.this.frame.getAvatarManager().clear();

						SwingHelper.showMessageDialog(OptionsView.this.frame, "Please restart your launcher to finish clearing the cache.", "Task finished", null, JOptionPane.INFORMATION_MESSAGE);
					}

					@Override
					public void onFailure(Throwable t) { }
				});

				SwingHelper.addErrorDialogCallback(OptionsView.this.frame, clearFuture);

				ProgressView view = new ProgressView(OptionsView.this.frame, clearFuture, new ObservableFuture<>(clearFuture, clearTask), "Clearing cache", false);

				OptionsView.this.frame.getLauncherLayout().show(view);
			});

			this.add(clearCacheButton, c);

			c.gridy = 10;

			this.add(Box.createVerticalStrut(20), c);

			c.gridy = 11;

			this.addLabel("Keys", c, 16.0f);

			c.gridy = 12;

			this.add(Box.createVerticalStrut(12), c);

			c.gridy = 13;

			this.addLabel("Game keys unlock special access to features and other goodies.", c, 12.0f);

			c.gridy = 14;

			c.weightx = 0.0D;
			c.weighty = 1.0D;
			c.ipady = 0;

			this.add(Box.createVerticalStrut(12), c);

			c.gridy = 15;

			this.keyInput = new FlatTextField("Game key");
			this.keyInput.setPreferredSize(new Dimension(400, 32));
			this.keyInput.addKeyListener(new KeyAdapter() {
				@Override
				public void keyTyped(KeyEvent e) {
					GeneralPanel.this.keyInputIndicator.setVisible(false);
				}
			});

			this.verifyKeyButton = new FlatButton("Verify", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
			this.verifyKeyButton.addActionListener(e -> this.validateKey());
			this.verifyKeyButton.setPreferredSize(new Dimension(90, 32));

			JPanel keyInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
			keyInputPanel.add(this.keyInput);
			keyInputPanel.add(this.verifyKeyButton);
			keyInputPanel.setOpaque(false);

			this.add(keyInputPanel, c);

			c.gridy = 16;

			this.keyInputIndicator = new InputIndicator();

			this.add(this.keyInputIndicator, c);
		}

		private void validateKey() {
			this.verifyKeyButton.setEnabled(false);

			this.keyInputIndicator.setVisible(true);
			this.keyInputIndicator.setText("Communicating with servers");
			this.keyInputIndicator.setChecking(true);

			String key = this.keyInput.getText();

			ListenableFuture<KeyVerificationResponse> future = OptionsView.this.frame.getKeyManager().validate(key);

			Futures.addCallback(future, new FutureCallback<KeyVerificationResponse>() {
				@Override
				public void onSuccess(@Nullable KeyVerificationResponse result) {
					if (result == null) {
						throw new RuntimeException("Key verification response is null");
					}

					GeneralPanel.this.keyInputIndicator.setChecking(false);

					if (result.packagesContainsKey(key)) {
						GeneralPanel.this.keyInputIndicator.setText("Verified");
						GeneralPanel.this.keyInputIndicator.setValid(true);
					} else {
						GeneralPanel.this.keyInputIndicator.setText("Invalid key");
						GeneralPanel.this.keyInputIndicator.setValid(false);
					}

					GeneralPanel.this.verifyKeyButton.setEnabled(true);
				}

				@Override
				public void onFailure(Throwable t) {
					Logger.getGlobal().info(t.getMessage());
					GeneralPanel.this.keyInputIndicator.setText("There was a problem checking the key. Please try again later.");
					GeneralPanel.this.keyInputIndicator.setValid(false);
					GeneralPanel.this.keyInputIndicator.setChecking(false);
					GeneralPanel.this.verifyKeyButton.setEnabled(true);
				}
			});
		}

		private void refreshCacheSize() {
			this.cacheSizeLabel.setText("(calculating cache size)");

			Path[] paths = new Path[] {
					OptionsView.this.launcher.getCacheDir().toPath(),
					OptionsView.this.launcher.getTemporaryDir().toPath()
			};

			ListenableFuture<Long> cacheSizeFuture = OptionsView.this.launcher.getExecutor().submit(new FolderSizeCalculator(paths));

			Futures.addCallback(cacheSizeFuture, new FutureCallback<Long>() {
				@Override
				public void onSuccess(Long result) {
					DecimalFormat format = new DecimalFormat("#.##");

					double size = result / 1024.0D / 1024.0D;

					GeneralPanel.this.cacheSizeLabel.setText(format.format(size) + "MB");
				}

				@Override
				public void onFailure(Throwable t) {
					GeneralPanel.this.cacheSizeLabel.setText("(couldn't calculate file size)");
				}
			}, SwingExecutor.INSTANCE);
		}

		@Override
		public void save(Launcher launcher, Configuration config) {
			config.setGameKey(this.keyInput.getText());
		}

		@Override
		public void load(Launcher launcher, Configuration config) {
			this.keyInput.setText(config.getGameKey());

			this.validateKey();
		}

		@Override
		public void validate(Launcher launcher, Configuration config) throws ConfigurationValidationException {

		}
	}

	private class MinecraftPanel extends SettingsPanel {
		private final Launcher launcher;

		private JCheckBox autoManageMemory;

		private JSpinner maxMemorySpinner, minMemorySpinner, youngGenMemorySpinner;

		private FlatTextField jvmPath, jvmArgs;

		public MinecraftPanel(Launcher launcher) {
			this.launcher = launcher;

			this.initComponents();
		}

		private void initComponents() {
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.weightx = 1.0D;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.NORTHWEST;

			c.gridy = 0;

			this.addLabel("Java", c, 16.0f);

			c.gridy = 1;

			this.add(Box.createVerticalStrut(12), c);

			c.gridy = 2;

			this.autoManageMemory = new JCheckBox("Auto manage memory (recommended)");
			this.autoManageMemory.addActionListener(e -> this.updateMemorySpinners());
			this.autoManageMemory.setForeground(Color.WHITE);
			this.autoManageMemory.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
			this.autoManageMemory.setUI(new PushButtonUI());

			this.add(this.autoManageMemory, c);

			// Max Memory
			c.gridy = 3;
			this.add(Box.createVerticalStrut(20), c);

			c.gridy = 4;
			this.addLabel("Max Memory", c, 12.0f);

			c.gridx = 1;
			this.maxMemorySpinner = new JSpinner();
			this.maxMemorySpinner.setPreferredSize(new Dimension(200, 20));

			this.add(this.maxMemorySpinner, c);

			// Min memory
			c.gridx = 0;
			c.gridy = 5;
			this.add(Box.createVerticalStrut(10), c);

			c.gridy = 6;
			this.addLabel("Minimum Memory", c, 12.0f);

			c.gridx = 1;
			this.minMemorySpinner = new JSpinner();
			this.minMemorySpinner.setPreferredSize(new Dimension(200, 20));

			this.add(this.minMemorySpinner, c);

			// PermGen
			c.gridx = 0;
			c.gridy = 7;
			this.add(Box.createVerticalStrut(10), c);

			c.gridy = 8;
			this.addLabel("YoungGen Memory", c, 12.0f);

			c.gridx = 1;

			this.youngGenMemorySpinner = new JSpinner();
			this.youngGenMemorySpinner.setPreferredSize(new Dimension(200, 20));

			this.add(this.youngGenMemorySpinner, c);

			// JVM Path
			c.gridx = 0;
			c.gridy = 9;

			this.add(Box.createVerticalStrut(30), c);

			c.gridy = 10;

			this.addLabel("Java Path", c, 12.0f);

			c.gridx = 1;
			this.jvmPath = new FlatTextField("(default)");
			this.jvmPath.setPreferredSize(new Dimension(400, 26));

			this.add(this.jvmPath, c);

			// JVM Path
			c.gridx = 0;
			c.gridy = 11;

			this.add(Box.createVerticalStrut(10), c);

			c.gridy = 12;

			this.addLabel("Java Arguments", c, 12.0f);

			c.gridx = 1;
			this.jvmArgs = new FlatTextField("(none)");
			this.jvmArgs.setPreferredSize(new Dimension(400, 26));

			this.add(this.jvmArgs, c);
		}

		private void updateMemorySpinners() {
			boolean enabled = !this.autoManageMemory.isSelected();

			this.minMemorySpinner.setEnabled(enabled);
			this.maxMemorySpinner.setEnabled(enabled);
			this.youngGenMemorySpinner.setEnabled(enabled);

			if (this.autoManageMemory.isSelected()) {
				this.launcher.setOptimizedMemoryConfig();

				Configuration config = this.launcher.getConfig();

				this.maxMemorySpinner.setValue(config.getMaxMemory());
				this.minMemorySpinner.setValue(config.getMinMemory());
				this.youngGenMemorySpinner.setValue(config.getYoungGen());
			}
		}

		@Override
		public void save(Launcher launcher, Configuration config) {
			config.setJvmPath(this.jvmPath.getText());
			config.setJvmArgs(this.jvmArgs.getText());
			config.setYoungGen((Integer) this.youngGenMemorySpinner.getValue());
			config.setMaxMemory((Integer) this.maxMemorySpinner.getValue());
			config.setMinMemory((Integer) this.minMemorySpinner.getValue());
			config.setMemoryManaged(this.autoManageMemory.isSelected());

			if (this.autoManageMemory.isSelected()) {
				launcher.setOptimizedMemoryConfig();
			}
		}

		@Override
		public void load(Launcher launcher, Configuration config) {
			this.jvmPath.setText(config.getJvmPath());
			this.jvmArgs.setText(config.getJvmArgs());
			this.youngGenMemorySpinner.setValue(config.getYoungGen());
			this.maxMemorySpinner.setValue(config.getMaxMemory());
			this.minMemorySpinner.setValue(config.getMinMemory());
			this.autoManageMemory.setSelected(config.isMemoryManaged());

			this.updateMemorySpinners();
		}

		@Override
		public void validate(Launcher launcher, Configuration config) throws ConfigurationValidationException {
			if ((int) this.maxMemorySpinner.getValue() < (int) this.minMemorySpinner.getValue()) {
				throw new ConfigurationValidationException("The minimum memory value cannot be smaller than the max memory value.");
			}
		}
	}

	private class AboutPanel extends SettingsPanel {
		public AboutPanel() {
			super();

			this.initComponents();
		}

		private void initComponents() {
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0;
			c.gridheight = 1;
			c.gridwidth = 1;
			c.weightx = 1.0D;
			c.fill = GridBagConstraints.NONE;
			c.anchor = GridBagConstraints.NORTHWEST;

			c.gridy = 0;

			this.addLabel(OptionsView.this.frame.getTitle(), c, 20.0f);

			c.gridy = 1;

			this.addLabel("Official Release", c, 14.0f);

			c.gridy = 2;
			FlatButton githublauncher = this.createURLButton("          View the source code on Github", "https://github.com/grand-trizmo/Nytro-Launcher", LauncherIcons.GITHUB);
			this.add(githublauncher, c);

			c.gridy = 3;
			this.add(Box.createVerticalStrut(12), c);

			c.gridy = 4;
			FlatButton kookyWebsite = this.createURLButton("          The official Nytro Website", "https://www.nytro.co", LauncherIcons.NET);
			this.add(kookyWebsite, c);

			c.gridy = 5;
			this.add(Box.createVerticalStrut(12), c);

			c.gridy = 6;
			FlatButton skcraftButton = this.createURLButton("          View SKCraft's Launcher on GitHub", "https://github.com/SKCraft/Launcher", LauncherIcons.GITHUB);
			this.add(skcraftButton, c);
		}

		private FlatButton createURLButton(String text, String url, ImageIcon icon) {
			FlatButton btn = new FlatButton(text, LauncherFonts.OPEN_SANS_REGULAR.deriveFont(16.0f));
			btn.setStyle(FlatButton.ButtonStyle.TRANSPARENT);
			btn.setButtonIcon(icon);
			btn.setAlign(FlatButton.AlignState.LEFT);
			btn.addActionListener(e -> {
				BrowserUtil.openPage(url);
			});
			btn.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));

			return btn;
		}

		@Override
		public void save(Launcher launcher, Configuration config) {

		}

		@Override
		public void load(Launcher launcher, Configuration config) {

		}

		@Override
		public void validate(Launcher launcher, Configuration config) throws ConfigurationValidationException {

		}
	}
	private abstract class SettingsPanel extends JPanel {
		public SettingsPanel() {
			this.setOpaque(false);
			this.setLayout(new GridBagLayout());
			this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		}

		protected void addLabel(String text, GridBagConstraints c, float size) {
			JLabel label = new JLabel(text);
			label.setFont(LauncherFonts.OPEN_SANS_REGULAR.deriveFont(size));
			label.setForeground(Color.WHITE);

			this.add(label, c);
		}

		public abstract void save(Launcher launcher, Configuration config);

		public abstract void load(Launcher launcher, Configuration config);

		public abstract void validate(Launcher launcher, Configuration config) throws ConfigurationValidationException;
	}

	private class ConfigurationValidationException extends Throwable {
		public ConfigurationValidationException(String message) {
			super(message);
		}
	}
}
