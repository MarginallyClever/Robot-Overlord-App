package com.marginallyclever.convenience.swing;

import com.marginallyclever.convenience.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;


/**
 * A dial that can be turned with the mouse wheel, mouse click+drag, or the keyboard +/- keys.
 * @author Dan Royer
 */
public class Dial extends JComponent {
	private static final Logger logger = LoggerFactory.getLogger(Dial.class);
	private double value=0;
	private double change=0;

	private boolean dragging=false;
	private int dragPreviousX,dragPreviousY;

	private final ArrayList<ActionListener> listeners = new ArrayList<>();

	public Dial() {
		super();

		Dimension d = new Dimension(50,50);
		setMinimumSize(d);
		setPreferredSize(d);
		
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				switch (e.getID()) {
					case KeyEvent.VK_PLUS -> {
						onChange(1);
					}
					case KeyEvent.VK_MINUS -> {
						onChange(-1);
					}
					default -> {}
				}
			}
		});

		addMouseWheelListener((MouseWheelEvent e)->{
			onChange(-e.getWheelRotation());
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dragging=true;
				setPrevious(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dragging=false;
			}
		});

		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if(!dragging) return;

				Vector2d center = new Vector2d(getWidth()/2.0,getHeight()/2.0);

				// find the current mouse position relative to the center of the dial.
				Vector2d delta = new Vector2d(e.getX(),e.getY());
				delta.sub(center);
				delta.normalize();

				// find the previous mouse position relative to the center of the dial.
				Vector2d previous = new Vector2d(dragPreviousX,dragPreviousY);
				previous.sub(center);
				previous.normalize();

				// find the orthogonal vector to the previous vector
				Vector2d ortho = new Vector2d(-previous.y,previous.x);

				// dot product of delta and ortho is the change in angle.
				double y = delta.dot(ortho);
				double x = delta.dot(previous);
				double change = Math.toDegrees(Math.atan2(y,x));

				if(change!=0) onChange(change);

				// remember the mouse moved.
				setPrevious(e);
			}
		});
	}

	private void onChange(double amount) {
		setChange(amount);
		notifyActionListeners(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"turn"));
	}

	private void setPrevious(MouseEvent e) {
		dragPreviousX = e.getX();
		dragPreviousY = e.getY();
	}
	
	public double getChange() {
		return change;
	}

	/**
	 * Set the change value.  The change value is the amount the dial moved on the last update.
	 * @param change the change value
	 */
	public void setChange(double change) {
		setValue(value+change);
	}

	/**
	 * Returns the current value of the dial.
	 * @return the current value of the dial, a value between 0 (inclusive) and 360 (exclusive).
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Set the value of the dial.  The value is clamped to 0..360.  Does not alter the results of getChange().
	 * @param arg0 the new value
	 */
	public void setValue(double arg0) {
		this.change = arg0-this.value;
		this.value = (arg0+360)%360;
		repaint();
	}

	/**
	 * Subscribe to receivei the "turn" command when the dial is turned.
	 * @param listener the listener
	 */
	public void addActionListener(ActionListener listener) {
		listeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyActionListeners(ActionEvent ae) {
		for( ActionListener listener : listeners ) listener.actionPerformed(ae);
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
		g.drawLine(x-v/2, -radius + inset,x-v/2,-radius+v+inset);
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
        	logger.error("Look and feel could not be set: "+e.getMessage());
        }

		JFrame frame = new JFrame(Dial.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel p = new JPanel();
		frame.add(p);
		Dial dial = new Dial();        
		p.add(dial);
		dial.addActionListener((e)->{
			logger.info(e.getActionCommand()+":"+dial.getChange()+"="+dial.getValue());
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
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
