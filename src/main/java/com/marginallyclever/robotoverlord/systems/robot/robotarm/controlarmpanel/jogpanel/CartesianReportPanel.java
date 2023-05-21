package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.jogpanel;

import com.marginallyclever.convenience.log.Log;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.vecmath.Matrix4d;
import java.awt.*;
import java.io.Serial;

/**
 * Displays the values of a {@link Matrix4d}.
 */
public class CartesianReportPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private final JTable table;

	public CartesianReportPanel(String title) {
		super();

		DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
        renderRight.setHorizontalAlignment(SwingConstants.RIGHT);

		table = new JTable(4,4) {
			@Serial
			private static final long serialVersionUID = 1L;

		    @Override
		    public TableCellRenderer getCellRenderer (int arg0, int arg1) {
		        return renderRight;
		    }
		};

		//setColumnNames();
		
		this.setBorder(BorderFactory.createTitledBorder(/*BorderFactory.createEmptyBorder(),*/title));
		this.setLayout(new BorderLayout());
		this.add(/*new JScrollPane*/(table),BorderLayout.CENTER);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(false);
	}

	@SuppressWarnings("unused")
	private void setColumnNames() {
		DefaultTableCellRenderer renderHeaderRight = (DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer();
		renderHeaderRight.setHorizontalAlignment(SwingConstants.RIGHT);
		table.getTableHeader().setDefaultRenderer(renderHeaderRight);
		
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setHeaderValue( "X" );
		tcm.getColumn(1).setHeaderValue( "Y" );
		tcm.getColumn(2).setHeaderValue( "Z" );
		tcm.getColumn(3).setHeaderValue( "-" );
	}

	public CartesianReportPanel() {
		this("CartesianReport");
	}
	
	public CartesianReportPanel(String title,Matrix4d m) {
		this(title);
		updateReport(m);
	}
	
	public CartesianReportPanel(Matrix4d m) {
		this();
		updateReport(m);
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

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}

		Matrix4d m = new Matrix4d();
		m.setIdentity();
		
		JFrame frame = new JFrame(CartesianReportPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new CartesianReportPanel(m));
		frame.pack();
		frame.setVisible(true);
	}
}
