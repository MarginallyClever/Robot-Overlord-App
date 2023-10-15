package com.marginallyclever.robotoverlord.swing.componentmanagerpanel;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;
import com.marginallyclever.robotoverlord.swing.actions.ComponentAddAction;
import com.marginallyclever.robotoverlord.swing.actions.ComponentCopyAction;
import com.marginallyclever.robotoverlord.swing.actions.ComponentDeleteAction;
import com.marginallyclever.robotoverlord.swing.actions.ComponentPasteAction;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import com.marginallyclever.robotoverlord.systems.SystemManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Collate all the {@link Component}s for selected {@link Entity}s.
 */
public class ComponentManagerPanel extends JPanel {
	private final EntityManager entityManager;
	private final JPanel componentList = new JPanel();
	private final JToolBar toolBar = new JToolBar();
	private final ComponentDeleteAction componentDeleteAction = new ComponentDeleteAction(this);
	private final ComponentCopyAction componentCopyAction = new ComponentCopyAction();
	private final ComponentPasteAction componentPasteAction = new ComponentPasteAction();
	private final List<EntitySystem> systems = new ArrayList<>();

	public ComponentManagerPanel(EntityManager entityManager, SystemManager systems) {
		super(new BorderLayout());

		if(entityManager == null) throw new NullPointerException("entityManager cannot be null.");
		this.entityManager = entityManager;

		if(systems != null) this.systems.addAll(systems.getList());

		componentList.setLayout(new BoxLayout(componentList,BoxLayout.Y_AXIS));
		Insets in = componentList.getInsets();
		in.left=3;
		in.top=3;
		in.right=3;
		in.bottom=3;

		add(toolBar,BorderLayout.NORTH);
		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.add(componentList,BorderLayout.PAGE_START);
		add(new JScrollPane(wrapper),BorderLayout.CENTER);

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

		componentList.removeAll();
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
		if(entity == null) return;

		for(int i=0;i<entity.getComponentCount();++i) {
			addSingleComponent(entity.getComponent(i));
		}
	}

	private void addSingleComponent(Component component) {
		// get the view
		ComponentSwingViewFactory factory = new ComponentSwingViewFactory(entityManager);

		// add control common to all components
		factory.add(component.enabled);

		// custom panel views based on component type
		for(EntitySystem sys : systems) {
			sys.decorate(factory,component);
		}


		JComponent outerPanel = wrapViewWithCommonComponentControls(factory.getResult(),component);
		componentList.add(outerPanel);
	}

	private JComponent wrapViewWithCommonComponentControls(JComponent finalView,Component component) {
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
		return outerPanel;
	}

	/**
	 * TODO find the list of {@link Component}s shared by all selected {@link Entity}.
	 * Display values shared across those Components.
	 */
	private void buildMultipleEntityPanel(List<Entity> entityList) {
		throw new RuntimeException("Not implemented yet.");
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
