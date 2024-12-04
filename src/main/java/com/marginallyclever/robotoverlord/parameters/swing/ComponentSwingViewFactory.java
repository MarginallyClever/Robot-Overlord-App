package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.parameters.AbstractParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.parameters.StringParameter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * {@link ComponentSwingViewFactory} builds Swing elements and collects them in a {@link JPanel}.
 */
@Deprecated public class ComponentSwingViewFactory {
	private final ViewElementFactory viewElementFactory = new ViewElementFactory();
	private final JPanel result = new JPanel();
	private final GridBagConstraints gbc = new GridBagConstraints();

	public ComponentSwingViewFactory() {
		super();
		result.setLayout(new GridBagLayout());
		result.setBorder(new EmptyBorder(1, 1, 1, 1));

		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.set(1, 1, 1, 1);
	}
	
	private void addViewElement(ViewElement c) {
		gbc.gridy++;
		result.add(c,gbc);
	}

	/**
	 * Get the result of the build.
	 * @return JPanel the result
	 */
	public JPanel getResult() {
		return result;
	}
	
	/**
	 * Add an componentpanel element based on the parameter type.
	 * @param parameter the parameter to add
	 */
	public ViewElement add(AbstractParameter<?> parameter) {
		ViewElement element;

		try {
			element = viewElementFactory.add(parameter);
		} catch(InvalidParameterException e) {
			return addStaticText("ViewPanel.add("+ parameter.getClass() +")");
		}

		addViewElement(element);
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
	 * Add a control for an string that includes a filename selection dialog
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
