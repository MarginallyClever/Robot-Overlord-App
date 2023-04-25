package com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
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
	private final DefaultListModel<ComponentPanelPair> listModel = new DefaultListModel<>();
	private final JList<ComponentPanelPair> componentList = new JList<>(listModel);
	private ComponentPanelPair selectedItem;
	private final JToolBar toolBar = new JToolBar();
	private final ComponentDeleteAction componentDeleteAction = new ComponentDeleteAction(this);
	private final ComponentCopyAction componentCopyAction = new ComponentCopyAction();
	private final ComponentPasteAction componentPasteAction = new ComponentPasteAction();


	public ComponentManagerPanel(RobotOverlord robotOverlord) {
		super(new BorderLayout());
		this.robotOverlord = robotOverlord;

		// Set a custom list cell renderer
		componentList.setCellRenderer(new ComponentPanelCellRenderer());

		// Add a ListSelectionListener to track the selected component
		componentList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				selectedItem = componentList.getSelectedValue();
				Component component = selectedItem == null ? null : selectedItem.component;
				componentCopyAction.setComponent(component);
				componentDeleteAction.setComponent(component);
			}
		});

		createToolBar();

		add(toolBar,BorderLayout.NORTH);
		add(new JScrollPane(componentList),BorderLayout.CENTER);
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
		componentList.removeAll();

		int size = entityList.size();
		if(size==0) {
			toolBar.setVisible(false);
		} else {
			toolBar.setVisible(true);

			listModel.clear();

			if (size == 1) {
				buildSingleEntityPanel(entityList.get(0));
			} else if(size > 1) {
				// TODO finish and re-enable this.
				//buildMultipleEntityPanel(panel,entityList);
			}
		}
		componentList.repaint();
		componentList.revalidate();

		if( componentList.getParent() instanceof JScrollPane ) {
			JScrollPane scroller = (JScrollPane)getParent();
			scroller.getVerticalScrollBar().setValue(0);
		}
	}

	private void buildSingleEntityPanel(Entity entity) {
		if(entity == null) return;

		for(int i=0;i<entity.getComponentCount();++i) {
			ComponentPanelFactory factory = new ComponentPanelFactory(robotOverlord);
			Component component = entity.getComponent(i);
			factory.startComponentPanel(component);
			component.getView(factory);

			JComponent finalView = factory.getFinalView();
			listModel.addElement(new ComponentPanelPair(component,finalView));
		}
	}

	/**
	 * TODO find the list of {@link Component}s shared by all selected {@link Entity}.
	 * Display values shared across those Components.
	 * @param factory the factory that builds the UI
	 * @param entityList the list of selected entities
	 */
	private void buildMultipleEntityPanel(ComponentPanelFactory factory, List<Entity> entityList) {
		// compare Entities and keep matching Components.
		Entity first = entityList.get(0);
		for(int i=0;i<first.getComponentCount();++i) {
			Component component = first.getComponent(i);
			if(sameComponentInAllEntities(component,entityList)) {
				factory.startComponentPanel(component);
				component.getView(factory);
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
