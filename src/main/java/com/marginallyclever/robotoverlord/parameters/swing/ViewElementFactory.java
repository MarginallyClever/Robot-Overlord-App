package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * A factory that translates an {@link AbstractParameter} into a {@link ViewElement}.
 * @author Dan Royer
 * @since 2.6.3
 */
public class ViewElementFactory {
	private final EntityManager entityManager;

	public ViewElementFactory(EntityManager entityManager) {
		super();
		this.entityManager = entityManager;
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
			throw new InvalidParameterException("unknown parameter "+parameter.getClass().toString());
		}

		return element;
	}
	

	public ViewElement addStaticText(String text) {
		ViewElement b = new ViewElement();
		b.add(new JLabel(text,JLabel.LEADING));
		return b;
	}

	/**
	 * Add a control for an integer that is bound to a combo box
	 * @param e the Parameter that holds the current value.
	 * @param labels the labels to use for the combo box
	 * @return the element
	 */
	public ViewElementComboBox addComboBox(IntParameter e, String [] labels) {
		return new ViewElementComboBox(e,labels);
	}

	/**
	 * Add a control for an integer that is bound between two values
	 * @param e the Parameter that holds the current value.
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElementSlider addRange(IntParameter e, int top, int bottom) {
		return new ViewElementSlider(e,top,bottom);
	}

	/**
	 * Add a control for an double that is bound between two values
	 * @param e the Parameter that holds the current value.
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElementSliderDouble addRange(DoubleParameter e, int top, int bottom) {
		return new ViewElementSliderDouble(e,top,bottom);
	}

	/**
	 * Add a control for a StringParameter that includes a filename selection dialog
	 * @param parameter the Parameter that holds the current value.
	 * @param filters
	 * @return the element
	 */
	public ViewElementFilename addFilename(StringParameter parameter, List<FileFilter> filters) {
		ViewElementFilename b = new ViewElementFilename(parameter);
		b.addFileFilters(filters);
		return b;
	}

	public ViewElementButton addButton(String string) {
		return new ViewElementButton(string);
	}
}
