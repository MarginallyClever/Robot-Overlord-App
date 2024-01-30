package com.marginallyclever.ro3.node.nodes.behavior.decorators;

import com.marginallyclever.ro3.PanelHelper;

import javax.swing.*;
import java.awt.*;

public class RepeatPanel extends JPanel {
    public RepeatPanel(Repeat repeat) {
        super(new GridLayout(0,2));

        // count
        JSpinner count = new JSpinner(new SpinnerNumberModel(repeat.getCount(),0,1000,1));
        count.addChangeListener(e -> repeat.setCount((Integer) count.getValue()));
        PanelHelper.addLabelAndComponent(this,"Count",count);

        // current
        JLabel current = new JLabel(""+repeat.getCurrent());
        repeat.addPropertyChangeListener(e->{current.setText(""+repeat.getCurrent());});
        PanelHelper.addLabelAndComponent(this,"Current",current);
    }
}
