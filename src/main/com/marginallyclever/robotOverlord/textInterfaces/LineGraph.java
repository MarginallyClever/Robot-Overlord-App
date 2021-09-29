package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;

public class LineGraph extends JPanel {
	private static final long serialVersionUID = 1L;
	private ArrayList<Double> fx;
	private Double yMin, yMax;
	private Color lineColor = Color.BLUE;
	
	public LineGraph(ArrayList<Double> list) {
		super();
		
		fx = list;
	}
	
	public LineGraph() {
		this(new ArrayList<Double>());
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Dimension size = getSize();
		drawBox(g,size);
		drawLine(g,size);
	}
	
	private void drawLine(Graphics g, Dimension size) {
		int entries = fx.size();
		if(entries==0) return;
		
		updateRange();
		
		int x0=0;
		Double y0d=fx.get(x0);
		int y0=scaleY(y0d,size);

		g.setColor(lineColor);
		for(int i=1;i<entries;++i) {
			Double y1d=fx.get(i);
			int y1=scaleY(y1d,size);
			int x1=(int)((double)size.width * (double)i/(double)entries);
			
			g.drawLine(x0,y0,x1,y1);
			
			x0=x1;
			y0=y1;
		}
	}

	private int scaleY(Double y1d,Dimension size) {
		double ratio = (y1d-yMin) / (yMax-yMin);
		return (int)(size.height * ratio);
	}

	private void drawBox(Graphics g, Dimension size) {
		int w=size.width;
		int h=size.height;
		g.setColor(Color.GRAY);
		g.drawLine(0,0,w,0);
		g.drawLine(w,0,w,h);
		g.drawLine(w,h,0,h);
		g.drawLine(0,h,0,0);
	}

	public ArrayList<Double> getDataModel() {
		return fx;
	}
	
	public void updateRange() {
		yMin = Double.MAX_VALUE;
		yMax = -Double.MAX_VALUE;
		
		for(int i=0;i<fx.size();++i) {
			Double d = fx.get(i);
			if(yMin>d) yMin=d;
			if(yMax<d) yMax=d;
		}
	}

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		ArrayList<Double> list = new ArrayList<Double>();
		for(int i=0;i<250;++i) {
			list.add(Math.random()*500);
		}

		JFrame frame = new JFrame("LineGraph");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new LineGraph(list));
		frame.pack();
		frame.setVisible(true);
	}

	
	public Color getLineColor() {
		return lineColor;
	}

	
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}
}
