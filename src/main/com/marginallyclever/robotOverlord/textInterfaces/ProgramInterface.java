package com.marginallyclever.robotOverlord.textInterfaces;

import java.awt.BorderLayout;
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

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.robots.sixi3.Sixi3IK;

public class ProgramInterface extends JPanel {
	private static final long serialVersionUID = 1L;
	private DefaultListModel<ProgramEvent> listModel = new DefaultListModel<ProgramEvent>();
	private JList<ProgramEvent> listView = new JList<ProgramEvent>(listModel);
	private JFileChooser chooser = new JFileChooser();

	private JButton bNew = new JButton("Clear");
	private JButton bSave = new JButton("Save");
	private JButton bLoad = new JButton("Load");
	private JButton bDelete = new JButton("Delete");
	private JButton bAdd = new JButton("Add");
	private JButton bRewind = new JButton("Rewind");
	private JButton bStep = new JButton("Step");
	
	private Sixi3IK mySixi3;
		
	public ProgramInterface(Sixi3IK sixi3) {
		super();
		mySixi3 = sixi3;
		createCellRenderingSystem();
		
		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		this.setBorder(BorderFactory.createTitledBorder("ProgramHistoryList"));
		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);
	}
	
	private void createCellRenderingSystem() {
		listView.setCellRenderer(new ListCellRenderer<ProgramEvent>() {
			private DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer(); 
			
			@Override
			public Component getListCellRendererComponent(JList<? extends ProgramEvent> list,
					ProgramEvent value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if(c instanceof JLabel) {
					JLabel jc = (JLabel)c;
					jc.setText(value.toString());
				}
				return c;
			}
			
		});
	}
	
	private JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setRollover(true);
		
		bar.add(bNew);
		bar.add(bSave);
		bar.add(bLoad);
		bar.add(bAdd);
		bar.add(bDelete);
		bar.add(bRewind);
		bar.add(bStep);

		bNew.addActionListener((e)-> runNewAction() );
		bSave.addActionListener((e)-> runSaveAction() );
		bLoad.addActionListener((e)-> runLoadAction() );
		bAdd.addActionListener((e)-> runAddAction() );
		bDelete.addActionListener((e)-> runDeleteAction() );
		bRewind.addActionListener((e)-> runRewindAction() );
		bStep.addActionListener((e)-> runStepAction() );

		listView.addListSelectionListener((e)->{
			if(e.getValueIsAdjusting()) return;
			updateDeleteButtonAccess(bDelete);
		});
		
		updateDeleteButtonAccess(bDelete);
		
		return bar;
	}

	private void runRewindAction() {
		listView.setSelectedIndex(0);
	}

	private void runStepAction() {
		int now = listView.getSelectedIndex();
		ProgramEvent pe = listModel.get(now);		
		mySixi3.setAngles(pe.getAngles());
		listView.setSelectedIndex(now+1);
	}

	private void updateDeleteButtonAccess(JButton bDelete) {
		bDelete.setEnabled( listView.getSelectedIndex() != -1 );
	}
	
	private void runNewAction() {
		listView.clearSelection();
	}

	private void runSaveAction() {
		if(chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				saveFile(chooser.getSelectedFile());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, e1.getLocalizedMessage(),"Save error",JOptionPane.ERROR_MESSAGE);
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

	private void runLoadAction() {
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			try {
				loadFile(chooser.getSelectedFile());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(this, e1.getLocalizedMessage(),"Load error",JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		}
	}
	
	private void loadFile(File file) throws IOException {
		BufferedReader fileReader = new BufferedReader(new FileReader(file));
		int i=0;
		String line;		
		while((line = fileReader.readLine()) != null) {
			i++;
			if(line.startsWith(ProgramEvent.NAME)) {
				listModel.addElement(ProgramEvent.valueOf(line));
			} else {
				fileReader.close();
				throw new IOException(file.getAbsolutePath() + " ("+i+"): "+line);
			}
		}
		fileReader.close();
	}

	private void runAddAction() {
		listModel.addElement(new ProgramEvent(mySixi3.getAngles()));
	}

	private void runDeleteAction() {
		int i = listView.getSelectedIndex();
		if(i==-1) return;
		listModel.remove(i);
		if(i>=listModel.getSize()) i = listModel.getSize()-1; 
		listView.setSelectedIndex(i);
	}

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("ProgramInterface");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ProgramInterface(new Sixi3IK()));
		frame.pack();
		frame.setVisible(true);
	}
}
