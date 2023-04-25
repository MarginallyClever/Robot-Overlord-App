package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.parameters.*;
import com.marginallyclever.robotoverlord.swinginterface.CollapsiblePanel;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentCopyAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentDeleteAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentPasteAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.ArrayList;

/**
 * A factory that builds Swing elements for the entity editor
 * @author Dan Royer
 * @since 1.6.0
 */
public class ComponentPanelFactory extends ViewElement {
	public JComponent panelBeingBuilt;
	public GridBagConstraints gbc;
	private final JPanel contentPane = new JPanel();

	private final RobotOverlord robotOverlord;

	public ComponentPanelFactory(RobotOverlord robotOverlord) {
		super();
		this.robotOverlord = robotOverlord;

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		Insets in = contentPane.getInsets();
		in.left=3;
		in.top=3;
		in.right=3;
		in.bottom=3;
	}
	
	public ComponentPanelFactory() {
		this(null);
	}

	private void startComponentPanelShared() {
		panelBeingBuilt = new JPanel(new GridBagLayout());
		panelBeingBuilt.setBorder(new EmptyBorder(1, 1, 1, 1));

		gbc = new GridBagConstraints();
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.set(1, 1, 1, 1);
	}

	public void startComponentPanel(Component component) {
		startComponentPanelShared();

		CollapsiblePanel collapsiblePanel = new CollapsiblePanel(component.getName());
		JPanel content = collapsiblePanel.getContentPane();
		collapsiblePanel.setCollapsed(!component.getExpanded());
		content.setLayout(new BorderLayout());
		content.add(panelBeingBuilt, BorderLayout.CENTER);
		collapsiblePanel.setPreferredSize(new Dimension(100, 100));
		contentPane.add(collapsiblePanel);

		collapsiblePanel.addCollapeListener(new CollapsiblePanel.CollapseListener() {
			@Override
			public void collapsed() {
				component.setExpanded(!collapsiblePanel.isCollapsed());
			}

			@Override
			public void expanded() {
				component.setExpanded(!collapsiblePanel.isCollapsed());
			}
		});
	}
	
	private void pushViewElement(ViewElement c) {
		gbc.gridy++;
		panelBeingBuilt.add(c,gbc);
	}

	public JComponent getFinalView() {
		return contentPane;
	}
	
	/**
	 * Add an componentpanel element based on the parameter type.
	 * @param parameter the parameter to add
	 */
	public ViewElement add(AbstractParameter<?> parameter) {
		ViewElement element=null;
		
		//logger.debug("Add "+e.getClass().toString());
		
			 if(parameter instanceof BooleanParameter  ) element = new ViewElementBoolean  ((BooleanParameter)parameter);
		else if(parameter instanceof ColorParameter    ) element = new ViewElementColor    ((ColorParameter)parameter);
		else if(parameter instanceof DoubleParameter   ) element = new ViewElementDouble   ((DoubleParameter)parameter);
		else if(parameter instanceof IntParameter      ) element = new ViewElementInt      ((IntParameter)parameter);
		else if(parameter instanceof Vector3DParameter ) element = new ViewElementVector3d ((Vector3DParameter)parameter);
		else if(parameter instanceof ReferenceParameter) element = new ViewElementReference((ReferenceParameter)parameter,robotOverlord);
		else if(parameter instanceof StringParameter   ) element = new ViewElementString   ((StringParameter)parameter);

		if(null==element) {
			return addStaticText("ViewPanel.add("+parameter.getClass().toString()+")");
		}

		pushViewElement(element);
		return element;
	}
	

	public ViewElement addStaticText(String text) {
		ViewElement b = new ViewElement();
		b.add(new JLabel(text,JLabel.LEADING));
		pushViewElement(b);
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
		pushViewElement(b);
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
		pushViewElement(b);
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
		pushViewElement(b);
		return b;
	}

	/**
	 * Add a control for an string that includes a filename selection dialog
	 * @param e the Parameter that holds the current value.
	 * @param filters
	 * @return the element
	 */
	public ViewElement addFilename(StringParameter e, ArrayList<FileFilter> filters) {
		ViewElementFilename b = new ViewElementFilename(e);
		b.addFileFilters(filters);
		
		pushViewElement(b);
		return b;
	}

	public ViewElementButton addButton(String string) {
		ViewElementButton b = new ViewElementButton(string);
		pushViewElement(b);
		return b;
	}
}
