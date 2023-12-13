package com.marginallyclever.ro3;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;

/**
 * {@link FactoryPanel} allows a user to select from a list of things that can be created by a given {@link Factory}.
 * @param <T> the class of thing to create.
 */
public class FactoryPanel<T> extends JPanel {
    private final Factory<T> factory;

    public FactoryPanel(Factory<T> factory) {
        super();
        this.factory = factory;

        setMinimumSize(new Dimension(400, 300));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel("Select a node type to create:"));

        String [] names = getListOfNodeTypes();
        JScrollPane scroll = new JScrollPane();
        scroll.setViewportView(new JList<>(names));
        add(scroll);
    }

    private String [] getListOfNodeTypes() {
        List<String> names = new ArrayList<>();
        Factory.Category<T> n = factory.getRoot();
        List<Factory.Category<T>> toVisit = new ArrayList<>(n.children);
        while(!toVisit.isEmpty()) {
            Factory.Category<T> current = toVisit.remove(0);
            names.add(current.name);
            toVisit.addAll(current.children);
        }

        String [] list = new String[names.size()];
        for(int i=0;i<names.size();++i) {
            list[i] = names.get(i);
        }
        return list;
    }

    /**
     * @return either JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION
     */
    public int getResult() {
        return JOptionPane.OK_OPTION;
    }

    public String getSelectedNode() {
        return "Node";
    }
}
