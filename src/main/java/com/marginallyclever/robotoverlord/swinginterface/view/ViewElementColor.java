package com.marginallyclever.robotoverlord.swinginterface.view;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.AbstractUndoableEdit;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.CollapsiblePanel;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.undoableedits.ColorRGBAEdit;
import com.marginallyclever.robotoverlord.uiexposedtypes.ColorEntity;

/**
 * Panel to alter a color parameter (four float values).
 * @author Dan Royer
 */
public class ViewElementColor extends ViewElement implements ChangeListener, PropertyChangeListener {
	@Serial
	private static final long serialVersionUID = 393949606034181281L;
	private final JSlider [] fields = new JSlider[4];
	private final ColorEntity colorEntity;
	
	public ViewElementColor(RobotOverlord ro,ColorEntity colorEntity) {
		super(ro);
		this.colorEntity = colorEntity;

		colorEntity.addPropertyChangeListener(this);
		CollapsiblePanel p = new CollapsiblePanel(colorEntity.getName());
		JPanel p2 = p.getContentPane();
		p2.setLayout(new GridBagLayout());
		
	    GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx=1;
		gbc.gridx=0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		//gbc.insets = new Insets(1,1,1,1);

		double [] oldValues = colorEntity.getDoubleArray();
		fields[0] = addField(oldValues[0],p2,"Red",gbc);
		fields[1] = addField(oldValues[1],p2,"Green",gbc);
		fields[2] = addField(oldValues[2],p2,"Blue",gbc);
		fields[3] = addField(oldValues[3],p2,"Alpha",gbc);

		this.setLayout(new BorderLayout());
		this.add(p,BorderLayout.CENTER);
	}
	
	private JSlider addField(double value,JPanel parent,String labelName,GridBagConstraints gbc) {
		JSlider field = new JSlider();
		field.setMaximum(255);
		field.setMinimum(0);
		field.setMinorTickSpacing(1);
		field.setValue((int)(value*255));
		field.addChangeListener(this);
		field.addFocusListener(this);

		JLabel label = new JLabel(labelName,JLabel.LEADING);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label,BorderLayout.LINE_START);
		panel.add(field,BorderLayout.LINE_END);
		
		parent.add(panel,gbc);
		
		return field;
	}
	
	private double getField(int i,double oldValue) {
		return (double)fields[i].getValue()/255.0;
	}

	/**
	 * entity changed, poke panel
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {		
		double [] newValues = colorEntity.getDoubleArray();
		
		for(int i=0;i<newValues.length;++i) {
			fields[i].setValue((int)(newValues[i]*255.0));
		}
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		double [] newValues = new double[fields.length];
		double [] oldValues = ((ColorEntity) colorEntity).getDoubleArray();
		
		float sum=0;
		for(int i=0;i<fields.length;++i) {
			newValues[i] = getField(i,oldValues[i]);
			sum += Math.abs( newValues[i] - oldValues[i] );
		}

		if(sum>1e-3) {
			AbstractUndoableEdit event = new ColorRGBAEdit(colorEntity,newValues);
			UndoSystem.addEvent(this,event);
		}
	}

	@Override
	public void setReadOnly(boolean arg0) {
		for(int i=0;i<fields.length;++i) {
			fields[i].setEnabled(!arg0);
		}
	}
}
