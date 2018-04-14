package com.gildedgames.launcher.ui.panels;

import com.gildedgames.launcher.ui.components.NewsTile;
import com.gildedgames.launcher.ui.resources.LauncherFonts;
import com.gildedgames.launcher.ui.resources.NewsFeedManager;
import com.gildedgames.launcher.ui.resources.NewsFeedManager.NewsFeed;
import com.gildedgames.launcher.ui.resources.NewsFeedManager.NewsPost;
import com.gildedgames.launcher.ui.resources.NewsFeedManager.NewsSection;
import com.gildedgames.launcher.ui.styles.FlatScrollbarUI;
import com.google.common.util.concurrent.ListenableFuture;

import javax.swing.*;
import java.awt.*;

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
		this.list.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		this.list.setOpaque(false);

		this.add(this.list, BorderLayout.NORTH);

		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}

	public void repopulate(NewsFeedManager manager, NewsFeed feed) {
		this.list.removeAll();

		for (NewsSection section : feed.getSections()) {
			Color color = new Color(section.getColor());

			JPanel row = new TransparentPanel(new BorderLayout());
			row.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 0));
			row.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

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
			horBar.setUI(new FlatScrollbarUI(horBar));
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

	public class TransparentPanel extends JPanel {
		public TransparentPanel() {
			this(null);
		}

		public TransparentPanel(LayoutManager manager) {
			super(manager);

			this.setOpaque(false);
		}
		@Override
		public void paintComponent(Graphics g) {
			g.setColor(this.getBackground());
			g.fillRect(0, 0, this.getWidth(), this.getHeight());

			super.paintComponent(g);
		}
	}
}
