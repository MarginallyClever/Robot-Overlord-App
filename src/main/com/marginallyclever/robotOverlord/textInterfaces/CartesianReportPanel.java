package com.marginallyclever.robotOverlord.textInterfaces;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.vecmath.Matrix4d;

public class CartesianReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTable table;

	public CartesianReportPanel(String title) {
		super();
		
		table = new JTable(4,4) {
			private static final long serialVersionUID = 1L;
			DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();

		    { // initializer block
		        renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
		    }

		    @Override
		    public TableCellRenderer getCellRenderer (int arg0, int arg1) {
		        return renderRight;
		    }
		};

		this.setBorder(BorderFactory.createTitledBorder(title));
		this.add((table));
	}
	public CartesianReportPanel() {
		this("CartesianReport");
	}
	
	public void setTitle(String s) {
		this.setBorder(BorderFactory.createTitledBorder(s));
	}
	
	public void updateReport(Matrix4d m) {
		table.setValueAt(String.format("%.3f", m.m00), 0, 0);
		table.setValueAt(String.format("%.3f", m.m01), 0, 1);
		table.setValueAt(String.format("%.3f", m.m02), 0, 2);
		table.setValueAt(String.format("%.3f", m.m03), 0, 3);
                                                           
		table.setValueAt(String.format("%.3f", m.m10), 1, 0);
		table.setValueAt(String.format("%.3f", m.m11), 1, 1);
		table.setValueAt(String.format("%.3f", m.m12), 1, 2);
		table.setValueAt(String.format("%.3f", m.m13), 1, 3);
                                                           
		table.setValueAt(String.format("%.3f", m.m20), 2, 0);
		table.setValueAt(String.format("%.3f", m.m21), 2, 1);
		table.setValueAt(String.format("%.3f", m.m22), 2, 2);
		table.setValueAt(String.format("%.3f", m.m23), 2, 3);
                                                           
		table.setValueAt(String.format("%.3f", m.m30), 3, 0);
		table.setValueAt(String.format("%.3f", m.m31), 3, 1);
		table.setValueAt(String.format("%.3f", m.m32), 3, 2);
		table.setValueAt(String.format("%.3f", m.m33), 3, 3);
	}
}
