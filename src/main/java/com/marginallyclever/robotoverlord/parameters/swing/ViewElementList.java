package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.ListParameter;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class ViewElementList extends ViewElement {
    private final ListParameter<?> list;
    private final EntityManager entityManager;
    private final JPanel interior = new JPanel();

    public ViewElementList(ListParameter<?> list,EntityManager entityManager) {
        super();
        this.list = list;
        this.entityManager = entityManager;

        this.setLayout(new BorderLayout());
        this.add(new JLabel(list.getName(), JLabel.LEADING), BorderLayout.NORTH);
        this.add(new JScrollPane(interior),BorderLayout.CENTER);

        updateList();
    }

    public void updateList() {
        interior.removeAll();
        ViewPanelFactory factory = new ViewPanelFactory(entityManager);
        for(int i=0;i<list.size();++i) {
            factory.add(list.get(i));
        }
        interior.add(factory.getFinalView(),BorderLayout.CENTER);
    }
}
