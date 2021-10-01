package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;

import com.marginallyclever.convenience.log.Log;

public class ConversationHistoryList extends JPanel {
	private static final long serialVersionUID = 6287436679006933618L;
	private DefaultListModel<ConversationEvent> listModel = new DefaultListModel<ConversationEvent>();
	private JList<ConversationEvent> listView = new JList<ConversationEvent>(listModel);
	private JFileChooser chooser = new JFileChooser();

	private JButton bNew = new JButton("New");
	private JButton bSave = new JButton("Save");
	private JButton bLoad = new JButton("Load");
	private JButton bDelete = new JButton("Delete");

	
	public ConversationHistoryList() {
		super();
		createCellRenderingSystem();

		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		this.setBorder(BorderFactory.createTitledBorder("ConversationHistoryList"));
		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);

		bar.add(bNew);
		bar.add(bLoad);
		bar.add(bSave);
		bar.add(bDelete);
		
		bNew.addActionListener( (e) -> runNewAction() );
		bLoad.addActionListener( (e) -> runLoadAction() );
		bSave.addActionListener( (e) -> runSaveAction() );
		bDelete.addActionListener( (e) -> runDeleteAction() );

		listView.addListSelectionListener((e)->{
			if(e.getValueIsAdjusting()) return;
			updateDeleteButtonAccess(bDelete);
		});
		
		updateDeleteButtonAccess(bDelete);
		
		return bar;
	}

	private void updateDeleteButtonAccess(JButton bDelete) {
		bDelete.setEnabled( listView.getSelectedIndex() != -1 );
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
					jc.setText(value.toString());
					if(!value.whoSpoke.contentEquals("You")) {
						jc.setForeground(Color.BLUE);
					}
				}
				return c;
			}
			
		});
	}

	private void runNewAction() {
		listModel.clear();
	}
	
	private void runLoadAction() {
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				loadFile(chooser.getSelectedFile());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, e1.getLocalizedMessage(),"runLoadAction error",JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
	
	private void loadFile(File file) throws IOException {
		BufferedReader fileReader = new BufferedReader(new FileReader(file));
		String line;
		while((line = fileReader.readLine()) != null) {
			addElement("You",line);
		}
		fileReader.close();
	}
	
	private void runSaveAction() {
		if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				saveFile(chooser.getSelectedFile());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, e1.getLocalizedMessage(),"runSaveAction error",JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
	
	private void saveFile(File file) throws IOException {
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(file));
		int size=listModel.getSize();
		for(int i=0;i<size;++i) {
			String str = listModel.get(i).toString();
			if(!str.endsWith("\n")) str+="\n";
			fileWriter.write(str);
		}
		fileWriter.close();
	}
	
	private void runDeleteAction() {
		int i = listView.getSelectedIndex();
		if(i==-1) return;
		listModel.remove(i);
		if(i>=listModel.getSize()) i = listModel.getSize()-1; 
		listView.setSelectedIndex(i);
	}

	public void clear() {
		runNewAction();
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
		listView.ensureIndexIsVisible(listModel.getSize()-1);
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
		frame.add(new ConversationHistoryList());
		frame.pack();
		frame.setVisible(true);
	}
}
