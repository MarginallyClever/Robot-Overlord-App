package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.CollapsiblePanel;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentAddAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentCopyAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentDeleteAction;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentPasteAction;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Collate all the {@link Component}s for selected {@link Entity}s.
 */
public class ComponentManagerPanel extends JPanel {

	private final RobotOverlord robotOverlord;
	private final JPanel componentList = new JPanel();
	private final JToolBar toolBar = new JToolBar();
	private final ComponentDeleteAction componentDeleteAction = new ComponentDeleteAction(this);
	private final ComponentCopyAction componentCopyAction = new ComponentCopyAction();
	private final ComponentPasteAction componentPasteAction = new ComponentPasteAction();


	public ComponentManagerPanel(RobotOverlord robotOverlord) {
		super(new BorderLayout());
		this.robotOverlord = robotOverlord;

		componentList.setLayout(new BoxLayout(componentList,BoxLayout.Y_AXIS));
		Insets in = componentList.getInsets();
		in.left=3;
		in.top=3;
		in.right=3;
		in.bottom=3;

		add(toolBar,BorderLayout.NORTH);
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(componentList,BorderLayout.PAGE_START);
		add(wrapper,BorderLayout.CENTER);

		createToolBar();

		refreshContentsFromClipboard();
	}

	private void createToolBar() {
		toolBar.add(new ComponentAddAction(this));
		toolBar.add(componentDeleteAction);
		toolBar.add(componentCopyAction);
		toolBar.add(componentPasteAction);
	}

	/**
	 * Collate all the {@link java.awt.Component}s for selected {@link Entity}s.
	 */
	public void refreshContentsFromClipboard() {
		List<Entity> entityList = Clipboard.getSelectedEntities();

		int size = entityList.size();
		if(size==0) {
			toolBar.setVisible(false);
		} else {
			toolBar.setVisible(true);

			if (size == 1) {
				buildSingleEntityPanel(entityList.get(0));
			} else if(size > 1) {
				// TODO finish and re-enable this.
				//buildMultipleEntityPanel(entityList);
			}
		}

		componentList.repaint();
		componentList.revalidate();
	}

	private void buildSingleEntityPanel(Entity entity) {
		componentList.removeAll();
		if(entity == null) return;

		for(int i=0;i<entity.getComponentCount();++i) {
			addSingleComponent(entity,entity.getComponent(i));
		}
	}

	private void addSingleComponent(Entity entity,Component component) {
		// get the view
		ComponentPanelFactory factory = new ComponentPanelFactory(robotOverlord,component);
		component.getView(factory);
		JComponent finalView = factory.getFinalView();

		// create the outer CollapsiblePanel
		CollapsiblePanel outerPanel = new CollapsiblePanel(component.getName());
		outerPanel.setCollapsed(!component.getExpanded());
		outerPanel.addCollapeListener(new CollapsiblePanel.CollapseListener() {
			@Override
			public void collapsed() {
				component.setExpanded(!outerPanel.isCollapsed());
			}

			@Override
			public void expanded() {
				component.setExpanded(!outerPanel.isCollapsed());
			}
		});

		// add a control panel
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

		// with a delete button
		JButton deleteButton = new JButton((String)componentDeleteAction.getValue(Action.NAME));
		deleteButton.addActionListener(e -> {
			componentDeleteAction.setComponent(component);
			componentDeleteAction.actionPerformed(null);
			componentList.remove(outerPanel);
		});
		controlPanel.add(deleteButton);

		// and a copy button
		JButton copyButton = new JButton((String)componentCopyAction.getValue(Action.NAME));
		copyButton.addActionListener(e -> {
			componentCopyAction.setComponent(component);
			componentCopyAction.actionPerformed(null);
		});
		controlPanel.add(copyButton);

		// connect the view and control panel in a wrapper panel
		JPanel wrapperPanel = new JPanel(new BorderLayout());
		wrapperPanel.add(finalView, BorderLayout.CENTER);
		wrapperPanel.add(controlPanel, BorderLayout.SOUTH);

		// add the wrapper panel to the outer panel
		JPanel magic = outerPanel.getContentPane();
		magic.setLayout(new BorderLayout());
		magic.add(wrapperPanel, BorderLayout.CENTER);

		// add the outer panel to the list
		componentList.add(outerPanel);
	}

	/**
	 * TODO find the list of {@link Component}s shared by all selected {@link Entity}.
	 * Display values shared across those Components.
	 */
	private void buildMultipleEntityPanel(List<Entity> entityList) {
		// compare Entities and keep matching Components.
		Entity first = entityList.get(0);
		for(int i=0;i<first.getComponentCount();++i) {
			Component component = first.getComponent(i);
			if(sameComponentInAllEntities(component,entityList)) {
				ComponentPanelFactory factory = new ComponentPanelFactory(robotOverlord,component);
				component.getView(factory);
				JComponent componentPanel = factory.getFinalView();
				componentList.add(componentPanel);
			}
		}
	}

	private boolean sameComponentInAllEntities(Component component, List<Entity> entityList) {
		for(Entity e : entityList) {
			if(!e.containsAnInstanceOfTheSameClass(component)) {
				return false;
			}
		}
		return true;
	}
}
