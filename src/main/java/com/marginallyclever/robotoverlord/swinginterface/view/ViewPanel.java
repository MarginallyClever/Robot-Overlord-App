package com.marginallyclever.robotoverlord.swinginterface.view;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.CollapsiblePanel;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentDeleteAction;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.uiexposedtypes.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.util.ArrayList;
import java.util.Stack;

/**
 * A factory that builds Swing elements for the entity editor
 * @author Dan Royer
 * @since 1.6.0
 */
public class ViewPanel extends ViewElement {
	protected static class StackElement {
		public JComponent p;
		public GridBagConstraints gbc;
	}
	
	protected final Stack<StackElement> panelStack = new Stack<>();
	protected StackElement se;
	protected final JPanel contentPane = new JPanel();

	private final RobotOverlord ro;

	public ViewPanel(RobotOverlord ro) {
		super();
		this.ro=ro;

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

		panelStack.push(se);
	}

	public void pushStack(String name,boolean expanded) {
		pushStackShared();

		CollapsiblePanel collapsiblePanel = new CollapsiblePanel(name);
		JPanel content = collapsiblePanel.getContentPane();
		collapsiblePanel.setCollapsed(!expanded);
		content.setLayout(new BorderLayout());
		content.add(se.p, BorderLayout.CENTER);
		contentPane.add(collapsiblePanel);
	}

	public void pushStack(Component component) {
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

		ComponentDeleteAction componentDeleteAction = new ComponentDeleteAction("DeleteComponentAction.name",component,ro);
		componentDeleteAction.putValue(Action.SHORT_DESCRIPTION, Translator.get("DeleteComponentAction.shortDescription"));
		componentDeleteAction.putValue(Action.SMALL_ICON,new UnicodeIcon("🗑"));
		popup.add(componentDeleteAction);

		panel.setComponentPopupMenu(popup);
	}
	
	public void popStack() {
		panelStack.pop();
	}
	
	protected void pushViewElement(ViewElement c) {
		se.gbc.gridy++;
		se.p.add(c,se.gbc);
	}

	public JComponent getFinalView() {
		return contentPane;
	}
	
	/**
	 * Add an view element based on the entity type.
	 */
	public ViewElement add(Entity e) {
		ViewElement b=null;
		
		//logger.debug("Add "+e.getClass().toString());
		
			 if(e instanceof BooleanEntity ) b = new ViewElementBoolean  ((BooleanEntity)e);
		else if(e instanceof ColorEntity   ) b = new ViewElementColor    ((ColorEntity)e);
		else if(e instanceof DoubleEntity  ) b = new ViewElementDouble   ((DoubleEntity)e);
		else if(e instanceof IntEntity     ) b = new ViewElementInt      ((IntEntity)e);
		else if(e instanceof Vector3dEntity) b = new ViewElementVector3d ((Vector3dEntity)e);
		else if(e instanceof RemoteEntity  ) b = new ViewElementRemote   ((RemoteEntity)e);  // must come before StringEntity because RemoteEntity extends StringEntity
		else if(e instanceof StringEntity  ) b = new ViewElementString   ((StringEntity)e);
		if(null==b) {
			return addStaticText("ViewPanel.add("+e.getClass().toString()+")");
		}
		// else b not null.
		pushViewElement(b);
		return b;
	}
	

	public ViewElement addStaticText(String text) {
		ViewElement b = new ViewElement();
		b.add(new JLabel(text,JLabel.LEADING));
		pushViewElement(b);
		return b;
	}

	public ViewElement addComboBox(IntEntity e,String [] labels) {
		ViewElement b = new ViewElementComboBox(e,labels);
		pushViewElement(b);
		return b;
		
	}

	/**
	 * Add a control for an integer that is bound between two values
	 * @param e
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElement addRange(IntEntity e,int top,int bottom) {
		ViewElement b = new ViewElementSlider(e,top,bottom);
		pushViewElement(b);
		return b;
	}

	/**
	 * Add a control for an double that is bound between two values
	 * @param e
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElement addRange(DoubleEntity e,int top,int bottom) {
		ViewElement b = new ViewElementSliderDouble(e,top,bottom);
		pushViewElement(b);
		return b;
	}

	/**
	 * Add a control for an string that includes a filename selection dialog
	 * @param e
	 * @param filters
	 * @return the element
	 */
	public ViewElement addFilename(StringEntity e,ArrayList<FileFilter> filters) {
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
