package com.marginallyclever.ro3;

import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.ro3.apps.nodedetailview.NodeDetailView;
import com.marginallyclever.ro3.node.Node;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AllPanels {
    private static final Logger logger = LoggerFactory.getLogger(AllPanels.class);

    public static JFrame showAllPanels() {
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

        JPanel container = new JPanel(new BorderLayout());
        JPanel comboBoxPane = new JPanel();
        JPanel cardPanel = new JPanel(new CardLayout());
        container.add(comboBoxPane, BorderLayout.PAGE_START);
        container.add(cardPanel, BorderLayout.CENTER);

        // names for combo box
        java.util.List<String> names = new ArrayList<>();

        // Don't add these panels because they don't have a default constructor.
        java.util.List<Class<?>> exceptions = java.util.List.of(
                com.marginallyclever.ro3.apps.nodedetailview.CollapsiblePanel.class,
                com.marginallyclever.ro3.apps.DockingPanel.class,
                com.marginallyclever.ro3.apps.App.class,
                com.marginallyclever.ro3.apps.nodeselector.NodeSelector.class
        );

        Reflections reflections = new Reflections("com.marginallyclever.ro3");

        // first pass, create one of every panel that extends JPanel
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

        // second pass, create one of every panel from Node.getComponents()
        Set<Class<? extends Node>> allNodeClasses = reflections.getSubTypesOf(Node.class);
        for(var nodeClass : allNodeClasses) {
            logger.debug("Node "+nodeClass.getName());
            try {
                Node node = nodeClass.getDeclaredConstructor().newInstance();
                try {
                    logger.debug("Adding "+node.getName());
                    var view = new NodeDetailView();
                    view.selectionChanged(List.of(node));
                    cardPanel.add(view, nodeClass.getName());
                    names.add(nodeClass.getName());
                } catch(Exception e) {
                    logger.error("Error getting components for node {}",node,e);
                }
            } catch (Exception e) {
                logger.warn("Failed to create instance of "+nodeClass.getName(),e);
            }
        }

        // build the combo box
        JComboBox<String> comboBox = new JComboBox<>(names.toArray(new String[0]));
        comboBox.setEditable(false);
        comboBoxPane.add(comboBox);
        comboBox.addItemListener((e)->{
            CardLayout cl = (CardLayout)(cardPanel.getLayout());
            cl.show(cardPanel, (String)e.getItem());
        });

        // create and display a frame
        javax.swing.JFrame frame = new javax.swing.JFrame("Test All Panels");
        frame.setContentPane(container);
        frame.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 600);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return frame;
    }
}
