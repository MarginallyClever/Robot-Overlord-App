package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;

import com.marginallyclever.convenience.log.Log;

@Deprecated
public class HistoryList extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3986789901447241422L;
	private DefaultListModel<String> listModel = new DefaultListModel<String>();
	private JList<String> listView = new JList<String>(listModel);
	private JScrollPane scrollPane = new JScrollPane(listView);
	private JPanel editPanel = setupEditPanel();
	private JFileChooser chooser = new JFileChooser();
	private JButton bNew, bSave, bLoad, bDelete;
	
	public HistoryList() {
		super();
		
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx=0;
		c.gridy=0;
		c.weightx=1;
		c.weighty=0;
		add(editPanel,c);

		c.gridy++;
		c.fill = GridBagConstraints.BOTH;
		c.weightx=1;
		c.weighty=1;
		add(scrollPane,c);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
	}
	
	private JPanel setupEditPanel() {
		final JPanel panel = new JPanel();
				
		bNew = new JButton(new AbstractAction("Clear") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				listModel.clear();
			}
		});
		
		bLoad = new JButton(new AbstractAction("Load") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					listModel.clear();
					try {
						BufferedReader fileReader = new BufferedReader(new FileReader(file));
						int size=listModel.getSize();
						for(int i=0;i<size;++i) {
							String str = fileReader.readLine();
							addElement(str);
						}
						fileReader.close();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(),"Load error",JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		
		bSave = new JButton(new AbstractAction("Save") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(chooser.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					try {
						BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
						int size=listModel.getSize();
						for(int i=0;i<size;++i) {
							String str = listModel.get(i);
							if(!str.endsWith("\n")) str+="\n";
							fileWriter.write(str);
						}
						fileWriter.close();
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(panel, e1.getLocalizedMessage(),"Save error",JOptionPane.ERROR_MESSAGE);
						e1.printStackTrace();
					}
				}
			}
		});
		
		bDelete = new JButton(new AbstractAction("Delete") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int i = listView.getSelectedIndex();
				if(i==-1) return;
				listModel.remove(i);
				if(i>=listModel.getSize()) i = listModel.getSize()-1; 
				listView.setSelectedIndex(i);
			}
		});
		
		panel.setLayout(new FlowLayout(FlowLayout.LEADING));
		panel.add(bNew);
		panel.add(bLoad);
		panel.add(bSave);
		panel.add(bDelete);

		listView.addListSelectionListener((e)->{
			if(e.getValueIsAdjusting()) return;
			bDelete.setEnabled( listView.getSelectedIndex() != -1 );
		});
		
		return panel;
	}

	
	public void addListSelectionListener(ListSelectionListener listener) {
		listView.addListSelectionListener(listener);
	}

	public int getSelectedIndex() {
		return listView.getSelectedIndex();
	}

	public String getSelectedValue() {
		return listView.getSelectedValue().toString();
	}

	public void addElement(String element) {
		listModel.addElement(element);
	}
	
	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		listView.setEnabled(state);
		bNew.setEnabled(state);
		bSave.setEnabled(state);
		bLoad.setEnabled(state);
		bDelete.setEnabled(state);
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("TextInterfaceWithHistory");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new HistoryList());
		frame.pack();
		frame.setVisible(true);
	}
}
