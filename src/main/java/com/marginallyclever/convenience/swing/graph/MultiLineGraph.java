package com.marginallyclever.convenience.swing.graph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * A simple line graph.  Assumes at most one y value per x value.  Interpolates between given values.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class MultiLineGraph extends JPanel {
	private GraphModel model = new GraphModel();
	private double yMin, yMax, xMin, xMax;
	private Color majorLineColor = new Color(0.8f,0.8f,0.8f);
	private Color minorLineColor = new Color(0.9f,0.9f,0.9f);
	private int gridSpacingX = 10;
	private int gridSpacingY = 10;

	public MultiLineGraph() {
		super();
		setBackground(Color.WHITE);
	}

	public void setModel(GraphModel model) {
		this.model = model;
	}

	public GraphModel getModel() {
		return model;
	}

	/**
	 * Set the visible range of the graph.  Values outside this range will not be drawn.
	 * @param range the minimum values and size of the range.
	 */
	public void setRange(Rectangle2D.Double range) {
		this.xMin = range.getMinX();
		this.xMax = range.getMaxX();
		this.yMin = range.getMinY();
		this.yMax = range.getMaxY();
	}

	public Rectangle2D.Double getRange() {
		return new Rectangle2D.Double(xMin,yMin,xMax-xMin,yMax-yMin);
	}

	/**
	 * Set the visible range of the graph to match the data in the model.
	 */
	public void setRangeToModel() {
		if(model.isEmpty()) return;

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
		if(model.isEmpty()) return new double[] {0,0,0,0};

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for(GraphLine line : model.getLines()) {
			List<Point2D> points = line.getPoints();
			for(Point2D point : points) {
				if(point.getX()<minX) minX = point.getX();
				if(point.getX()>maxX) maxX = point.getX();
				if(point.getY()<minY) minY = point.getY();
				if(point.getY()>maxY) maxY = point.getY();
			}
		}

		return new double[] {minX,maxX,minY,maxY};
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		super.paintComponent(g2d);
		drawGrid(g2d);
		drawGraphLines(g2d);
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

	private void drawGraphLines(Graphics g) {
		if(model.isEmpty()) return;

		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int numLines = model.getLineCount();
		List<String> names = model.getLineNames();
		for(int i=0;i<numLines;++i) {
			drawGraphLine(g,model.getLine(names.get(i)));
		}
	}

	private void drawGraphLine(Graphics g,GraphLine line) {
		if(model.isEmpty()) return;

		g.setColor(line.getColor());
		int height = getHeight();

		List<Point2D> points = line.getPoints();
		int prevX = transformX(points.get(0).getX());
		int prevY = transformY(points.get(0).getY());
		for(Point2D point : points) {
			int currentX = transformX(point.getX());
			int currentY = transformY(point.getY());
			g.drawLine(prevX, height - prevY, currentX, height - currentY);
			prevX = currentX;
			prevY = currentY;
		}
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

	/**
	 * Set colors for each line in the graph based on the number of lines and the color wheel.
	 */
	public void assignQualitativeColors() {
		int count = model.getLineCount();
		GraphLine[] lines = model.getLines();
		// use RGB color wheel to generate 'count' colors
		for(int i=0;i<count;++i) {
			float hue = (float)i/count;
			Color color = Color.getHSBColor(hue,1,1);
			lines[i].setColor(color);
		}
	}
}
