package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import com.marginallyclever.robotOverlord.RobotOverlord;

/**
 * An element in the View
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElement extends Observable implements FocusListener {
	protected RobotOverlord ro;
	public JPanel panel = new JPanel();
	
	public ViewElement(RobotOverlord ro) {
		this.ro=ro;
	}
	
	public void setReadOnly(boolean arg0) {
		// an empty element is already read only.
	}

	@Override
	public void focusGained(FocusEvent e) {
		Component c = e.getComponent();

		
		// I need the absolute position of this component in the top-most component inside the JScrollPane
		// in order to call scrollRectToVisible() with the correct coordinates.
		Rectangle rec = c.getBounds();
		//System.out.println("START "+c.getClass().getName() + " >> "+rec.y);
		
		Container c0 = null;
		Container c1 = c.getParent();
		while( (c1!=null) && !(c1 instanceof JScrollPane) ) {
			Rectangle r2 = c1.getBounds();
			rec.x += r2.x;
			rec.y += r2.y;
			//System.out.println("\t"+c1.getClass().getName() + " REL "+r2.y+" ABS "+rec.y);
			c0 = c1;
			c1 = c1.getParent();
		}
		//System.out.println("\tFINAL "+c0.getClass().getName() + " >> "+rec.y);
		
		((JComponent)c0).scrollRectToVisible(rec);
	}

	@Override
	public void focusLost(FocusEvent e) {/*
		System.out.println("LOST "
					+e.getComponent().getClass().getName() + " >> "
					+e.getOppositeComponent().getClass().getName());//*/
		
	}
}
