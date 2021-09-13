package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;

public class ConversationHistoryList extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6287436679006933618L;
	
	private DefaultListModel<ConversationEvent> listModel = new DefaultListModel<ConversationEvent>();
	private JList<ConversationEvent> listView = new JList<ConversationEvent>(listModel);
	private JScrollPane scrollPane = new JScrollPane(listView);
	private JPanel editPanel = setupEditPanel();
	private JFileChooser chooser = new JFileChooser();
	private JButton bNew, bSave, bLoad, bDelete;
	
	public ConversationHistoryList() {
		super();
		
		createCellRenderingSystem();
		
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
	
	private void createCellRenderingSystem() {
		listView.setCellRenderer(new ListCellRenderer<ConversationEvent>() {
			private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer(); 
			
			@Override
			public Component getListCellRendererComponent(JList<? extends ConversationEvent> list,
					ConversationEvent value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if(c instanceof JLabel) {
					JLabel jc = (JLabel)c;
					jc.setText(value.whoSpoke+"> "+value.whatWasSaid);
					if(!value.whoSpoke.contentEquals("You")) {
						jc.setForeground(Color.BLUE);
					}
				}
				return c;
			}
			
		});
		
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
							addElement("You",str);
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
							ConversationEvent evt = listModel.get(i);
							String str = evt.whatWasSaid;
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

	public void addElement(String src,String str) {
		listModel.addElement(new ConversationEvent(src, str));
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
}
