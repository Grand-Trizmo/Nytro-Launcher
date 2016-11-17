package com.gildedgames.launcher.ui.views.game;

import com.gildedgames.launcher.ui.components.FlatButton;
import com.gildedgames.launcher.ui.panels.MessageLog;
import com.gildedgames.launcher.ui.panels.ProgressIndicatorPanel;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.LauncherStyles;
import lombok.Getter;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class ConsolePanel extends JPanel {
	@Getter
	private MessageLog log;

	@Getter
	private ProgressIndicatorPanel indicator;

	private JPanel buttons;

	public ConsolePanel() {
		this.setLayout(new BorderLayout());

		this.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);

		this.initComponents();
	}

	private void initComponents() {
		this.log = new MessageLog(600, true);

		this.add(this.log, BorderLayout.CENTER);

		FlowLayout buttonLayout = new FlowLayout();
		buttonLayout.setVgap(10);
		buttonLayout.setAlignment(FlowLayout.RIGHT);

		this.buttons = new JPanel(buttonLayout);
		this.buttons.setOpaque(false);

		FlatButton forceClose = new FlatButton("Force close", LauncherFonts.OPEN_SANS_REGULAR.deriveFont(12.0f));
		forceClose.setStyle(FlatButton.ButtonStyle.NORMAL);

		this.buttons.add(forceClose);

		this.add(this.buttons, BorderLayout.SOUTH);
	}

	public void setProcess(Process process) {

	}
}
