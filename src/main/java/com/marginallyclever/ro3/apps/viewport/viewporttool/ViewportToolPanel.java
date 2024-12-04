package com.marginallyclever.ro3.apps.viewport.viewporttool;

import com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel;
import com.marginallyclever.ro3.apps.viewport.Viewport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Displays the settings for the currently active {@link ViewportTool}
 */
public class ViewportToolPanel extends JPanel implements ViewportToolChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(ViewportToolPanel.class);
    private final JScrollPane scroll = new JScrollPane();

    public ViewportToolPanel() {
        this(new Viewport());
    }

    public ViewportToolPanel(Viewport viewport) {
        super(new BorderLayout());
        setName("Tool");

        add(scroll,BorderLayout.CENTER);

        // TODO I don't love that one Viewport is tied to one ViewportToolPanel.
        viewport.addToolChangeListener(this);
        onViewportToolChange(viewport.getTool(viewport.getActiveToolIndex()));
    }

    @Override
    public void onViewportToolChange(ViewportTool tool) {
        scroll.setViewportView(createPanelFor(tool));
        this.revalidate();
        this.repaint();
    }

    private JPanel createPanelFor(ViewportTool tool) {
        this.removeAll();
        if(tool == null) return new JPanel();

        var list = new ArrayList<JPanel>();
        try {
            tool.getComponents(list);
        } catch (Exception e) {
            logger.error("Error getting components for tool {}", tool, e);
        }

        // collate the components
        Box vertical = Box.createVerticalBox();
        for (JPanel c : list) {
            CollapsiblePanel panel = new CollapsiblePanel(c.getName());
            panel.setContentPane(c);
            vertical.add(panel);
        }

        // attach them
        JPanel parent = new JPanel(new BorderLayout());
        parent.add(vertical, BorderLayout.NORTH);
        return parent;
    }
}
