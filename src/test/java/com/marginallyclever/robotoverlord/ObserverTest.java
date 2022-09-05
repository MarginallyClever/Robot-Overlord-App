package com.marginallyclever.robotoverlord;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.junit.After;
import org.junit.Before;

import com.marginallyclever.convenience.log.Log;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ObserverTest extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

	@Before
	public void before() {
		Log.start();
	}
	
	@After
	public void after() {
		Log.end();
	}
	
    public ObserverTest() {
        setLayout(new BorderLayout());

        ObservableModel mod = new ObservableModel(0, 100, 50);
        ObservingField obs = new ObservingField(mod);

        add(obs, BorderLayout.CENTER);

        mod.setValue(20);
        mod.setValue(50);
    }

    public static void main(String[] argv) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame f = new JFrame("Observer Test");
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                f.add("Center", new JScrollPane(new ObserverTest()));
                f.pack();
                f.setVisible(true);
            }
        });
    }

    class ObservableModel {
        protected int value;
        protected int max, min;

        // who is listening to me?
        protected ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();

        public ObservableModel(int min, int max, int value) {
            this.min = min;
            this.max = max;
            this.value = value;
        }

        public void addPropertyChangeListener(PropertyChangeListener p) {
            propertyChangeListeners.add(p);
        }

        public void removePropertyChangeListener(PropertyChangeListener p) {
            propertyChangeListeners.remove(p);
        }

        public void notifyPropertyChangeListeners(PropertyChangeEvent evt) {
            for (PropertyChangeListener p : propertyChangeListeners) {
                p.propertyChange(evt);
            }
        }

        public int getValue() {
            return value;
        }

        public void setValue(int newValue) {
            Log.message("setState(" + newValue + ")");
            if (newValue < min) return;
            if (newValue > max) return;
            int oldValue = value;
            this.value = newValue;

            notifyPropertyChangeListeners(new PropertyChangeEvent(this, "state", oldValue, newValue));
        }
    }

    class ObservingField extends JPanel implements PropertyChangeListener, ChangeListener {
        /**
         *
         */
        private static final long serialVersionUID = 1L;
        JSlider field;
        ObservableModel mod;
        JButton buttonAdd = new JButton("+");
        JButton buttonSub = new JButton("-");
        JLabel label = new JLabel();

        public ObservingField(final ObservableModel mod) {
            setLayout(new BorderLayout());
            this.mod = mod;
            field = new JSlider(mod.min, mod.max, mod.getValue());
            mod.addPropertyChangeListener(this);
            field.addChangeListener(this);

            buttonAdd.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    mod.setValue(mod.getValue() + 10);
                }
            });

            buttonSub.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg0) {
                    mod.setValue(mod.getValue() - 10);
                }
            });

            add(buttonSub, BorderLayout.LINE_START);
            add(field, BorderLayout.CENTER);
            add(buttonAdd, BorderLayout.LINE_END);
            add(label, BorderLayout.PAGE_END);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            int v = (Integer) evt.getNewValue();
            Log.message("update(" + v + ")");
            field.setValue(v);
            label.setText(Integer.toString(v));
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            Log.message("actionPerformed(" + field.getValue() + ")");
            mod.setValue(field.getValue());
        }
    }
}
