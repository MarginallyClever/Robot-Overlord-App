package com.marginallyclever.ro3.render;

import com.marginallyclever.ro3.Registry;

import javax.swing.*;
import java.awt.*;

/**
 * A panel that shows the {@link com.marginallyclever.ro3.Registry#renderPasses} list and allows the user to change it.
 */
public class RenderPassPanel extends JPanel {
    private final DefaultListModel<RenderPass> model = new DefaultListModel<>();
    private final JList<RenderPass> list = new JList<>(model);

    public RenderPassPanel() {
        super(new BorderLayout());

        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(((RenderPass) value).getName());
                return this;
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
