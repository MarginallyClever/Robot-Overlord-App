package com.marginallyclever.robotOverlord.swingInterface;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.marginallyclever.convenience.log.Log;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class InputManagerResultsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	class Row {
		public String controller, component, identifier;
		public boolean isRelative, isAnalog;
		public float value;
	}
	
	class MyTableData extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public ArrayList<Row> rows = new ArrayList<Row>();

	    @Override
		public int getColumnCount() {
	        return 6;
	    }

	    @Override
		public int getRowCount() {
	        return rows.size();
	    }

	    @Override
		public String getColumnName(int col) {
	        return columnNames[col];
	    }

	    @Override
		public Object getValueAt(int row, int col) {
	    	Row r = rows.get(row);
	    	switch(col) {
	    	case 0: return r.controller;
	    	case 1: return r.component;
	    	case 2: return r.identifier;
	    	case 3: return r.isRelative;
	    	case 4: return r.isAnalog;
	    	default: return r.value;
	    	} 
	    }

	    @Override
		@SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }
	}
	
	private static String [] columnNames = {"Controller","Component","Identifier","Relative","Analog","Value"};
	private JTable table;
	private MyTableData data = new MyTableData();
	
	public InputManagerResultsPanel() {
		super();
		
		DefaultTableCellRenderer renderRight = new DefaultTableCellRenderer();
	    renderRight.setHorizontalAlignment(SwingConstants.RIGHT);

	    fillData();
		table = new JTable(data) {
			private static final long serialVersionUID = 1L;
	
		    @Override
		    public TableCellRenderer getCellRenderer (int arg0, int arg1) {
		        return renderRight;
		    }
		};
		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table),BorderLayout.CENTER);
		this.setPreferredSize(getPreferredSize());
		
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setFillsViewportHeight(false);
		
		InputManager.addPropertyChangeListener((e)->{
			SwingUtilities.invokeLater(()->{
				fillData();
				repaint();
			});
		});
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				InputManager.update(true);
			}
		}, 30, 30);
	}
	
	private void fillData() {
		data.rows.clear();
		
		ControllerEnvironment ce = ControllerEnvironment.getDefaultEnvironment();
		Controller[] ca = ce.getControllers();
        for(int i =0;i<ca.length;i++){
            Component[] components = ca[i].getComponents();

            // Get this controllers components (buttons and axis)
            for(int j=0;j<components.length;j++){
                Row r = new Row();
                r.component = ca[i].getName();
                r.controller = components[j].getName();
                r.identifier = components[j].getIdentifier().getName();
                r.isRelative = components[j].isRelative();
                r.isAnalog = components[j].isAnalog();
                r.value = components[j].getPollData();
                data.rows.add(r);
            }
        }
        data.fireTableDataChanged();
	}
	
	// TEST
        
	public static void main(String[] args) {
		Log.start();
		InputManager.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		
		ArrayList<Double> list = new ArrayList<Double>();
		for(int i=0;i<250;++i) {
			list.add(Math.random()*500);
		}

		JFrame frame = new JFrame(InputManagerResultsPanel.class.getSimpleName());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new InputManagerResultsPanel());
		frame.pack();

		frame.addWindowListener(new WindowAdapter() {
    		// switch back to this window
        	@Override
            public void windowActivated(WindowEvent e) {
        		super.windowActivated(e);
        		InputManager.focusGained();
        		frame.setTitle(InputManagerResultsPanel.class.getSimpleName() + " [in focus]");
        	}

    		// switch away to another window
        	@Override
            public void windowDeactivated(WindowEvent e) {
        		super.windowDeactivated(e);
        		InputManager.focusLost();
        		frame.setTitle(InputManagerResultsPanel.class.getSimpleName() + " [out of focus]");
        	}
		});
		frame.setVisible(true);
	}
}
