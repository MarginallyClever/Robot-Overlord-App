package com.marginallyclever.ro3;

import com.formdev.flatlaf.FlatLightLaf;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import java.awt.*;

public class AllPanels {
    private static final Logger logger = LoggerFactory.getLogger(AllPanels.class);

    public AllPanels() {
        super();
    }

    // Create a panel with a combo box and a card layout containing all the panels.
    public JPanel buildSet() {
        JPanel container = new JPanel(new BorderLayout());
        JPanel comboBoxPane = new JPanel();
        JPanel cardPanel = new JPanel(new CardLayout());
        container.add(comboBoxPane, BorderLayout.PAGE_START);
        container.add(cardPanel, BorderLayout.CENTER);

        // names for combo box
        List<String> names = new ArrayList<>();

        // Don't add these panels because they don't have a default constructor.
        List<Class<?>> exceptions = List.of(
            com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel.class,
            com.marginallyclever.ro3.apps.DockingPanel.class,
            com.marginallyclever.ro3.apps.App.class,  // abstract
            com.marginallyclever.ro3.apps.dialogs.AppSettingsDialog.class
        );

        Reflections reflections = new Reflections("com.marginallyclever.ro3");
        collectAllJPanels(reflections, cardPanel, names, exceptions);

        // build the combo box
        names.sort(String::compareTo);
        JComboBox<String> comboBox = new JComboBox<>(names.toArray(new String[0]));
        comboBox.setEditable(false);
        comboBoxPane.add(comboBox);
        comboBox.addItemListener((e)->{
            CardLayout cl = (CardLayout)(cardPanel.getLayout());
            cl.show(cardPanel, (String)e.getItem());
        });

        return container;
    }

    // create one of every panel that extends JPanel
    private void collectAllJPanels(Reflections reflections, JPanel cardPanel, List<String> names, List<Class<?>> exceptions) {
        Set<Class<? extends JPanel>> allClasses = reflections.getSubTypesOf(JPanel.class);
        for(var panelClass : allClasses) {
            logger.debug("Panel "+panelClass.getName());
            try {
                if(exceptions.contains(panelClass)) continue;  // skip, no default constructor
                JPanel panelInstance = panelClass.getDeclaredConstructor().newInstance();
                logger.debug("Adding "+panelClass.getName());
                cardPanel.add(panelInstance, panelClass.getName());
                names.add(panelClass.getName());
            } catch (Exception e) {
                logger.warn("Failed to create instance of "+panelClass.getName(),e);
            }
        }
    }

    // create and display a frame containing all the panels
    public JFrame buildFrame() {
        javax.swing.JFrame frame = new javax.swing.JFrame("All Panels");
        frame.setContentPane(buildSet());
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return frame;
    }

    public static void main(String[] args) {
        Registry.start();

        // print the classpath
        System.out.println("Classpath:");
        for(var path : ClasspathHelper.forJavaClassPath()) {
            System.out.println("  "+path);
        }

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
            logger.error("Failed to set look and feel.");
        }

        AllPanels allPanels = new AllPanels();
        allPanels.buildFrame();
    }
}
