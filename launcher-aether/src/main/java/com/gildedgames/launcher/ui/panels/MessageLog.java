package com.gildedgames.launcher.ui.panels;


import com.gildedgames.launcher.ui.resources.LauncherStyles;
import com.skcraft.launcher.swing.TextFieldPopupMenu;
import com.skcraft.launcher.util.LimitLinesDocumentListener;
import com.skcraft.launcher.util.SimpleLogFormatter;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class MessageLog extends JPanel {
	private final int numLines;

	private final boolean colorEnabled;

	private JTextComponent textComponent;

	private Document document;

	private Handler loggerHandler;

	private final SimpleAttributeSet defaultAttributes = new SimpleAttributeSet();

	private final SimpleAttributeSet highlightedAttributes, errorAttributes, infoAttributes, debugAttributes;

	public MessageLog(int numLines, boolean colorEnabled) {
		this.numLines = numLines;
		this.colorEnabled = colorEnabled;

		this.highlightedAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground(this.highlightedAttributes, new Color(0xFF7F00));

		this.errorAttributes = new SimpleAttributeSet();
		StyleConstants.setForeground(this.errorAttributes, new Color(0xFF0000));
		this.infoAttributes = new SimpleAttributeSet();
		this.debugAttributes = new SimpleAttributeSet();

		this.setLayout(new BorderLayout());

		this.initComponents();
	}

	private void initComponents() {
		if (this.colorEnabled) {
			this.textComponent = new JTextPane() {
				@Override
				public boolean getScrollableTracksViewportWidth() {
					return true;
				}
			};
		} else {
			JTextArea text = new JTextArea();
			this.textComponent = text;
			text.setLineWrap(true);
			text.setWrapStyleWord(true);
		}

		this.textComponent.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		this.textComponent.setForeground(new Color(0xbbbbbb));
		this.textComponent.setBackground(LauncherStyles.LAUNCHER_BACKGROUND);
		this.textComponent.setEditable(false);
		this.textComponent.setComponentPopupMenu(TextFieldPopupMenu.INSTANCE);

		DefaultCaret caret = (DefaultCaret) this.textComponent.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		this.document = this.textComponent.getDocument();
		this.document.addDocumentListener(new LimitLinesDocumentListener(this.numLines, true));

		JScrollPane scrollText = new JScrollPane(this.textComponent);
		scrollText.setBorder(null);
		scrollText.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollText.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollText.setOpaque(false);

		this.add(scrollText, BorderLayout.CENTER);
	}

	public String getPastableText() {
		String text = this.textComponent.getText().replaceAll("[\r\n]+", "\n");
		text = text.replaceAll("Session ID is [A-Fa-f0-9]+", "Session ID is [redacted]");
		return text;
	}

	public void clear() {
		this.textComponent.setText("");
	}

	/**
	 * Log a message given the {@link javax.swing.text.AttributeSet}.
	 *
	 * @param line line
	 * @param attributes attribute set, or null for none
	 */
	public void log(final String line, AttributeSet attributes) {
		final Document d = this.document;
		final JTextComponent t = this.textComponent;
		if (this.colorEnabled) {
			if (line.startsWith("(!!)")) {
				attributes = this.highlightedAttributes;
			}
		}
		final AttributeSet a = attributes;

		SwingUtilities.invokeLater(() -> {
			try {
				int offset = d.getLength();
				d.insertString(offset, line,
						(a != null && this.colorEnabled) ? a : this.defaultAttributes);
				t.setCaretPosition(d.getLength());
			} catch (BadLocationException ble) {

			}
		});
	}

	/**
	 * Get an output stream that can be written to.
	 *
	 * @return output stream
	 */
	public MessageLog.ConsoleOutputStream getOutputStream() {
		return this.getOutputStream((AttributeSet) null);
	}

	/**
	 * Get an output stream with the given attribute set.
	 *
	 * @param attributes attributes
	 * @return output stream
	 */
	public MessageLog.ConsoleOutputStream getOutputStream(AttributeSet attributes) {
		return new MessageLog.ConsoleOutputStream(attributes);
	}

	/**
	 * Get an output stream using the give color.
	 *
	 * @param color color to use
	 * @return output stream
	 */
	public MessageLog.ConsoleOutputStream getOutputStream(Color color) {
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		StyleConstants.setForeground(attributes, color);
		return this.getOutputStream(attributes);
	}

	/**
	 * Consume an input stream and print it to the dialog. The consumer
	 * will be in a separate daemon thread.
	 *
	 * @param from stream to read
	 */
	public void consume(InputStream from) {
		this.consume(from, this.getOutputStream());
	}

	/**
	 * Consume an input stream and print it to the dialog. The consumer
	 * will be in a separate daemon thread.
	 *
	 * @param from stream to read
	 * @param color color to use
	 */
	public void consume(InputStream from, Color color) {
		this.consume(from, this.getOutputStream(color));
	}

	/**
	 * Consume an input stream and print it to the dialog. The consumer
	 * will be in a separate daemon thread.
	 *
	 * @param from stream to read
	 * @param attributes attributes
	 */
	public void consume(InputStream from, AttributeSet attributes) {
		this.consume(from, this.getOutputStream(attributes));
	}

	/**
	 * Internal method to consume a stream.
	 *
	 * @param from stream to consume
	 * @param outputStream console stream to write to
	 */
	private void consume(InputStream from, MessageLog.ConsoleOutputStream outputStream) {
		final InputStream in = from;
		final PrintWriter out = new PrintWriter(outputStream, true);
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				byte[] buffer = new byte[1024];
				try {
					int len;
					while ((len = in.read(buffer)) != -1) {
						String s = new String(buffer, 0, len);
						System.out.print(s);
						out.append(s);
						out.flush();
					}
				} catch (IOException e) {
				} finally {
					closeQuietly(in);
					closeQuietly(out);
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	public SimpleAttributeSet asDefault() {
		return this.defaultAttributes;
	}

	public SimpleAttributeSet asHighlighted() {
		return this.highlightedAttributes;
	}

	public SimpleAttributeSet asError() {
		return this.errorAttributes;
	}

	public SimpleAttributeSet asInfo() {
		return this.infoAttributes;
	}

	public SimpleAttributeSet asDebug() {
		return this.debugAttributes;
	}

	/**
	 * Used to send logger messages to the console.
	 */
	private class ConsoleLoggerHandler extends Handler {
		private final SimpleLogFormatter formatter = new SimpleLogFormatter();

		@Override
		public void publish(LogRecord record) {
			Level level = record.getLevel();
			Throwable t = record.getThrown();
			AttributeSet attributes = MessageLog.this.defaultAttributes;

			if (level.intValue() >= Level.WARNING.intValue()) {
				attributes = MessageLog.this.errorAttributes;
			} else if (level.intValue() < Level.INFO.intValue()) {
				attributes = MessageLog.this.debugAttributes;
			}

			MessageLog.this.log(this.formatter.format(record), attributes);
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
	}

	/**
	 * Used to send console messages to the console.
	 */
	private class ConsoleOutputStream extends ByteArrayOutputStream {
		private AttributeSet attributes;

		private ConsoleOutputStream(AttributeSet attributes) {
			this.attributes = attributes;
		}

		@Override
		public void flush() {
			String data = this.toString();
			if (data.length() == 0) return;
			MessageLog.this.log(data, this.attributes);
			this.reset();
		}
	}
}