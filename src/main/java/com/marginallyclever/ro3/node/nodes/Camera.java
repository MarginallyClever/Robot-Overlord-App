package com.marginallyclever.ro3.node.nodes;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.robotoverlord.swing.CollapsiblePanel;

import javax.swing.*;
import java.util.List;

public class Camera extends Pose {

    public Camera() {
        super("Camera");
    }

    public Camera(String name) {
        super(name);
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        Registry.cameras.add(this);
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        Registry.cameras.remove(this);
    }

    /**
     * Build a Swing Component that represents this Node.
     * @param list the list to add components to.
     */
    public void getComponents(List<JComponent> list) {
        CollapsiblePanel panel = new CollapsiblePanel(Camera.class.getSimpleName());
        list.add(panel);
        JPanel pane = panel.getContentPane();

        pane.add(new JLabel("Camera stuff goes here"));

        super.getComponents(list);
    }
}
