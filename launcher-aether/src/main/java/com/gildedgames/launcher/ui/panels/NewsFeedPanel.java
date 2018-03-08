package com.gildedgames.launcher.ui.panels;

import com.gildedgames.launcher.ui.components.NewsTile;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.NewsFeedManager;
import com.gildedgames.launcher.ui.resources.NewsFeedManager.NewsFeed;
import com.gildedgames.launcher.ui.resources.NewsFeedManager.NewsPost;
import com.gildedgames.launcher.ui.resources.NewsFeedManager.NewsSection;
import com.google.common.util.concurrent.ListenableFuture;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public class NewsFeedPanel extends JPanel {
	private JPanel list;

	public NewsFeedPanel() {
		this.setLayout(new BorderLayout());

		this.initComponents();
	}

	private void initComponents() {
//		GridBagLayout layout = new GridBagLayout();

		this.list = new JPanel();
		this.list.setLayout(new BoxLayout(this.list, BoxLayout.Y_AXIS));
		this.list.setOpaque(false);

		this.add(this.list, BorderLayout.NORTH);

		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	}

	public void repopulate(NewsFeedManager manager, NewsFeed feed) {
		this.list.removeAll();

		for (NewsSection section : feed.getSections()) {
			JPanel row = new JPanel(new BorderLayout());
			row.setOpaque(false);

			this.list.add(row);

			row.add(this.createLabel(section.getTitle()), BorderLayout.NORTH);

			JPanel cards = new JPanel();
			cards.setOpaque(false);
			cards.setLayout(new FlowLayout(FlowLayout.LEFT));
			cards.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

			for (NewsPost post : section.getPosts()) {
				ListenableFuture<Image> image = manager.getImage(post.getImages().get("launcher_preview"));

				NewsTile tile = new NewsTile(post, image);

				cards.add(tile);
			}

			JScrollPane cardsPane = new JScrollPane(cards);
			cardsPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			cardsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

			JScrollBar horBar = cardsPane.getHorizontalScrollBar();
			horBar.setUI(new FlatScrollbar(horBar));
			horBar.setOpaque(false);
			horBar.setUnitIncrement(9);

			cardsPane.setOpaque(false);
			cardsPane.setBorder(BorderFactory.createEmptyBorder());
			cardsPane.getViewport().setOpaque(false);

			row.add(cardsPane, BorderLayout.CENTER);
		}
	}

	private JLabel createLabel(String title) {
		JLabel label = new JLabel(title.toUpperCase());
		label.setFont(LauncherFonts.OSWALD_NORMAL.deriveFont(Font.ITALIC).deriveFont(18.0f));
		label.setForeground(new Color(230, 230, 230, 255));
		label.setBorder(BorderFactory.createEmptyBorder(20, 4, 12, 0));

		return label;
	}

	public class FlatScrollbar extends BasicScrollBarUI {
		private boolean isHovered;

		public FlatScrollbar(JScrollBar bar) {
			bar.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					FlatScrollbar.this.isHovered = true;
					bar.repaint();
				}

				@Override
				public void mouseExited(MouseEvent e) {
					FlatScrollbar.this.isHovered = false;
					bar.repaint();
				}
			});
		}

		@Override
		protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
			g.setColor(new Color(0, 0, 0, 90));

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.fill(new RoundRectangle2D.Float(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height - 10, 8, 8));
		}

		@Override
		protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
			g.setColor(this.isHovered ? new Color(255, 255, 255, 220) : new Color(190, 190, 190, 255));

			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			g2.fill(new RoundRectangle2D.Float(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height - 10, 8, 8));
		}

		@Override
		protected JButton createDecreaseButton(int orientation) {
			JButton b = new JButton();
			b.setVisible(false);
			b.setPreferredSize(new Dimension(0, 0));
			b.setMaximumSize(new Dimension(0, 0));

			return b;
		}

		@Override
		protected JButton createIncreaseButton(int orientation) {
			JButton b = new JButton();
			b.setVisible(false);
			b.setPreferredSize(new Dimension(0, 0));
			b.setMaximumSize(new Dimension(0, 0));

			return b;
		}

		@Override
		protected void configureScrollBarColors() {

		}
	}
}
