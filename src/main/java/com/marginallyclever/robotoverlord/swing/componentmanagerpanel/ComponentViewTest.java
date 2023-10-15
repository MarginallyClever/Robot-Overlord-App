package com.marginallyclever.robotoverlord.swing.componentmanagerpanel;

import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.ComponentFactory;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import java.util.List;
import javax.swing.*;
import java.awt.*;

public class ComponentViewTest extends JPanel {
    private final EntityManager entityManager = new EntityManager();
    private final ComponentManagerPanel componentManagerPanel = new ComponentManagerPanel(entityManager,null);
    private final JComboBox<String> names = new JComboBox<>();

    public ComponentViewTest() {
        super(new BorderLayout());
        add(getSelectionPanel(),BorderLayout.NORTH);
        add(componentManagerPanel,BorderLayout.CENTER);
    }

    private JPanel getSelectionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        ComponentFactory.getAllComponentNames().forEach(names::addItem);
        names.addActionListener(e -> updateInnerView());
        panel.add(names);
        updateInnerView();
        return panel;
    }

    private void updateInnerView() {
        String name = (String)names.getSelectedItem();
        if(name!=null) {
            Component component = ComponentFactory.load(name);
            Entity entity = entityManager.getRoot();
            List<Component> list = entity.getComponents();
            while(!list.isEmpty()) {
                entity.removeComponent(list.remove(0));
            }
            entity.addComponent(component);
            Clipboard.setSelectedEntity(entity);
            componentManagerPanel.refreshContentsFromClipboard();
        }
    }

    public static void main(String[] args) {
        Translator.start();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ignored) {}

        JFrame frame = new JFrame(ComponentViewTest.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ComponentViewTest());
        frame.setPreferredSize(new Dimension(500,600));
        frame.pack();
        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
    }
}
