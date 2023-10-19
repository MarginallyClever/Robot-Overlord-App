package com.marginallyclever.robotoverlord.swing;

import com.marginallyclever.robotoverlord.components.ComponentFactory;
import com.marginallyclever.robotoverlord.swing.searchBar.SearchBar;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Searchable list of all {@link ComponentFactory} names.
 * @since 2.7.0
 * @author Dan Royer
 */
public class ComponentFactoryPanel extends JPanel {
    private final JList<String> filteredNames = new JList<>();
    private static String lastSearch = "";

    public ComponentFactoryPanel() {
        super(new BorderLayout());
        setName("ComponentFactoryPanel");

        SearchBar searchBar = new SearchBar(lastSearch);
        add(searchBar, BorderLayout.NORTH);
        searchBar.addPropertyChangeListener("match", evt -> filterSearch(evt.getNewValue().toString()));
        filterSearch(lastSearch);

        add(new JScrollPane(filteredNames), BorderLayout.CENTER);
    }

    private void filterSearch(String match) {
        lastSearch = match;
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
