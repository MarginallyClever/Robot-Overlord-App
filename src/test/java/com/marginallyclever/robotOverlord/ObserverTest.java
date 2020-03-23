package com.marginallyclever.robotOverlord;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ObserverTest extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	class ObservableModel extends Observable {
		protected int value;
		protected int max,min;

		public ObservableModel(int min,int max,int value) {
			this.min=min;
			this.max=max;
			this.value=value;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int newValue) {
			System.out.println("setState("+newValue+")");
			if(newValue<min) return;
			if(newValue>max) return;
			setChanged();
			this.value = newValue;
			notifyObservers(newValue);
		}
	}
	
	class ObservingField extends JPanel implements Observer, ChangeListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JSlider field;
		ObservableModel mod;
		JButton buttonAdd = new JButton("+");
		JButton buttonSub = new JButton("-");
		JLabel label = new JLabel();
		
		public ObservingField(ObservableModel mod) {
			setLayout(new BorderLayout());
			this.mod=mod;
			field = new JSlider(mod.min,mod.max,mod.getValue());
			mod.addObserver(this);
			field.addChangeListener(this);

			buttonAdd.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					mod.setValue(mod.getValue()+10);
				}
			});
			
			buttonSub.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					mod.setValue(mod.getValue()-10);
				}
			});
			
			add(buttonSub,BorderLayout.LINE_START);
			add(field,BorderLayout.CENTER);
			add(buttonAdd,BorderLayout.LINE_END);
			add(label,BorderLayout.PAGE_END);
		}
		
		@Override
		public void update(Observable arg0, Object arg1) {
			int v = (int)arg1;
			System.out.println("update("+v+")");
			field.setValue(v);
			label.setText(Integer.toString(v));
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			System.out.println("actionPerformed("+field.getValue()+")");
			mod.setValue(field.getValue());
		}
	}
	
	public ObserverTest() {
		setLayout(new BorderLayout());
	
		ObservableModel mod = new ObservableModel(0,100,50);
		ObservingField obs = new ObservingField(mod);
		
		add(obs,BorderLayout.CENTER);
		
		mod.setValue(20);
		mod.setValue(50);
	}
	
	
	public static void main(String[] argv) {
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
	            JFrame f = new JFrame("Tab Test");
	            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	            
	            f.add("Center", new JScrollPane(new ObserverTest()));
	            f.pack();
	            //f.setSize(400,200);
	            f.setVisible(true);
	        }
	    });
	}
}
