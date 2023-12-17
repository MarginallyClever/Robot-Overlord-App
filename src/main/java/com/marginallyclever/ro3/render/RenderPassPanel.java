package com.marginallyclever.ro3.render;

import com.marginallyclever.ro3.Registry;

import javax.swing.*;
import java.awt.*;

public class RenderPassPanel extends JPanel {
    private final DefaultListModel<RenderPass> model = new DefaultListModel<>();
    private final JList<RenderPass> list = new JList<>(model);

    public RenderPassPanel() {
        super(new BorderLayout());

        list.setCellRenderer(new ListCellRenderer<RenderPass>() {
            @Override
            public Component getListCellRendererComponent(JList<? extends RenderPass> list, RenderPass value, int index, boolean isSelected, boolean cellHasFocus) {
                JPanel panel = new JPanel(new BorderLayout());
                JButton button = new JButton();
                setButtonLabel(button, value.getActiveStatus());
                button.addActionListener(e -> {
                    value.setActiveStatus((value.getActiveStatus() + 1) % RenderPass.MAX_STATUS );
                    setButtonLabel(button, value.getActiveStatus());
                    list.repaint();
                });
                panel.add(button, BorderLayout.WEST);
                panel.add(new JLabel(value.getName()), BorderLayout.CENTER);
                return panel;
            }

            void setButtonLabel(JButton button, int status) {
                switch(status) {
                    case RenderPass.NEVER -> button.setText("N");
                    case RenderPass.SOMETIMES -> button.setText("S");
                    case RenderPass.ALWAYS -> button.setText("A");
                }
            }
        });

        add(new JScrollPane(list), BorderLayout.CENTER);
        addAllPasses();
    }

    private void addAllPasses() {
        for(RenderPass pass : Registry.renderPasses.getList()) {
            addPass(pass);
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.renderPasses.addItemAddedListener(this::addPass);
        Registry.renderPasses.addItemRemovedListener(this::removePass);
    }

    private void addPass(RenderPass renderPass) {
        model.addElement(renderPass);
    }

    private void removePass(RenderPass renderPass) {
        model.removeElement(renderPass);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.renderPasses.removeItemAddedListener(this::addPass);
        Registry.renderPasses.removeItemRemovedListener(this::removePass);
    }
}