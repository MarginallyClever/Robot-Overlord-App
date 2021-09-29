package com.marginallyclever.robotOverlord.textInterfaces;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.marginallyclever.robotOverlord.robots.sixi3.ApproximateJacobian;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class JacobianReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTable table;
	
	public JacobianReportPanel(Sixi3IK sixi3) {
		super();
	
		table = new JTable(6,6) {
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
		
		this.setBorder(BorderFactory.createTitledBorder("Jacobian"));
		this.add(
				(table));
		
		sixi3.addPropertyChangeListener( (e)-> updateReport(sixi3) );
		
		updateReport(sixi3);
	}
	
	private void updateReport(Sixi3IK sixi3) {
		ApproximateJacobian aj = sixi3.getApproximateJacobian();
		for(int y=0;y<aj.jacobian.length;++y) {
			for(int x=0;x<aj.jacobian[y].length;++x) {
				table.setValueAt(String.format("%.5f", aj.jacobian[y][x]), y, x);
			}
		}
	}
}
