package com.marginallyclever.robotOverlord.swingInterface;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.marginallyclever.robotOverlord.MessageListener;
import com.marginallyclever.robotOverlord.swingInterface.translator.Translator;

public class MessageMonitor extends JFrame implements MessageListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static final int LOG_LENGTH = 5000;
	
	// logging
	private JList<String> logArea;
	private DefaultListModel<String> listModel;
	private JScrollPane logPane;

	// command line
	private JPanel textInputArea;
	private JTextField commandLineText;
	private JButton commandLineSend;
	private JButton clearLog;

	public MessageMonitor() {
		super();
		
		listModel = new DefaultListModel<String>();
		logArea = new JList<String>(listModel);
		logPane = new JScrollPane(logArea); 

		// Now put all the parts together
		this.setLayout(new GridBagLayout());
		GridBagConstraints con1 = new GridBagConstraints();
		con1.gridx = 0;
		con1.gridy = 0;
		
		con1.weightx=1;
		con1.weighty=1;
		con1.fill=GridBagConstraints.BOTH;
		con1.anchor=GridBagConstraints.NORTHWEST;
		this.add(logPane,con1);
		con1.gridy++;


		con1.fill=GridBagConstraints.HORIZONTAL;
		con1.weightx=1;
		con1.weighty=0;
		this.add(getTextInputField(),con1);
	}


	private JPanel getTextInputField() {
		textInputArea = new JPanel();
		textInputArea.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		commandLineText = new JTextField(0);
		//commandLineText.setPreferredSize(new Dimension(10, 10));
		commandLineSend = new JButton(Translator.get("Send"));
		clearLog = new JButton(Translator.get("Clear"));
		//commandLineSend.setHorizontalAlignment(SwingConstants.EAST);
		c.gridwidth=4;
		c.weightx=1;
		c.fill=GridBagConstraints.HORIZONTAL;
		c.gridx=c.gridy=0;
		textInputArea.add(commandLineText,c);
		c.gridwidth=1;
		c.gridx=4;
		c.weightx=0;
		textInputArea.add(commandLineSend,c);
		c.gridwidth=1;
		c.gridx=5;
		c.weightx=0;
		textInputArea.add(clearLog,c);
		
		commandLineText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				super.keyReleased(e);

				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					sendCommand();
				}
			}
		});
		commandLineSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendCommand();
			}
		});
		clearLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				clearLog();
			}
		});

		//textInputArea.setMinimumSize(new Dimension(100,50));
		//textInputArea.setMaximumSize(new Dimension(10000,50));

		return textInputArea;
	}
	

	public void finalize() throws Throwable  {
		super.finalize();
	}
	
	
	public void clearLog() {
		listModel.removeAllElements();
	}
	

	public void sendCommand() {
		//String command = commandLineText.getText();
		
		commandLineText.setText("");
	}


	@Override
	public void messageEvent(String message, Object sender) {
		// remove the 
		//if (msg.indexOf(';') != -1) msg = msg.substring(0, msg.indexOf(';'));
		message = message.trim();
		message = message.replace("\n", "<br>\n") + "\n";
		message = message.replace("\n\n", "\n");
		if(message.length()==0) return;
		
		listModel.addElement(message);
		if(listModel.size()>LOG_LENGTH) {
			listModel.remove(0);
		}
		logArea.ensureIndexIsVisible(listModel.getSize()-1);
	}

}
