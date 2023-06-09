package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.*;
import com.marginallyclever.robotoverlord.parameters.swing.*;

import java.util.List;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

/**
 * A factory that builds Swing elements for the entity editor
 * @author Dan Royer
 * @since 1.6.0
 */
public class ViewPanelFactory {
	private final JPanel innerPanel = new JPanel();
	private final GridBagConstraints gbc = new GridBagConstraints();
	private final EntityManager entityManager;

	public ViewPanelFactory(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;

		innerPanel.setLayout(new GridBagLayout());
		innerPanel.setBorder(new EmptyBorder(1, 1, 1, 1));

		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.set(1, 1, 1, 1);
	}
	
	private void addViewElement(ViewElement c) {
		gbc.gridy++;
		innerPanel.add(c,gbc);
	}

	public JComponent getFinalView() {
		return innerPanel;
	}
	
	/**
	 * Add an componentpanel element based on the parameter type.
	 * @param parameter the parameter to add
	 */
	public ViewElement add(AbstractParameter<?> parameter) {
		ViewElement element=null;
		
		//logger.debug("Add "+e.getClass().toString());
		
			 if(parameter instanceof BooleanParameter  ) element = new ViewElementBoolean((BooleanParameter)parameter);
		else if(parameter instanceof ColorParameter    ) element = new ViewElementColor((ColorParameter)parameter);
		else if(parameter instanceof DoubleParameter   ) element = new ViewElementDouble   ((DoubleParameter)parameter);
		else if(parameter instanceof IntParameter      ) element = new ViewElementInt      ((IntParameter)parameter);
		else if(parameter instanceof Vector3DParameter ) element = new ViewElementVector3d ((Vector3DParameter)parameter);
		else if(parameter instanceof ReferenceParameter) element = new ViewElementReference((ReferenceParameter)parameter, entityManager);
		else if(parameter instanceof StringParameter   ) element = new ViewElementString   ((StringParameter)parameter);
		else if(parameter instanceof ListParameter     ) element = new ViewElementList((ListParameter<?>)parameter, entityManager);

		if(null==element) {
			return addStaticText("ViewPanel.add("+parameter.getClass().toString()+")");
		}

		addViewElement(element);
		return element;
	}
	

	public ViewElement addStaticText(String text) {
		ViewElement b = new ViewElement();
		b.add(new JLabel(text,JLabel.LEADING));
		addViewElement(b);
		return b;
	}

	/**
	 * Add a control for an integer that is bound to a combo box
	 * @param e the Parameter that holds the current value.
	 * @param labels the labels to use for the combo box
	 * @return the element
	 */
	public ViewElement addComboBox(IntParameter e, String [] labels) {
		ViewElement b = new ViewElementComboBox(e,labels);
		addViewElement(b);
		return b;
		
	}

	/**
	 * Add a control for an integer that is bound between two values
	 * @param e the Parameter that holds the current value.
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElement addRange(IntParameter e, int top, int bottom) {
		ViewElement b = new ViewElementSlider(e,top,bottom);
		addViewElement(b);
		return b;
	}

	/**
	 * Add a control for an double that is bound between two values
	 * @param e the Parameter that holds the current value.
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElement addRange(DoubleParameter e, int top, int bottom) {
		ViewElement b = new ViewElementSliderDouble(e,top,bottom);
		addViewElement(b);
		return b;
	}

	/**
	 * Add a control for an string that includes a filename selection dialog
	 * @param parameter the Parameter that holds the current value.
	 * @param filters
	 * @return the element
	 */
	public ViewElement addFilename(StringParameter parameter, List<FileFilter> filters) {
		ViewElementFilename b = new ViewElementFilename(parameter);
		b.addFileFilters(filters);
		addViewElement(b);
		return b;
	}

	public ViewElementButton addButton(String string) {
		ViewElementButton b = new ViewElementButton(string);
		addViewElement(b);
		return b;
	}
}
