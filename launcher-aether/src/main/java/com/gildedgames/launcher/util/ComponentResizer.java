package com.gildedgames.launcher.util;

import lombok.Getter;
import lombok.Setter;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * The ComponentResizer allows you to resize a component by dragging a border
 * of the component.
 */
public class ComponentResizer extends MouseAdapter {
	private final static Dimension MINIMUM_SIZE = new Dimension(10, 10);
	private final static Dimension MAXIMUM_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

	private static Map<Integer, Integer> cursors = new HashMap<Integer, Integer>();

	{
		cursors.put(1, Cursor.N_RESIZE_CURSOR);
		cursors.put(2, Cursor.W_RESIZE_CURSOR);
		cursors.put(4, Cursor.S_RESIZE_CURSOR);
		cursors.put(8, Cursor.E_RESIZE_CURSOR);
		cursors.put(3, Cursor.NW_RESIZE_CURSOR);
		cursors.put(9, Cursor.NE_RESIZE_CURSOR);
		cursors.put(6, Cursor.SW_RESIZE_CURSOR);
		cursors.put(12, Cursor.SE_RESIZE_CURSOR);
	}

	private Insets dragInsets;
	private Dimension snapSize;

	private int direction;
	protected static final int NORTH = 1;
	protected static final int WEST = 2;
	protected static final int SOUTH = 4;
	protected static final int EAST = 8;

	private Cursor sourceCursor;
	private boolean resizing;
	private Rectangle bounds;
	private Point pressed;
	private boolean autoscrolls;

	private Dimension minimumSize = MINIMUM_SIZE;
	private Dimension maximumSize = MAXIMUM_SIZE;

	@Getter
	private boolean disabled = false;

	/**
	 * Convenience contructor. All borders are resizable in increments of
	 * a single pixel. Components must be registered separately.
	 */
	public ComponentResizer() {
		this(new Insets(5, 5, 5, 5), new Dimension(1, 1));
	}

	/**
	 * Convenience contructor. All borders are resizable in increments of
	 * a single pixel. Components can be registered when the class is created
	 * or they can be registered separately afterwards.
	 *
	 * @param components components to be automatically registered
	 */
	public ComponentResizer(Component... components) {
		this(new Insets(5, 5, 5, 5), new Dimension(1, 1), components);
	}

	/**
	 * Convenience contructor. Eligible borders are resisable in increments of
	 * a single pixel. Components can be registered when the class is created
	 * or they can be registered separately afterwards.
	 *
	 * @param dragInsets Insets specifying which borders are eligible to be
	 *                   resized.
	 * @param components components to be automatically registered
	 */
	public ComponentResizer(Insets dragInsets, Component... components) {
		this(dragInsets, new Dimension(1, 1), components);
	}

	/**
	 * Create a ComponentResizer.
	 *
	 * @param dragInsets Insets specifying which borders are eligible to be
	 *                   resized.
	 * @param snapSize   Specify the dimension to which the border will snap to
	 *                   when being dragged. Snapping occurs at the halfway mark.
	 * @param components components to be automatically registered
	 */
	public ComponentResizer(Insets dragInsets, Dimension snapSize, Component... components) {
		this.setDragInsets(dragInsets);
		this.setSnapSize(snapSize);
		this.registerComponent(components);
	}

	public void setDisabled(boolean val) {
		if (!val) {
			this.resizing = false;
		}

		this.disabled = val;
	}
	/**
	 * Get the drag insets
	 *
	 * @return the drag insets
	 */
	public Insets getDragInsets() {
		return this.dragInsets;
	}

	/**
	 * Set the drag dragInsets. The insets specify an area where mouseDragged
	 * events are recognized from the edge of the border inwards. A value of
	 * 0 for any size will imply that the border is not resizable. Otherwise
	 * the appropriate drag cursor will appear when the mouse is inside the
	 * resizable border area.
	 *
	 * @param dragInsets Insets to control which borders are resizeable.
	 */
	public void setDragInsets(Insets dragInsets) {
		this.validateMinimumAndInsets(this.minimumSize, dragInsets);

		this.dragInsets = dragInsets;
	}

