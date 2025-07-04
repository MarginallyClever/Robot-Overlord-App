package com.marginallyclever.ro3.apps.nodedetailview;

import com.marginallyclever.ro3.RO3;
import com.marginallyclever.ro3.RO3Frame;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.actions.BrowseURLAction;
import com.marginallyclever.ro3.listwithevents.ItemAddedListener;
import com.marginallyclever.ro3.listwithevents.ItemRemovedListener;
import com.marginallyclever.ro3.node.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link NodeDetailView} is a panel that displays the details of a class that implements {@link Node}.
 */
public class NodeDetailView extends App implements ItemAddedListener<Node>, ItemRemovedListener<Node>, SceneChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(NodeDetailView.class);
    public static final String DOC_ROOT_URL = "https://marginallyclever.github.io/Robot-Overlord-App/";
    public static String VERSION_NAME = NodeDetailView.class.getPackage().getImplementationVersion();
    public static String DOC_URL = DOC_ROOT_URL + VERSION_NAME;
    private final JScrollPane scroll = new JScrollPane();
    private final JToggleButton pinButton = new JToggleButton();
    private final JButton docButton = new JButton();
    private final ImageIcon bookIcon;

    static {
        String v = NodeDetailView.class.getPackage().getImplementationVersion();
        if(v==null) {
            // if the version is not set, use the value in the pom file.
            v = RO3Frame.VERSION;
        }
        VERSION_NAME = v;
        DOC_URL = DOC_ROOT_URL + VERSION_NAME;
    }

    public NodeDetailView() {
        super(new BorderLayout());

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        // pin button to keep the current selection
        pinButton.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/shared/icons8-pin-16.png"))));
        pinButton.setToolTipText("Pin this selection.");
        pinButton.addActionListener(e -> {
            setPinToolTipText(pinButton);
            if(!pinButton.isSelected()) {
                // when pin released, refresh view.
                selectionChanged();
            }
        });
        toolbar.add(pinButton);

        // book icon links to documentation
        bookIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/com/marginallyclever/ro3/apps/icons8-open-book-16.png")));
        docButton.setIcon(bookIcon);
        docButton.setEnabled(false);
        toolbar.add(docButton);

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        selectionChanged();

        Registry.addSceneChangeListener(this);
    }

    private void setPinToolTipText(JToggleButton pinButton) {
        if(pinButton.isSelected()) {
            pinButton.setToolTipText("Unpin this selection.");
        } else {
            pinButton.setToolTipText("Pin this selection.");
        }
    }

    private JPanel createPanelFor(List<Node> nodeList) {
        JPanel parent = new JPanel(new BorderLayout());

        // handle no selection.
        if(nodeList.isEmpty()) {
            JLabel label = new JLabel("No nodes selected.");
            label.setHorizontalAlignment(SwingConstants.CENTER);
            parent.add(label, BorderLayout.CENTER);
            return parent;
        }

        // collect the components from every node
        List<JPanel> list = new ArrayList<>();
        for (Node node : nodeList) {
            try {
                node.getComponents(list);
            } catch (Exception e) {
                logger.error("Error getting components for node {}", node, e);
            }
        }

        if(nodeList.size()==1) {
            // add a documentation button that links to
            var clazz = nodeList.getFirst().getClass();
            String moduleName = clazz.getModule().getName();
            String className = clazz.getName();
            String url = DOC_URL + (DOC_URL.endsWith("/")?"":"/")
                    + moduleName + "/"
                    + className.replace('.', '/') + ".html";
            var docAction = new BrowseURLAction(url);
            docAction.putValue(Action.NAME, "Docs");
            docAction.putValue(Action.SMALL_ICON, bookIcon);
            docAction.putValue(Action.SHORT_DESCRIPTION, "Open documentation for "+clazz.getSimpleName());
            docButton.setAction(docAction);
        }
        docButton.setEnabled(nodeList.size()==1);

        // collate the components
        Box vertical = Box.createVerticalBox();
        for (JPanel c : list) {
            CollapsiblePanel panel = new CollapsiblePanel(c.getName());
            panel.setContentPane(c);
            vertical.add(panel);
        }

        // attach them to the parent
        parent.add(vertical, BorderLayout.NORTH);
        return parent;
    }

    private void selectionChanged() {
        selectionChanged(Registry.selection.getList());
    }

    /**
     * Called when the selection changes.
     * @param selectedNodes the list of nodes that are currently selected.
     */
    public void selectionChanged(List<Node> selectedNodes) {
        if(pinButton.isSelected()) return;
        scroll.setViewportView(createPanelFor(selectedNodes));
        docButton.setEnabled(!selectedNodes.isEmpty());
        this.revalidate();
        this.repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.selection.addItemAddedListener(this);
        Registry.selection.addItemRemovedListener(this);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.selection.removeItemAddedListener(this);
        Registry.selection.removeItemRemovedListener(this);
    }

    @Override
    public void itemAdded(Object source,Node item) {
        selectionChanged();
    }

    @Override
    public void itemRemoved(Object source,Node item) {
        selectionChanged();
    }

    @Override
    public void beforeSceneChange(Node oldScene) {
        pinButton.setSelected(false);
    }

    @Override
    public void afterSceneChange(Node newScene) {}
}
