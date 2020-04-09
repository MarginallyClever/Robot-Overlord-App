package com.marginallyclever.robotOverlord.swingInterface.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.RemoteEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;

/**
 * A factory that builds Swing elements for the entity editor
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class ViewPanel extends ViewElement {
	
	protected class StackElement {
		public JComponent p;
		public GridBagConstraints gbc;
	}
	
	protected Stack<StackElement> panelStack = new Stack<StackElement>();
	protected StackElement se;
	protected JTabbedPane tabbedPane;

	
	public ViewPanel(RobotOverlord ro) {
		super(ro);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
		tabbedPane.addFocusListener(this);
		Insets in = tabbedPane.getInsets();
		in.left=5;
		in.top=5;
		in.right=5;
		in.bottom=5;
	}
	
	public void pushStack(String title,String tip) {
		se = new StackElement();
		se.p = new JPanel();
		//se.p.setLayout(new BoxLayout(se.p, BoxLayout.PAGE_AXIS));
		se.p.setLayout(new GridBagLayout());
		//se.p.setBorder(new LineBorder(Color.RED));
		se.p.setBorder(new EmptyBorder(5,5,5,5));

		se.gbc = new GridBagConstraints();
		se.gbc.weightx=1;
		se.gbc.gridx  =0;
		se.gbc.gridy  =0;
		se.gbc.fill      = GridBagConstraints.HORIZONTAL;
		se.gbc.gridwidth = GridBagConstraints.REMAINDER;
		se.gbc.insets.top   =5;
		se.gbc.insets.left  =5;
		se.gbc.insets.right =5; 
		se.gbc.insets.bottom=5; 

		panelStack.push(se);
		
		tabbedPane.addTab(title, null, se.p, tip);
	}
	
	public void popStack() {
		se.gbc.weighty=1;
		se.gbc.gridy++;
		se.p.add(new JLabel(""),se.gbc);
		panelStack.pop();
	}
	
	protected void pushViewElement(ViewElement c) {
		se.gbc.gridy++;
		se.p.add(c.panel,se.gbc);
	}

	public JComponent getFinalView() {
		return tabbedPane;
	}
	
	/**
	 * Add an view element based on the entity type.
	 */
	public ViewElement add(Entity e) {
		ViewElement b=null;
		
		//System.out.println("Add "+e.getClass().toString());
		
			 if(e instanceof BooleanEntity ) b = new ViewElementBoolean  (ro,(BooleanEntity)e);
		else if(e instanceof ColorEntity   ) b = new ViewElementColor    (ro,(ColorEntity)e);
		else if(e instanceof DoubleEntity  ) b = new ViewElementDouble   (ro,(DoubleEntity)e);
		else if(e instanceof IntEntity     ) b = new ViewElementInt      (ro,(IntEntity)e);
		else if(e instanceof Vector3dEntity) b = new ViewElementVector3d (ro,(Vector3dEntity)e);
		else if(e instanceof RemoteEntity  ) b = new ViewElementRemote   (ro,(RemoteEntity)e);  // must come before StringEntity because extends StringEntity
		else if(e instanceof StringEntity  ) b = new ViewElementString   (ro,(StringEntity)e);
		if(null==b) {
			return addStaticText("ViewPanel.add("+e.getClass().toString()+")");
		}
		// else b not null.
		pushViewElement(b);
		return b;
	}
	

	public ViewElement addStaticText(String text) {
		ViewElement b = new ViewElement(ro);
		b.panel.add(new JLabel(text,JLabel.LEADING));
		pushViewElement(b);
		return b;
	}

	public ViewElement addComboBox(IntEntity e,String [] labels) {
		ViewElement b = new ViewElementComboBox(ro,e,labels);
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
		ViewElement b = new ViewElementSlider(ro,e,top,bottom);
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
		ViewElement b = new ViewElementSliderDouble(ro,e,top,bottom);
		pushViewElement(b);
		return b;
	}

	/**
	 * Add a control for an string that includes a filename selection dialog
	 * @param e
	 * @param top the maximum value, inclusive
	 * @param bottom the minimum value, inclusive
	 * @return the element
	 */
	public ViewElement addFilename(StringEntity e,ArrayList<FileFilter> filters) {
		ViewElementFilename b = new ViewElementFilename(ro,e);
		b.addFileFilters(filters);
		
		pushViewElement(b);
		return b;
	}

	public ViewElementButton addButton(String string) {
		ViewElementButton b = new ViewElementButton(ro,string);
		pushViewElement(b);
		return b;
	}
}