	/**
	 * Get the components maximum size.
	 *
	 * @return the maximum size
	 */
	public Dimension getMaximumSize() {
		return this.maximumSize;
	}

	/**
	 * Specify the maximum size for the component. The component will still
	 * be constrained by the size of its parent.
	 *
	 * @param maximumSize the maximum size for a component.
	 */
	public void setMaximumSize(Dimension maximumSize) {
		this.maximumSize = maximumSize;
	}

	/**
	 * Get the components minimum size.
	 *
	 * @return the minimum size
	 */
	public Dimension getMinimumSize() {
		return this.minimumSize;
	}

	/**
	 * Specify the minimum size for the component. The minimum size is
	 * constrained by the drag insets.
	 *
	 * @param minimumSize the minimum size for a component.
	 */
	public void setMinimumSize(Dimension minimumSize) {
		this.validateMinimumAndInsets(minimumSize, this.dragInsets);

		this.minimumSize = minimumSize;
	}

	/**
	 * Remove listeners from the specified component
	 *
	 * @param components the component the listeners are removed from
	 */
	public void deregisterComponent(Component... components) {
		for (Component component : components) {
			component.removeMouseListener(this);
			component.removeMouseMotionListener(this);
		}
	}

	/**
	 * Add the required listeners to the specified component
	 *
	 * @param components the component the listeners are added to
	 */
	public void registerComponent(Component... components) {
		for (Component component : components) {
			component.addMouseListener(this);
			component.addMouseMotionListener(this);
		}
	}

	/**
	 * Get the snap size.
	 *
	 * @return the snap size.
	 */
	public Dimension getSnapSize() {
		return this.snapSize;
	}

	/**
	 * Control how many pixels a border must be dragged before the size of
	 * the component is changed. The border will snap to the size once
	 * dragging has passed the halfway mark.
	 *
	 * @param snapSize Dimension object allows you to separately spcify a
	 *                 horizontal and vertical snap size.
	 */
	public void setSnapSize(Dimension snapSize) {
		this.snapSize = snapSize;
	}

	/**
	 * When the components minimum size is less than the drag insets then
	 * we can't determine which border should be resized so we need to
	 * prevent this from happening.
	 */
	private void validateMinimumAndInsets(Dimension minimum, Insets drag) {
		int minimumWidth = drag.left + drag.right;
		int minimumHeight = drag.top + drag.bottom;

		if (minimum.width < minimumWidth
				|| minimum.height < minimumHeight) {
			String message = "Minimum size cannot be less than drag insets";
			throw new IllegalArgumentException(message);
		}
	}

