package com.marginallyclever.robotoverlord.parameters.swing;

import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.AbstractParameter;
import com.marginallyclever.robotoverlord.parameters.ListParameter;
import com.marginallyclever.robotoverlord.parameters.StringParameter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to alter a {@link ListParameter}.
 * @author Dan Royer
 * @since 2.6.3
 */
public class ViewElementList extends ViewElement {
    private final ListParameter<?> list;
    private final EntityManager entityManager;
    private final JPanel header = new JPanel(new BorderLayout(2,2));
    private final JPanel interior = new JPanel();
    private final JButton bAdd = new JButton("+");
    private final JButton bRemove = new JButton("-");
    private final JLabel countLabel = new JLabel();
    private final List<Boolean> selectedItems = new ArrayList<>();

    public ViewElementList(ListParameter<?> list,EntityManager entityManager) {
        super();
        this.list = list;
        this.entityManager = entityManager;

        this.setLayout(new BorderLayout(0,0));
        this.add(header, BorderLayout.NORTH);
        interior.setLayout(new BoxLayout(interior,BoxLayout.Y_AXIS));
        this.add(new JScrollPane(interior),BorderLayout.CENTER);

        initializeSelectedItems();

        buildHeader();
        updateList();
    }

    private void initializeSelectedItems() {
        for(int i=0;i<list.size();++i) {
            selectedItems.add(false);
        }
    }

    private void buildHeader() {
        JPanel buttons = new JPanel(new GridLayout(1,2));
        header.add(new JLabel(list.getName(), JLabel.LEADING),BorderLayout.WEST);
        buttons.add(bAdd);
        buttons.add(bRemove);
        header.add(buttons,BorderLayout.EAST);
        header.add(countLabel,BorderLayout.CENTER);

        bAdd.addActionListener(e->addItem());
        bRemove.addActionListener(e->removeSelectedItems());
    }

    private void addItem() {
        list.addOneToEnd();
        selectedItems.add(false);
        updateList();
    }

    private void removeSelectedItems() {
        for(int i=selectedItems.size()-1;i>=0;--i) {
            if(selectedItems.get(i)) {
                list.remove(i);
                selectedItems.remove(i);
            }
        }
        updateList();
    }

    public void updateList() {
        countLabel.setText("("+Integer.toString(list.size())+" items)");

        interior.removeAll();
        ViewElementFactory factory = new ViewElementFactory(entityManager);
        for(int i=0;i<list.size();++i) {
            // name each item in sequence
            AbstractParameter<?> item = list.get(i);
            item.setName(Integer.toString(i));
            // add a selection checkbox
            JCheckBox b = new JCheckBox();
            int finalI = i;
            b.addChangeListener(e->{
                selectedItems.set(finalI,b.isSelected());
                bRemove.setEnabled(selectedItems.contains(true));
            });
            // put it together
            JPanel itemPanel = new JPanel(new BorderLayout());
            itemPanel.add(b,BorderLayout.WEST);
            itemPanel.add(factory.add(item),BorderLayout.CENTER);
            interior.add(itemPanel);
        }
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        ListParameter<StringParameter> list = new ListParameter<>("Test",new StringParameter("A"));
        EntityManager em = new EntityManager();
        ViewElementFactory factory = new ViewElementFactory(em);

        JFrame frame = new JFrame(ListParameter.class.getSimpleName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(factory.add(list),BorderLayout.NORTH);
        frame.setPreferredSize(new Dimension(400,400));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
