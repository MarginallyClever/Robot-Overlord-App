package com.marginallyclever.robotoverlord.components.robot.robotarm.robotArmInterface;

import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.robots.Robot;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DHTable extends JPanel {
    private final JTable table = new JTable();
    private final DefaultTableModel dtm;

    public DHTable(RobotComponent robot) {
        super();

        DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
        renderRight.setHorizontalAlignment(SwingConstants.RIGHT);

        dtm = new DefaultTableModel((int)robot.get(Robot.NUM_JOINTS),5);
        dtm.setColumnIdentifiers(new String[]{"#","D","R","α (alpha)","Θ (theta)"});
        table.setModel(dtm);
        table.setDefaultRenderer(Object.class,renderRight);

        table.getTableHeader().setDefaultRenderer(renderRight);

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(table),BorderLayout.CENTER);
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(false);

        updateReport(robot);
    }

    public void updateReport(RobotComponent robot) {
        int count = (int)robot.get(Robot.NUM_JOINTS);

        for(int i=0;i<count;++i) {
            table.setValueAt(i, i, 0);
            table.setValueAt(String.format("%.3f", robot.getBone(i).getD()), i, 1);
            table.setValueAt(String.format("%.3f", robot.getBone(i).getR()), i, 2);
            table.setValueAt(String.format("%.3f", robot.getBone(i).getAlpha()), i, 3);
            table.setValueAt(String.format("%.3f", robot.getBone(i).getTheta()), i, 4);
        }
    }
}