	/**
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		Component source = e.getComponent();

		if (this.disabled) {
			source.setCursor(this.sourceCursor);

			return;
		}

		Point location = e.getPoint();
		this.direction = 0;

		if (location.x < this.dragInsets.left)
			this.direction += WEST;

		if (location.x > source.getWidth() - this.dragInsets.right - 1)
			this.direction += EAST;

		if (location.y < this.dragInsets.top)
			this.direction += NORTH;

		if (location.y > source.getHeight() - this.dragInsets.bottom - 1)
			this.direction += SOUTH;

		//  Mouse is no longer over a resizable border
		if (this.direction == 0 || this.direction == WEST || this.direction == NORTH
				|| this.direction == NORTH + EAST || this.direction == NORTH + WEST || this.direction == SOUTH + WEST) {
			source.setCursor(this.sourceCursor);
		} else  // use the appropriate resizable cursor
		{
			int cursorType = cursors.get(this.direction);
			Cursor cursor = Cursor.getPredefinedCursor(cursorType);
			source.setCursor(cursor);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (!this.resizing) {
			Component source = e.getComponent();
			this.sourceCursor = source.getCursor();
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (!this.resizing) {
			Component source = e.getComponent();
			source.setCursor(this.sourceCursor);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (this.disabled) {
			return;
		}

		//	The mouseMoved event continually updates this variable

		if (this.direction == 0) return;

		//  Setup for resizing. All future dragging calculations are done based
		//  on the original bounds of the component and mouse pressed location.

		this.resizing = true;

		Component source = e.getComponent();
		this.pressed = e.getPoint();
		SwingUtilities.convertPointToScreen(this.pressed, source);
		this.bounds = source.getBounds();

		//  Making sure autoscrolls is false will allow for smoother resizing
		//  of components

		if (source instanceof JComponent) {
			JComponent jc = (JComponent) source;
			this.autoscrolls = jc.getAutoscrolls();
			jc.setAutoscrolls(false);
		}
	}

	/**
	 * Restore the original state of the Component
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (this.disabled) {
			return;
		}

		this.resizing = false;

		Component source = e.getComponent();
		source.setCursor(this.sourceCursor);

		if (source instanceof JComponent) {
			((JComponent) source).setAutoscrolls(this.autoscrolls);
		}
	}

	/**
	 * Resize the component ensuring location and size is within the bounds
	 * of the parent container and that the size is within the minimum and
	 * maximum constraints.
	 * <p>
	 * All calculations are done using the bounds of the component when the
	 * resizing started.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (this.resizing == false || this.disabled) return;

		Component source = e.getComponent();
		Point dragged = e.getPoint();
		SwingUtilities.convertPointToScreen(dragged, source);

		this.changeBounds(source, this.direction, this.bounds, this.pressed, dragged);
	}

	protected void changeBounds(Component source, int direction, Rectangle bounds, Point pressed, Point current) {
		//  Start with original locaton and size

		int x = bounds.x;
		int y = bounds.y;
		int width = bounds.width;
		int height = bounds.height;

		//  Resizing the West or North border affects the size and location

		if (WEST == (direction & WEST)) {
			int drag = this.getDragDistance(pressed.x, current.x, this.snapSize.width);
			int maximum = Math.min(width + x, this.maximumSize.width);
			drag = this.getDragBounded(drag, this.snapSize.width, width, this.minimumSize.width, maximum);

			x -= drag;
			width += drag;
		}

		if (NORTH == (direction & NORTH)) {
			int drag = this.getDragDistance(pressed.y, current.y, this.snapSize.height);
			int maximum = Math.min(height + y, this.maximumSize.height);
			drag = this.getDragBounded(drag, this.snapSize.height, height, this.minimumSize.height, maximum);

			y -= drag;
			height += drag;
		}

		//  Resizing the East or South border only affects the size

		if (EAST == (direction & EAST)) {
			int drag = this.getDragDistance(current.x, pressed.x, this.snapSize.width);
			Dimension boundingSize = this.getBoundingSize(source);
			int maximum = Math.min(boundingSize.width - x, this.maximumSize.width);
			drag = this.getDragBounded(drag, this.snapSize.width, width, this.minimumSize.width, maximum);
			width += drag;
		}

		if (SOUTH == (direction & SOUTH)) {
			int drag = this.getDragDistance(current.y, pressed.y, this.snapSize.height);
			Dimension boundingSize = this.getBoundingSize(source);
			int maximum = Math.min(boundingSize.height - y, this.maximumSize.height);
			drag = this.getDragBounded(drag, this.snapSize.height, height, this.minimumSize.height, maximum);
			height += drag;
		}

		source.setBounds(x, y, width, height);
		source.validate();
	}

	/*
	 *  Determine how far the mouse has moved from where dragging started
	 */
	private int getDragDistance(int larger, int smaller, int snapSize) {
		int halfway = snapSize / 2;
		int drag = larger - smaller;
		drag += (drag < 0) ? -halfway : halfway;
		drag = (drag / snapSize) * snapSize;

		return drag;
	}

	/*
	 *  Adjust the drag value to be within the minimum and maximum range.
	 */
	private int getDragBounded(int drag, int snapSize, int dimension, int minimum, int maximum) {
		while (dimension + drag < minimum)
			drag += snapSize;

		while (dimension + drag > maximum)
			drag -= snapSize;


		return drag;
	}

	/*
	 *  Keep the size of the component within the bounds of its parent.
	 */
	private Dimension getBoundingSize(Component source) {
		if (source instanceof Window) {
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Rectangle bounds = env.getMaximumWindowBounds();
			return new Dimension(bounds.width, bounds.height);
		} else {
			return source.getParent().getSize();
		}
	}
}
