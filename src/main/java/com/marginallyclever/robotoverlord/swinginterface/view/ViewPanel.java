package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.parameters.*;
import com.marginallyclever.robotoverlord.swinginterface.CollapsiblePanel;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentCopyAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentDeleteAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentPasteAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.ArrayList;

/**
 * A factory that builds Swing elements for the entity editor
 * @author Dan Royer
 * @since 1.6.0
 */
public class ViewPanel extends ViewElement {

	private static class StackElement {
		public JComponent p;
		public GridBagConstraints gbc;
	}

	private StackElement se;
	private final JPanel contentPane = new JPanel();

	private final RobotOverlord robotOverlord;

	public ViewPanel(RobotOverlord robotOverlord) {
		super();
		this.robotOverlord = robotOverlord;

		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));

		contentPane.addFocusListener(this);
		Insets in = contentPane.getInsets();
		in.left=3;
		in.top=3;
		in.right=3;
		in.bottom=3;
	}
	
	public ViewPanel() {
		this(null);
	}

	private void pushStackShared() {
		se = new StackElement();
		se.p = new JPanel();

		se.p.setLayout(new GridBagLayout());
		se.p.setBorder(new LineBorder(Color.RED));
		se.p.setBorder(new EmptyBorder(1, 1, 1, 1));

		se.gbc = new GridBagConstraints();
		se.gbc.weightx = 1;
		se.gbc.gridx = 0;
		se.gbc.gridy = 0;
		se.gbc.fill = GridBagConstraints.HORIZONTAL;
		se.gbc.gridwidth = GridBagConstraints.REMAINDER;
		se.gbc.insets.set(1, 1, 1, 1);
	}

	public void startNewSubPanel(String name, boolean expanded) {
		pushStackShared();

		CollapsiblePanel collapsiblePanel = new CollapsiblePanel(name);
		JPanel content = collapsiblePanel.getContentPane();
		collapsiblePanel.setCollapsed(!expanded);
		content.setLayout(new BorderLayout());
		content.add(se.p, BorderLayout.CENTER);
		contentPane.add(collapsiblePanel);
	}

	public void startNewSubPanel(Component component) {
		pushStackShared();
		setPopupMenu(component, se.p);

		CollapsiblePanel collapsiblePanel = new CollapsiblePanel(component.getName());
		JPanel content = collapsiblePanel.getContentPane();
		collapsiblePanel.setCollapsed(!component.getExpanded());
		content.setLayout(new BorderLayout());
		content.add(se.p, BorderLayout.CENTER);
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

	private void setPopupMenu(Component component,JComponent panel) {
		JPopupMenu popup = new JPopupMenu();

		ComponentDeleteAction componentDeleteAction = new ComponentDeleteAction(component, robotOverlord);
		popup.add(componentDeleteAction);

		ComponentCopyAction componentCopyAction = new ComponentCopyAction(component);
		popup.add(componentCopyAction);

		ComponentPasteAction componentPasteAction = new ComponentPasteAction();
		popup.add(componentPasteAction);

		panel.setComponentPopupMenu(popup);
	}
	
	private void pushViewElement(ViewElement c) {
		se.gbc.gridy++;
		se.p.add(c,se.gbc);
	}

	public JComponent getFinalView() {
		return contentPane;
	}
	
	/**
	 * Add an view element based on the parameter type.
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
