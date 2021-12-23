package com.marginallyclever.robotOverlord.robots.robotArm.robotArmInterface.jogInterface;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.marginallyclever.convenience.log.Log;


public class Dial extends JComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int value=0;
	private int change=0;

	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();

	public Dial() {
		super();

		Dimension d = new Dimension(50,50);
		setMinimumSize(d);
		setPreferredSize(d);
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch(e.getID()) {
				case KeyEvent.VK_PLUS:
					setChange(1);
					setValue(value+1);
					break;
				case KeyEvent.VK_MINUS:
					setChange(-1);
					setValue(value-1);
					break;
				default: break;
				}
			}
		});
		addMouseWheelListener(new MouseAdapter() {
			@Override
		    public void mouseWheelMoved(MouseWheelEvent e) {
				int v = -e.getWheelRotation();
				setChange(v);
				setValue(value+v);
				notifyActionListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"turn"));
			}
		});
	}
	
	public int getChange() {
		return change;
	}

	public void setChange(int change) {
		this.change = change;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = (value+360)%360;
		repaint();
	}
	
	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyActionListeners(ActionEvent ae) {
		for( ActionListener a : listeners ) a.actionPerformed(ae);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Rectangle rect = this.getBounds();
		
		g.translate(rect.width/2, rect.height/2);
		int radius = Math.min(rect.width, rect.height) /2;
		Color old = g.getColor();

		drawEdge(g,radius);
		drawTurnIndicator(g,radius);
		drawLabels(g,radius);

		g.setColor(old);
	}
	
	private void drawLabels(Graphics g, int radius) {
		int inset = 4;
		int v = radius/5;
		int x = inset-radius;
		int y = -radius+v/2 + inset; 
		// -
		g.drawLine(x,y,x+v,y);
		// +
		x = radius-inset;
		g.drawLine(x-v,y,x,y);
		g.drawLine(x-v/2,-radius+0+inset,x-v/2,-radius+v+inset);
	}

	private void drawTurnIndicator(Graphics g, int radius) {
		radius-=6;
		double radians = Math.toRadians(value);
		int x=(int)Math.round(Math.cos(radians)*radius);
		int y=(int)Math.round(Math.sin(radians)*radius);
		
		g.setColor(Color.GRAY);
		g.drawLine(0,-2,x,y-2);
	}

	private void drawEdge(Graphics g,int r) {
		r-=3;
		
		// shadow
		int x=0;
		int y=-2;
		g.setColor(Color.DARK_GRAY);
		g.drawArc(x-r, y-r, x+r*2, y+r*2, 45, 180);
		g.setColor(Color.GRAY);
		g.drawArc(x-r, y-r, x+r*2, y+r*2, 180+45, 180);
		g.setColor(getBackground());
		// edge
		r-=3;
		g.setColor(Color.DARK_GRAY);
		g.drawArc(x-r, y-r, x+r*2, y+r*2, 180+45, 180);
		g.setColor(Color.LIGHT_GRAY);
		g.drawArc(x-r, y-r, x+r*2, y+r*2, 45, 180);
	}

	public static void main(String[] args) {
		Log.start();

        try {
        	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        	Log.error("Look and feel could not be set: "+e.getLocalizedMessage());
        }

		JFrame frame = new JFrame(Dial.class.getSimpleName());
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel p = new JPanel();
		frame.add(p);
		Dial dial = new Dial();        
		p.add(dial);
		dial.addActionListener((e)->{
			Log.message(e.getActionCommand()+":"+dial.getChange()+"="+dial.getValue());
		});
		
		p.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				Dimension d = new Dimension();
				p.getSize(d);
				dial.setPreferredSize(d);
			}
		});

		frame.pack();
		frame.setVisible(true);
	}
}
