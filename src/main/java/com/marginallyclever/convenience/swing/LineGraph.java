package com.marginallyclever.convenience.swing;

import com.marginallyclever.convenience.log.Log;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple line graph.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class LineGraph extends JPanel {
	private final TreeMap<Double, Double> data = new TreeMap<>();
	private double yMin, yMax, xMin, xMax;
	
	public LineGraph() {
		super();
	}

	public void add(double x,double y) {
		data.put(x,y);
	}

	public void remove(double x) {
		data.remove(x);
	}

	public void clear() {
		data.clear();
	}

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

		List<Double> x = new ArrayList<>(data.keySet());
		List<Double> y = new ArrayList<>(data.values());
		xMin = x.get(0);
		xMax = x.get(0);
		yMin = y.get(0);
		yMax = y.get(0);
		for(int i=1;i<x.size();++i) {
			if(x.get(i)<xMin) xMin = x.get(i);
			if(x.get(i)>xMax) xMax = x.get(i);
			if(y.get(i)<yMin) yMin = y.get(i);
			if(y.get(i)>yMax) yMax = y.get(i);
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(data.isEmpty()) return;

		g.setColor(getForeground());
		int width = getWidth();
		int height = getHeight();

		Map.Entry<Double, Double> prevEntry = data.firstEntry();
		int prevX = (int) (((prevEntry.getKey() - xMin) / (xMax - xMin)) * width);
		int prevY = (int) (((prevEntry.getValue() - yMin) / (yMax - yMin)) * height);
		for (Map.Entry<Double, Double> entry : data.entrySet()) {
			int currentX = (int) (((entry.getKey() - xMin) / (xMax - xMin)) * width);
			int currentY = (int) (((entry.getValue() - yMin) / (yMax - yMin)) * height);
			g.drawLine(prevX, height - prevY, currentX, height - currentY);
			prevX = currentX;
			prevY = currentY;
		}
	}

	// TEST 
	
	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}

		LineGraph graph = new LineGraph();
		for(int i=0;i<250;++i) {
			graph.add(i,Math.random()*500);
		}
		graph.setBoundsToData();
		graph.setBorder(new BevelBorder(BevelBorder.LOWERED));

		JFrame frame = new JFrame("LineGraph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800,400));
		frame.setContentPane(graph);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
