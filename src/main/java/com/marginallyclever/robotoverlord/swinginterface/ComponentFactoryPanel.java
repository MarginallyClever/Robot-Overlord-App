package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.components.ComponentFactory;
import com.marginallyclever.robotoverlord.swinginterface.searchBar.SearchBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class ComponentFactoryPanel extends JPanel {
    private final SearchBar searchBar = new SearchBar();
    private final JList<String> filteredNames = new JList<>();
    private final JButton addButton = new JButton("Add");

    public ComponentFactoryPanel() {
        super(new BorderLayout());
        setName("ComponentFactoryPanel");

        add(searchBar, BorderLayout.NORTH);
        searchBar.addPropertyChangeListener("match", evt -> filterSearch(evt.getNewValue().toString()));
        filterSearch("");

        add(new JScrollPane(filteredNames), BorderLayout.CENTER);
        add(addButton, BorderLayout.SOUTH);
        addButton.addActionListener(evt->addNow(evt));
    }

    private void filterSearch(String match) {
        ArrayList<String> names = ComponentFactory.getAllComponentNames();
        match = match.toLowerCase();

        for(int i=names.size()-1;i>=0;--i) {
            String n = names.get(i);
            if (!n.toLowerCase().contains(match))
                names.remove(i);
        }
        names.sort(String::compareToIgnoreCase);

        DefaultListModel<String> model = new DefaultListModel<>();
        model.addAll(names);
        filteredNames.setModel(model);
    }

    private void addNow(ActionEvent evt) {
        String name = filteredNames.getSelectedValue();
        if (name==null) return;
        //ComponentFactory.createInstance(name);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ComponentFactoryPanel");
        frame.setContentPane(new ComponentFactoryPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new java.awt.Dimension(450,300));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
