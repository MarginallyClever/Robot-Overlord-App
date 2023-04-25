package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * An element in the View
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewElement extends JComponent implements FocusListener {
	public ViewElement() {
		super();
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
		//logger.info("START "+c.getClass().getName() + " >> "+rec.y);
		
		Container c0 = null;
		Container c1 = c.getParent();
		while( (c1!=null) && !(c1 instanceof JScrollPane) ) {
			Rectangle r2 = c1.getBounds();
			rec.x += r2.x;
			rec.y += r2.y;
			//logger.info("\t"+c1.getClass().getName() + " REL "+r2.y+" ABS "+rec.y);
			c0 = c1;
			c1 = c1.getParent();
		}
		//logger.info("\tFINAL "+c0.getClass().getName() + " >> "+rec.y);

		assert(c0 != null);
		((JComponent)c0).scrollRectToVisible(rec);
	}

	@Override
	public void focusLost(FocusEvent e) {/*
		logger.info("LOST "
					+e.getComponent().getClass().getName() + " >> "
					+e.getOppositeComponent().getClass().getName());//*/
	}
}
