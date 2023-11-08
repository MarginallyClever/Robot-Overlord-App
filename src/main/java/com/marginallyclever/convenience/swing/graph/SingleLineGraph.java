package com.marginallyclever.convenience.swing.graph;

import com.marginallyclever.convenience.helpers.StringHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple line graph.  Assumes at most one y value per x value.  Interpolates between given values.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class SingleLineGraph extends JPanel {
	private final TreeMap<Double, Double> data = new TreeMap<>();
	private double yMin, yMax, xMin, xMax;
	private Color majorLineColor = new Color(0.8f,0.8f,0.8f);
	private Color minorLineColor = new Color(0.9f,0.9f,0.9f);
	private int gridSpacingX = 10;
	private int gridSpacingY = 10;
	private boolean mouseIn = false;
	private double mouseX, mouseY;

	public SingleLineGraph() {
		super();
		setBackground(Color.WHITE);

		final JPanel me = this;
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				mouseIn = true;
				updateToolTip(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouseIn = false;
				setToolTipText("");
				me.repaint();
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				me.repaint();
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				updateToolTip(e);
			}
		});
	}

	public void updateToolTip(MouseEvent event) {
		if(data.isEmpty()) return;

		double mx = event.getX();
		double scaledX = mx / getWidth();
		mouseX = xMin + (xMax - xMin) * scaledX;
		mouseY = getYatX(mouseX);
		setToolTipText( mouseX+"="+ StringHelper.formatDouble(mouseY) );
	}

	private double getYatX(double x) {
		// Find the two keys k1 and k2 such that k1 <= x <= k2
		Double k1 = data.floorKey(x);
		Double k2 = data.ceilingKey(x);

		// If x matches a key exactly, return the corresponding value
		if (k1.equals(k2)) {
			return data.get(k1);
		}

		// Perform linear interpolation
		double y1 = data.get(k1);
		double y2 = data.get(k2);
		return y1 + (x - k1) * (y2 - y1) / (k2 - k1);
	}

	public void addValue(double x,double y) {
		data.put(x,y);
	}

	public void removeValue(double x) {
		data.remove(x);
	}

	public void clear() {
		data.clear();
	}

	/**
	 * Set the bounds of the graph.  This limits the range of values that can be displayed.
	 * Data outside this range will not be drawn, but will still be stored.
	 * @param xMin the minimum x value
	 * @param xMax the maximum x value
	 * @param yMin the minimum y value
	 * @param yMax the maximum y value
	 */
	public void setBounds(double xMin,double xMax,double yMin,double yMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
	}
	public void setYMin(double yMin) {
		this.yMin = yMin;
	}
	public void setYMax(double yMax) {
		this.yMax = yMax;
	}
	public void setXMin(double xMin) {
		this.xMin = xMin;
	}
	public void setXMax(double xMax) {
		this.xMax = xMax;
	}

	public void setBoundsToData() {
		if(data.isEmpty()) return;

		double [] bounds = getDataBounds();

		xMin = bounds[0];
		xMax = bounds[1];
		yMin = bounds[2];
		yMax = bounds[3];
	}

	/**
	 *
	 * @return minx,maxx,miny,maxy
	 */
	public double [] getDataBounds() {
		if(data.isEmpty()) return new double[] {0,0,0,0};

		List<Double> x = new ArrayList<>(data.keySet());
		List<Double> y = new ArrayList<>(data.values());
		double minX = x.get(0);
		double maxX = x.get(0);
		double minY = y.get(0);
		double maxY = y.get(0);
		for(int i=1;i<x.size();++i) {
			if(x.get(i)<minX) minX = x.get(i);
			if(x.get(i)>maxX) maxX = x.get(i);
			if(y.get(i)<minY) minY = y.get(i);
			if(y.get(i)>maxY) maxY = y.get(i);
		}

		return new double[] {minX,maxX,minY,maxY};
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		super.paintComponent(g2d);
		drawGrid(g2d);
		if(mouseIn) drawYatX1(g2d);
		drawGraphLine(g2d);
		if(mouseIn) drawYatX2(g2d);
	}

	private void drawGrid(Graphics g) {
		g.setColor(minorLineColor);

		int width = getWidth();
		int height = getHeight();

		double [] bounds = getDataBounds();
		double minX = bounds[0];
		double maxX = bounds[1];
		double minY = bounds[2];
		double maxY = bounds[3];

		// draw vertical lines
		double left = Math.floor(minX / gridSpacingX) * gridSpacingX;
		for (double x = left; x <= maxX; x += gridSpacingX) {
			int x1 = transformX(x);
			g.drawLine(x1, 0, x1, height);
		}

		// draw horizontal lines
		double bottom = Math.floor(minY / gridSpacingY) * gridSpacingY;
		for (double y = bottom; y <= maxY; y += gridSpacingY) {
			int y1 = transformY(y);
			g.drawLine(0, y1, width, y1);
		}
	}

	private void drawGraphLine(Graphics g) {
		if(data.isEmpty()) return;

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		g.setColor(getForeground());
		int height = getHeight();

		Map.Entry<Double, Double> prevEntry = data.firstEntry();
		int prevX = transformX(prevEntry.getKey());
		int prevY = transformY(prevEntry.getValue());
		for (Map.Entry<Double, Double> entry : data.entrySet()) {
			int currentX = transformX(entry.getKey());
			int currentY = transformY(entry.getValue());
			g.drawLine(prevX, height - prevY, currentX, height - currentY);
			prevX = currentX;
			prevY = currentY;
		}
	}

	private void drawYatX1(Graphics g) {
		if (data.isEmpty()) return;

		g.setColor(minorLineColor);
		int currentX = transformX(mouseX);
		int y0 = transformY(yMin);
		int y1 = transformY(yMax);
		g.drawLine(currentX, y0, currentX, y1);
	}

	private void drawYatX2(Graphics g) {
		if(data.isEmpty()) return;

		int currentX = transformX(mouseX);
		int height = getHeight();
		g.setColor(getForeground());
		int y0 = transformY(mouseY);

		int v = 2;
		g.drawLine(currentX-v,height - y0-v,currentX+v,height - y0-v);
		g.drawLine(currentX+v,height - y0-v,currentX+v,height - y0+v);
		g.drawLine(currentX+v,height - y0+v,currentX-v,height - y0+v);
		g.drawLine(currentX-v,height - y0+v,currentX-v,height - y0-v);
	}

	private int transformX(double x) {
		return (int) (((x - xMin) / (xMax - xMin)) * getWidth());
	}

	private int transformY(double y) {
		return (int) (((y - yMin) / (yMax - yMin)) * getHeight());
	}

	public Color getMajorLineColor() {
		return majorLineColor;
	}

	public void setMajorLineColor(Color majorLineColor) {
		this.majorLineColor = majorLineColor;
	}

	public Color getMinorLineColor() {
		return minorLineColor;
	}

	public void setMinorLineColor(Color minorLineColor) {
		this.minorLineColor = minorLineColor;
	}

	public int getGridSpacingX() {
		return gridSpacingX;
	}

	public void setGridSpacingX(int gridSpacingX) {
		this.gridSpacingX = gridSpacingX;
	}

	public int getGridSpacingY() {
		return gridSpacingY;
	}

	public void setGridSpacingY(int gridSpacingY) {
		this.gridSpacingY = gridSpacingY;
	}
}
