package com.marginallyclever.robotoverlord.experiments;

import com.marginallyclever.convenience.log.Log;
import org.apache.batik.ext.swing.GridBagConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;

/**
 * Rewind button, Play button, Stop button, and progress bar from 0...100.
 * Listen for events on each button.
 * @author Dan 
 *
 */
@Deprecated
public class TapeDeckPanel extends JPanel {
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = -8342180275906617044L;
	public static final int ACTION_STOP = 0;
	public static final int ACTION_PLAY = 1;
	public static final int ACTION_REWIND = 2;
	
	private final JButton bPlay = new JButton();
	private final JButton bStop = new JButton();
	private final JButton bRewind = new JButton();
	private final JProgressBar progressBar = new JProgressBar(0,100);

	private final ArrayList<ActionListener> listeners = new ArrayList<>();

	public TapeDeckPanel() {
		super();
		setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=0;
		c.weighty=1;
		add(bRewind,c);
		
		c.gridx++;
		add(bPlay,c);
		
		c.gridx++;
		add(bStop,c);
		
		c.gridx++;
		c.weightx=1;
		c.fill=GridBagConstants.HORIZONTAL;
		add(progressBar,c);
		
		bRewind.setText("\u23EE");
		bPlay.setText("\u25B6");
		bStop.setText("\u23F9");

		final Object parent = this;
		bRewind.addActionListener((e)->notifyListeners(new ActionEvent(parent,ACTION_REWIND,"rewind")));
		bPlay.addActionListener((e)->notifyListeners(new ActionEvent(parent,ACTION_PLAY,"play")));
		bStop.addActionListener((e)->notifyListeners(new ActionEvent(parent,ACTION_STOP,"stop")));
	}
	
	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("TextInterfaceToNetworkSession");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new TapeDeckPanel());
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * @param p 0...100
	 */
	public void setValue(int p) {
		progressBar.setValue(p);
	}
	
	public int getValue() {
		return progressBar.getValue();
	}

	// OBSERVER PATTERN

	public void addActionListener(ActionListener a) {
		listeners.add(a);
	}
	
	public void removeActionListener(ActionListener a) {
		listeners.remove(a);
	}
	
	private void notifyListeners(ActionEvent e) {
		for( ActionListener a : listeners ) {
			a.actionPerformed(e);
		}
	}
}
