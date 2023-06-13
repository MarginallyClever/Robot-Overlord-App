package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.components.ComponentFactory;
import com.marginallyclever.robotoverlord.swinginterface.searchBar.SearchBar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

public class ComponentFactoryPanel extends JPanel {
    private final JList<String> filteredNames = new JList<>();

    public ComponentFactoryPanel() {
        super(new BorderLayout());
        setName("ComponentFactoryPanel");

        SearchBar searchBar = new SearchBar();
        add(searchBar, BorderLayout.NORTH);
        searchBar.addPropertyChangeListener("match", evt -> filterSearch(evt.getNewValue().toString()));
        filterSearch("");

        add(new JScrollPane(filteredNames), BorderLayout.CENTER);
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

    public String getSelectedClassName() {
        return filteredNames.getSelectedValue();
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
