package com.gildedgames.launcher.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.util.Stack;

public class LauncherLayout {
	private final JPanel root;

	private Stack<JComponent> stack = new Stack<>();

	public LauncherLayout() {
		this.root = new JPanel();
		this.root.setLayout(new BorderLayout());
	}

	public void show(JComponent comp) {
		this.stack.push(comp);

		this.setView(comp);
	}

	public void back() {
		this.stack.pop();

		JComponent panel = this.stack.peek();

		this.setView(this.stack.peek());

		if (panel instanceof IListeningView) {
			((IListeningView) panel).reload();
		}
	}

	public JPanel getRoot() {
		return this.root;
	}

	private void setView(JComponent view) {
		SwingUtilities.invokeLater(() -> {
			this.root.removeAll();
			this.root.add(view, BorderLayout.CENTER);

			this.root.revalidate();
			this.root.repaint();
		});
	}
}
