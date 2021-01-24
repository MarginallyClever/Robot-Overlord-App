package com.marginallyclever.robotOverlord;

import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

public class ObserverTest2 extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	GridBagConstraints gbc;
	
	class ObservableModel {
		protected boolean state;

		// who is listening to me?
		protected ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();
		
		public void addPropertyChangeListener(PropertyChangeListener p) {
			propertyChangeListeners.add(p);
		}
		
		public void removePropertyChangeListener(PropertyChangeListener p) {
			propertyChangeListeners.remove(p);
		}
		
		public void notifyPropertyChangeListeners(PropertyChangeEvent evt) {
			for( PropertyChangeListener p : propertyChangeListeners ) {
				p.propertyChange(evt);
			}
		}

		public boolean isState() {
			return state;
		}

		public void setState(boolean newValue) {
			boolean oldValue = this.state;
			System.out.println("setState("+newValue+")");
			this.state = newValue;
			notifyPropertyChangeListeners(new PropertyChangeEvent(this,"state",oldValue,newValue));
		}
	}
	
	class ObservingField extends JCheckBox implements PropertyChangeListener, ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ObservableModel mod;
		
		public ObservingField(ObservableModel mod) {
			super();
			this.mod=mod;
			mod.addPropertyChangeListener(this);
			addActionListener(this);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Object arg1 = evt.getNewValue();
			System.out.println("update("+arg1+")");
			setSelected((boolean)arg1);
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			System.out.println("actionPerformed("+this.isSelected()+")");
			mod.setState(this.isSelected());
		}
	}
	
	public ObserverTest2() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	
		gbc = new GridBagConstraints();
		gbc.weightx=1;
		//gbc.gridx  =0;
		gbc.fill      = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.insets.top   =5;
		gbc.insets.left  =5;
		gbc.insets.right =5; 
		gbc.insets.bottom=5; 

		ObservableModel mod = new ObservableModel();
		ObservingField obs = new ObservingField(mod);
		
		add(obs,gbc);
		
		mod.setState(true);
		mod.setState(false);
		
		JButton thirdParty = new JButton("3rd party");
		thirdParty.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				mod.setState(!mod.isState());
			}
		
		});
		add(thirdParty,gbc);
	}
	
	
	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
	            JFrame f = new JFrame("Observer Test 2");
	            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	            
	            f.add("Center", new JScrollPane(new ObserverTest2()));
	            f.pack();
	            f.setVisible(true);
	        }
	    });
	}
}
