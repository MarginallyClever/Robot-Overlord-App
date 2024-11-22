package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.StringParameter;

import javax.swing.filechooser.FileFilter;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * A factory that translates an {@link AbstractParameter} into a {@link ViewElement}.
 */
@Deprecated public class ViewElementFactory {
	public ViewElementFactory() {
		super();
	}

	/**
	 * Add an componentpanel element based on the parameter type.
	 * @param parameter the parameter to add
	 */
	public ViewElement add(AbstractParameter<?> parameter) {
		ViewElement element=null;
		
		//logger.debug("Add "+e.getClass().toString());

		if(null==element) {
			throw new InvalidParameterException("unknown parameter "+ parameter.getClass());
		}

		return element;
	}
	

	public ViewElement addStaticText(String text) {
		return null;
	}

	/**
	 * Add a control for an integer that is bound to a combo box
	 * @param e the Parameter that holds the current value.
	 * @param labels the labels to use for the combo box
	 * @return the element
	 */
	public ViewElement addComboBox(IntParameter e, String [] labels) {
		return null;
	}

	/**
	 * Add a control for an integer that is bound between two values
	 * @param e the Parameter that holds the current value.
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElement addRange(IntParameter e, int top, int bottom) {
		return null;
	}

	/**
	 * Add a control for an double that is bound between two values
	 * @param e the Parameter that holds the current value.
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElement addRange(DoubleParameter e, int top, int bottom) {
		return null;
	}

	/**
	 * Add a control for a StringParameter that includes a filename selection dialog
	 * @param parameter the Parameter that holds the current value.
	 * @param filters
	 * @return the element
	 */
	public ViewElement addFilename(StringParameter parameter, List<FileFilter> filters) {
		return null;
	}

	public ViewElement addButton(String string) {
		return null;
	}
}
