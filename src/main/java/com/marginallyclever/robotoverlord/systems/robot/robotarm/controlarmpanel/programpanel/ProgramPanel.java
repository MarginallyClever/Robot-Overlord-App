package com.marginallyclever.robotoverlord.systems.robot.robotarm.controlarmpanel.programpanel;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotoverlord.components.GCodePathComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.systems.render.gcodepath.PathWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.vecmath.Point3d;
import java.awt.*;
import java.io.*;

/**
 * A panel that displays a list of {@link ProgramEvent}s for controlling a robot.
 */
public class ProgramPanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(ProgramPanel.class);
	private final DefaultListModel<ProgramEvent> listModel = new DefaultListModel<>();
	private final JList<ProgramEvent> listView = new JList<>(listModel);
	private final JFileChooser chooser = new JFileChooser();
	private final JButton bNew = new JButton("New");
	private final JButton bSave = new JButton("Save");
	private final JButton bLoad = new JButton("Load");
	private final JButton bDelete = new JButton("Delete");
	private final JButton bCopy = new JButton("Copy");
	private final JButton bAdd = new JButton("Add");
	private final JButton bRewind = new JButton("Rewind");
	private final JButton bStep = new JButton("Step");
	private final JButton bNickname = new JButton("Nickname");
	
	private final RobotComponent myArm;
	private final GCodePathComponent path;

	public ProgramPanel(RobotComponent arm, GCodePathComponent path) {
		super();
		this.myArm = arm;
		this.path = path;

		createCellRenderingSystem();
		
		JScrollPane scrollPane = new JScrollPane(listView);
		scrollPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listView.setMaximumSize(new Dimension(300,Integer.MAX_VALUE));
		listView.setTransferHandler(new ListItemTransferHandler());
		listView.setDropMode(DropMode.INSERT);
		listView.setDragEnabled(true);
		
		this.setLayout(new BorderLayout());
		this.add(getToolBar(), BorderLayout.PAGE_START);
		this.add(scrollPane, BorderLayout.CENTER);

		loadGCodePath();
	}

	private void loadGCodePath() {
		if(path==null) return;
		PathWalker walker = path.getPathWalker();
		if(walker==null) return;

		// make a copy of the arm, so we don't mess up the real one.
		Entity newCopy = myArm.getEntity().deepCopy();
		RobotComponent temp = newCopy.getComponent(RobotComponent.class);
		temp.findBones();

		int i=0;
		while (walker.hasNext()) {
			walker.next();

			try {
				// Convert the Cartesian values to FK joint values using the PathWalker and the Robot interface
				Point3d endEffectorTargetPosition = walker.getCurrentPosition();
				temp.set(Robot.END_EFFECTOR_TARGET_POSITION, endEffectorTargetPosition);

				// Get the resulting joint values
				double[] jointValues = (double[]) temp.get(Robot.ALL_JOINT_VALUES);

				// Create a new ProgramEvent with the FK joint values and add it to the list
				ProgramEvent event = new ProgramEvent(jointValues);
				event.setNickname("step "+i);
				insertWhereAppropriate(event);
			} catch (Exception e) {
				logger.error("step "+i+": "+e.getMessage());
			}
			++i;
		}
	}

	private void createCellRenderingSystem() {
		listView.setCellRenderer(new ListCellRenderer<>() {
			private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
			
			@Override
			public Component getListCellRendererComponent(JList<? extends ProgramEvent> list,
					ProgramEvent value, int index, boolean isSelected, boolean cellHasFocus) {
				Component c = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				
				if(c instanceof JLabel) {
					JLabel jc = (JLabel)c;
					jc.setText(value.getFormattedDisplay());
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
		bar.addSeparator();
		bar.add(bAdd);
		bar.add(bNickname);
		bar.add(bCopy);
		bar.add(bDelete);
		bar.addSeparator();
		bar.add(bRewind);
		bar.add(bStep);

		bNew.addActionListener((e)-> runNewAction() );
		bSave.addActionListener((e)-> runSaveAction() );
		bLoad.addActionListener((e)-> runLoadAction() );
		bAdd.addActionListener((e)-> runAddAction() );
		bCopy.addActionListener((e)-> runCopyAction() );
		bDelete.addActionListener((e)-> runDeleteAction() );
		bRewind.addActionListener((e)-> rewind() );
		bStep.addActionListener((e)-> step() );
		bNickname.addActionListener((e)-> nickname() );

		listView.addListSelectionListener((e)->{
			if(e.getValueIsAdjusting()) return;
			updateButtonAccess(bDelete);
		});
		
		updateButtonAccess(bDelete);
		
		return bar;
	}

	private void nickname() {
		int i = listView.getSelectedIndex();
		ProgramEvent pe = listModel.get(i); 
		String oldName = pe.getNickname();
		String newName = JOptionPane.showInputDialog("New nickname:",oldName);
		pe.setNickname(newName);
		listView.repaint();
	}

	public void rewind() {
		listView.setSelectedIndex(0);
	}

	private void updateButtonAccess(JButton bDelete) {
		boolean somethingSelected = (listView.getSelectedIndex() != -1);
		bDelete.setEnabled(somethingSelected);
		bStep.setEnabled(somethingSelected);
		bNickname.setEnabled(somethingSelected);
	}
	
	private void runNewAction() {
		listModel.clear();
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
		int count = (int)myArm.get(Robot.NUM_JOINTS);
		double [] angles = new double[count];
		for (int i = 0; i < count; ++i) {
			myArm.set(Robot.ACTIVE_JOINT, i);
			angles[i] = (double)myArm.get(Robot.JOINT_VALUE);
		}

		insertWhereAppropriate(new ProgramEvent(angles));
	}

	private void runCopyAction() {
		int i = listView.getSelectedIndex();
		if(i==-1) return;
		
		insertWhereAppropriate(new ProgramEvent(listModel.getElementAt(i)));
	}

	/**
	 * if something is selected, add immediately after the selection.
	 * if nothing is selected, add to the end of the list.
	 */
	private void insertWhereAppropriate(ProgramEvent pe) {
		int index = listView.getSelectedIndex();
		if(index == -1 || index == listModel.getSize()) {
			listModel.addElement(pe);
		} else {
			listModel.add(index+1,pe);
		}
	}

	private void runDeleteAction() {
		int i = listView.getSelectedIndex();
		if(i==-1) return;
		listModel.remove(i);
		if(i>=listModel.getSize()) i = listModel.getSize()-1; 
		listView.setSelectedIndex(i);
	}

	/**
	 * Move the play head to the lineNumber-th instruction.
	 */
	public void setLineNumber(int lineNumber) {
		listView.setSelectedIndex(lineNumber);
	}

	/**
	 * @return the currently selected instruction.
	 */
	public int getLineNumber() {
		return listView.getSelectedIndex();
	}

	/**
	 * @return the total number of instructions in the buffer.
	 */
	public int getMoveCount() {
		return listModel.getSize();
	}

	/**
	 * Tell the {@link Robot} to move to the currently selected instruction and
	 * advance the selected instruction by one. If there are no further instructions
	 * the selection is nullified.
	 */
	public void step() {
		int now = listView.getSelectedIndex();
		if(now == -1) return;

		// Increment the line as soon as possible so that step() does not get called
		// twice on the same line.
		listView.setSelectedIndex(now + 1);

		ProgramEvent move = listModel.get(now);
		// logger.info("Step to ("+now+"):"+move.toString());
		int count = (int)myArm.get(Robot.NUM_JOINTS);
		double [] angles = move.getAngles();
		for (int i = 0; i < count; ++i) {
			myArm.set(Robot.ACTIVE_JOINT, i);
			myArm.set(Robot.JOINT_VALUE,angles[i]);
		}

		int selected = listView.getSelectedIndex();
		listView.ensureIndexIsVisible(selected);
		if (selected == now) {
			// could not advance. reached the end.
			listView.clearSelection();
		}
	}
	
	// TEST

	public static void main(String[] args) {
		Log.start();
		JFrame frame = new JFrame("ProgramInterface");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(new ProgramPanel(new RobotComponent(),null));
		frame.pack();
		frame.setVisible(true);
	}
}
