package com.marginallyclever.robotoverlord.robots.robotarm.robotArmInterface.jogInterface;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.robots.robotarm.ApproximateJacobian2;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.io.Serial;

public class JacobianReportPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = 1L;
	private final JTable table;
	
	public JacobianReportPanel(RobotComponent arm) {
		super();

		ApproximateJacobian2 aj = new ApproximateJacobian2(arm);

		DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
        renderRight.setHorizontalAlignment(SwingConstants.RIGHT);
        
		table = new JTable(aj.jacobian.length,aj.jacobian[0].length) {
			private static final long serialVersionUID = 1L;

		    @Override
		    public TableCellRenderer getCellRenderer (int arg0, int arg1) {
		        return renderRight;
		    }
		};

		//setColumnNames(sixi3);
		
		this.setBorder(BorderFactory.createTitledBorder(/*BorderFactory.createEmptyBorder(),*/JacobianReportPanel.class.getSimpleName()));
		this.setLayout(new BorderLayout());
		this.add(/*new JScrollPane*/(table),BorderLayout.CENTER);
		
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(false);
		
		arm.addPropertyChangeListener( (e)-> updateReport(arm) );
		
		updateReport(arm);
	}
	
	@SuppressWarnings("unused")
	private void setColumnNames(RobotComponent arm) {
		DefaultTableCellRenderer renderHeaderRight = (DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer();
		renderHeaderRight.setHorizontalAlignment(SwingConstants.RIGHT);
		table.getTableHeader().setDefaultRenderer(renderHeaderRight);
		
		TableColumnModel tcm = table.getColumnModel();
		for(int i=0;i<arm.getNumBones();++i) {
			TableColumn tc = tcm.getColumn(i);
			tc.setHeaderValue( arm.getBone(i).getName() );
		}
	}

	private void updateReport(RobotComponent arm) {
		ApproximateJacobian2 aj = new ApproximateJacobian2(arm);
		for(int y=0;y<aj.jacobian.length;++y) {
			for(int x=0;x<aj.jacobian[y].length;++x) {
				table.setValueAt(String.format("%.5f", aj.jacobian[y][x]), y, x);
			}
		}
	}

	// TEST

	public static void main(String[] args) {
		Log.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception ignored) {}

		JFrame frame = new JFrame(JacobianReportPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new JacobianReportPanel(new RobotComponent()));
		frame.pack();
		frame.setVisible(true);
	}
}
