package com.marginallyclever.ro3;

import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.NodeDetailView;
import com.marginallyclever.ro3.node.nodeselector.NodeSelectionPanel;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.util.Set;

/**
 * <p>Display a card layout of all the {@link JPanel}s in the project.  This would be handy for translators to see all
 * the panels in one place.</p>
 * <p>{@link Reflections} can only find classes that extend {@link JPanel}.  Therefore in a second pass create one
 * instance of every {@link Node} and add the components from {@link Node#getComponents(List)}.</p>
 */
public class TestAllPanels {
    private static final Logger logger = LoggerFactory.getLogger(TestAllPanels.class);
    public static void main(String[] args) {
        Registry.start();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            //UIManager.setLookAndFeel(new FlatDarkLaf());
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            logger.error("Failed to set look and feel.");
        }

        JPanel container = new JPanel(new BorderLayout());
        JPanel comboBoxPane = new JPanel();
        JPanel cardPanel = new JPanel(new CardLayout());
        container.add(comboBoxPane, BorderLayout.PAGE_START);
        container.add(cardPanel, BorderLayout.CENTER);

        // names for combo box
        List<String> names = new ArrayList<>();

        // first pass, create one of every panel that extends JPanel
        Reflections reflections = new Reflections("com.marginallyclever.ro3");
        Set<Class<? extends JPanel>> allClasses = reflections.getSubTypesOf(JPanel.class);
        for(var panelClass : allClasses) {
            System.out.println("Panel "+panelClass.getName());
            try {
                JPanel panelInstance;
                if(panelClass.equals(FactoryPanel.class)) continue;  // skip, no default constructor
                if(panelClass.equals(DockingPanel.class)) continue;  // skip, no default constructor
                panelInstance = panelClass.getDeclaredConstructor().newInstance();
                System.out.println("Adding "+panelClass.getName());
                cardPanel.add(panelInstance, panelClass.getName());
                names.add(panelClass.getName());
            } catch (Exception e) {
                logger.warn("Failed to create instance of "+panelClass.getName(),e);
            }
        }
        // second pass, create one of every panel from Node.getComponents()
        Set<Class<? extends Node>> allNodeClasses = reflections.getSubTypesOf(Node.class);
        for(var nodeClass : allNodeClasses) {
            System.out.println("Node "+nodeClass.getName());
            try {
                Node node = nodeClass.getDeclaredConstructor().newInstance();
                try {
                    System.out.println("Adding "+node.getName());
                    JComponent interior = NodeDetailView.createPanelFor(List.of(node));
                    cardPanel.add(interior, nodeClass.getName());
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
    }
}
