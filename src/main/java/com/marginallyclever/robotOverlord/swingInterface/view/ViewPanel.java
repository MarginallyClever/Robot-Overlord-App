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
import javax.swing.filechooser.FileNameExtensionFilter;

import com.marginallyclever.robotOverlord.RobotOverlord;
import com.marginallyclever.robotOverlord.entity.Entity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.ColorEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.DoubleEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.IntEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.StringEntity;
import com.marginallyclever.robotOverlord.entity.basicDataTypes.Vector3dEntity;

public class ViewPanel implements View {
	class StackElement {
		public JComponent p;
		public GridBagConstraints gbc;
	}
	// This doesn't need to be a stack.
	Stack<StackElement> panelStack = new Stack<StackElement>();
	StackElement se;

	JTabbedPane tabbedPane;
	
	RobotOverlord ro;

	public ViewPanel(RobotOverlord ro) {
		super();
		this.ro=ro;
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP,JTabbedPane.SCROLL_TAB_LAYOUT);
		Insets in = tabbedPane.getInsets();
		in.left=5;
		in.top=5;
		in.right=5;
		in.bottom=5;
	}
	
	@Override
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
	
	@Override
	public void popStack() {
		se.gbc.weighty=1;
		pushViewElement(new JLabel(""));
		panelStack.pop();
	}
	
	protected void pushViewElement(JComponent c) {
		se.gbc.gridy++;
		se.p.add(c,se.gbc);
	}

	@Override
	public void addReadOnly(String s) {
		JLabel label = new JLabel(s,JLabel.LEADING);
		pushViewElement(label);
	}
	
	@Override
	public void addReadOnly(Entity e) {
		JLabel label = new JLabel(e.toString(),JLabel.LEADING);
		pushViewElement(label);
	}
	
	@Override
	public void addBoolean(BooleanEntity e) {
		ViewPanelBoolean b = new ViewPanelBoolean(ro,e);
		e.addObserver(b);
		pushViewElement(b);
	}
	
	@Override
	public void addEnum(IntEntity e,String [] listOptions) {
		ViewPanelComboBox b = new ViewPanelComboBox(ro, e, listOptions);
		e.addObserver(b);
		pushViewElement(b);
	}

	@Override
	public void addFilename(StringEntity e,ArrayList<FileNameExtensionFilter> f) {
		ViewPanelFilename b = new ViewPanelFilename(ro, e);
		for( FileNameExtensionFilter fi : f ) {
			b.addChoosableFileFilter( fi );
		}
		
		e.addObserver(b);
		pushViewElement(b);
	}

	@Override
	public void addColor(ColorEntity e) {
		ViewPanelColorRGBA b = new ViewPanelColorRGBA(ro, e);
		e.addObserver(b);
		pushViewElement(b);
	}

	@Override
	public void addVector3(Vector3dEntity e) {
		ViewPanelVector3d b = new ViewPanelVector3d(ro, e);
		e.addObserver(b);
		pushViewElement(b);
	}

	@Override
	public void addInt(IntEntity e) {
		ViewPanelInt b = new ViewPanelInt(ro, e);
		e.addObserver(b);
		pushViewElement(b);
	}

	@Override
	public void addDouble(DoubleEntity e) {
		ViewPanelDouble b = new ViewPanelDouble(ro, e);
		e.addObserver(b);
		pushViewElement(b);
	}

	@Override
	public JComponent getFinalView() {
		return tabbedPane;
	}
}
