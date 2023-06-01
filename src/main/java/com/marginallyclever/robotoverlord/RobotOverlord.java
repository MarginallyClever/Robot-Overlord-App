package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel3;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.renderpanel.OpenGLRenderPanel;
import com.marginallyclever.robotoverlord.renderpanel.RenderPanel;
import com.marginallyclever.robotoverlord.systems.motor.MotorSystem;
import com.marginallyclever.robotoverlord.systems.physics.PhysicsSystem;
import com.marginallyclever.robotoverlord.systems.render.RenderSystem;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.swinginterface.componentmanagerpanel.ComponentManagerPanel;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.actions.*;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanel;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.systems.*;
import com.marginallyclever.robotoverlord.systems.robot.RobotGripperSystem;
import com.marginallyclever.robotoverlord.systems.robot.crab.CrabRobotSystem;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.ArmRobotSystem;
import com.marginallyclever.robotoverlord.systems.robot.dog.DogRobotSystem;
import com.marginallyclever.robotoverlord.systems.robot.robotarm.ProgramExecutorSystem;
import com.marginallyclever.util.PropertiesFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * {@code RobotOverlord} is the top-level controller of an application to educate robots.
 * It is built around good design patterns.
 * See <a href="https://github.com/MarginallyClever/Robot-Overlord-App">https://github.com/MarginallyClever/Robot-Overlord-App</a>
 *
 * @author Dan Royer
 */
public class RobotOverlord {
	private static final Logger logger = LoggerFactory.getLogger(RobotOverlord.class);

	public static final String APP_TITLE = "Robot Overlord";
	public static final String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";
	private static final String KEY_WINDOW_WIDTH = "windowWidth";
	private static final String KEY_WINDOW_HEIGHT = "windowHeight";
	private static final String KEY_WINDOW_X = "windowX";
	private static final String KEY_WINDOW_Y = "windowY";
	private static final String KEY_IS_FULLSCREEN = "isFullscreen";
	private static final String KEY_LAST_DIRECTORY_IMPORT = "LastDirectoryImport";
	private static final String KEY_LAST_DIRECTORY_SAVE = "LastDirectorySave";
	private static final String KEY_LAST_DIRECTORY_LOAD = "LastDirectoryLoad";

	public static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter("RO files", "RO");

	// used for checking the application version with the GitHub release, for "there is a new version available!" notification
	public static final String VERSION;

	static {
		try {
			VERSION = PropertiesFileHelper.getVersionPropertyValue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// settings
	private final Preferences prefs = Preferences.userRoot().node("Evil Overlord");
    //private RecentFiles recentFiles = new RecentFiles();

	private final Project project = new Project();

	/**
	 * The main frame of the GUI
	 */
	private JFrame mainFrame;

	/**
	 * The frame that contains the log panel.
	 */
	private static JFrame logFrame;

	/**
	 * The panel that contains the OpenGL canvas.
	 */
	private final RenderPanel renderPanel;

	/**
	 * The menu bar of the main frame.
	 */
    private final JMenuBar mainMenu = new JMenuBar();

	/**
	 * The left contains the renderPanel.  The right contains the rightFrameSplitter.
	 */
	private final JSplitPane splitLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	/**
	 * The panel that contains the entity tree and the component panel.
	 */
	private final JSplitPane rightFrameSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	/**
	 * Tree componentpanel of all Entities in the scene.
	 */
	private final EntityTreePanel entityTreePanel;

	/**
	 * Collated componentpanel of all components in all selected Entities.
	 */
	private final ComponentManagerPanel componentManagerPanel;

	private final RecentFiles recentFiles = new RecentFiles();

	private final List<EntitySystem> systems = new ArrayList<>();

	public static void main(String[] argv) {
		Log.start();
		//logFrame = LogPanel.createFrame();
		logFrame = LogPanel3.createFrame(Log.getLogLocation());
		PathHelper.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(RobotOverlord::new);
	}


	public RobotOverlord() {
		super();

		if(GraphicsEnvironment.isHeadless()) {
			throw new RuntimeException("RobotOverlord cannot be run headless yet.");
		}

		Translator.start();
		UndoSystem.start();
		preferencesLoad();

		buildSystems();

		buildMainFrame();
		entityTreePanel = new EntityTreePanel(project.getEntityManager());
		componentManagerPanel = new ComponentManagerPanel(project.getEntityManager(),systems);

		//renderPanel = new OpenGLTestOrthographic(project.getEntityManager());
		//renderPanel = new OpenGLTestPerspective(project.getEntityManager());
		//renderPanel = new OpenGLTestStencil(project.getEntityManager());
		renderPanel = new OpenGLRenderPanel(project.getEntityManager());

		renderPanel.setUpdateCallback((dt)->{
			for(EntitySystem system : systems) system.update(dt);
		});

		layoutComponents();
		refreshMainMenu();

		listenToClipboardChanges();

		ProjectClearAction action = new ProjectClearAction(project);
		action.clearScene();
		action.addDefaultEntities();

		updateActionEnableStatus();

		logger.info("** READY **");
    }

	private void buildSystems() {
		addSystem(new PhysicsSystem());
		addSystem(new RenderSystem());
		addSystem(new OriginAdjustSystem());
		//addSystem(new SoundSystem());
		addSystem(new ArmRobotSystem(project.getEntityManager()));
		addSystem(new DogRobotSystem(project.getEntityManager()));
		addSystem(new CrabRobotSystem(project.getEntityManager()));
		addSystem(new ProgramExecutorSystem(project.getEntityManager()));
		addSystem(new RobotGripperSystem(project.getEntityManager()));
		addSystem(new MotorSystem(project.getEntityManager()));
	}

	private void addSystem(EntitySystem system) {
		systems.add(system);
		//system.addListener(this);
	}

	private void listenToClipboardChanges() {
		Clipboard.addListener(this::updateActionEnableStatus);
	}

	private void preferencesLoad() {
		ProjectImportAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, System.getProperty("user.dir")));
		ProjectLoadAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, System.getProperty("user.dir")));
		ProjectSaveAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, System.getProperty("user.dir")));
	}

	private void preferencesSave() {
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, ProjectImportAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, ProjectLoadAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, ProjectSaveAction.getLastDirectory());
	}

	private JComponent buildEntityManagerPanel() {
        logger.info("buildEntityManagerPanel()");

		return entityTreePanel;
	}

	private void layoutComponents() {
		// the right hand top/bottom split
		rightFrameSplitter.add(buildEntityManagerPanel());
		rightFrameSplitter.add(new JScrollPane(componentManagerPanel));
		// make sure the master panel can't be squished.
        Dimension minimumSize = new Dimension(360,300);
        rightFrameSplitter.setMinimumSize(minimumSize);
        // if the window resizes, give top and bottom halves equal share of the real estate
		rightFrameSplitter.setResizeWeight(0.25);

		// left/right split
        splitLeftRight.add(renderPanel.getPanel());
        splitLeftRight.add(rightFrameSplitter);
        // if the window resizes, give left half as much real estate as it can get.
        splitLeftRight.setResizeWeight(1);

        mainFrame.add(splitLeftRight);
	}

	private void buildMainFrame() {
		logger.info("buildMainFrame()");
		// start the main application frame - the largest visible rectangle on the screen with the minimize/maximize/close buttons.
        mainFrame = new JFrame( APP_TITLE + " " + VERSION );
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setJMenuBar(mainMenu);
		setWindowSizeAndPosition();
		setupDropTarget();
        mainFrame.addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
			@Override
			public void windowClosing(WindowEvent e) {
				confirmClose();
				super.windowClosing(e);
			}
		});

		mainFrame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				saveWindowSizeAndPosition();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				saveWindowSizeAndPosition();
			}
		});
		mainFrame.setVisible(true);
	}

	private void setWindowSizeAndPosition() {
		logger.info("Set window size and position");

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		boolean isFullscreen = prefs.getBoolean("isFullscreen",false);
		if(isFullscreen) {
			mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		} else {
			int windowW = prefs.getInt(KEY_WINDOW_WIDTH, dim.width);
			int windowH = prefs.getInt(KEY_WINDOW_HEIGHT, dim.height);
			int windowX = prefs.getInt(KEY_WINDOW_X, (dim.width - windowW)/2);
			int windowY = prefs.getInt(KEY_WINDOW_Y, (dim.height - windowH)/2);
			mainFrame.setBounds(windowX, windowY,windowW, windowH);
		}
	}

	// remember window location for next time.
	private void saveWindowSizeAndPosition() {
		int state = mainFrame.getExtendedState();
		boolean isFullscreen = ((state & JFrame.MAXIMIZED_BOTH)!=0);
		prefs.putBoolean(KEY_IS_FULLSCREEN, isFullscreen);
		if(!isFullscreen) {
			Dimension frameSize = mainFrame.getSize();
			prefs.putInt(KEY_WINDOW_WIDTH, frameSize.width);
			prefs.putInt(KEY_WINDOW_HEIGHT, frameSize.height);
			Point p = mainFrame.getLocation();
			prefs.putInt(KEY_WINDOW_X, p.x);
			prefs.putInt(KEY_WINDOW_Y, p.y);
		}
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	private void refreshMainMenu() {
		mainMenu.removeAll();
		mainMenu.add(createFileMenu());
		mainMenu.add(createEditMenu());
		mainMenu.add(createDemoMenu());
		mainMenu.add(createHelpMenu());
        //mainMenu.updateUI();
		mainFrame.revalidate();
	}

	private JComponent createFileMenu() {
		JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.File"));

		menu.add(new ProjectClearAction(project));
		menu.add(new ProjectLoadAction(project));
		if(recentFiles.size()>0) menu.add(createRecentFilesMenu());
		menu.add(new ProjectImportAction(project));
		menu.add(new ProjectSaveAction(project));
		menu.add(new JSeparator());
		menu.add(new QuitAction(this));

		return menu;
	}

	private JMenu createRecentFilesMenu() {
		JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.RecentFiles"));
		for(String filename : recentFiles.getFilenames()) {
			AbstractAction loader = new AbstractAction(filename) {
				@Override
				public void actionPerformed(ActionEvent e) {
					ProjectLoadAction projectLoadAction = new ProjectLoadAction(project);
					File file = new File(filename);
					if(file.exists()) {
						projectLoadAction.loadIntoScene(file,mainFrame);
						recentFiles.add(filename);
					} else {
						recentFiles.remove(filename);
					}
					refreshMainMenu();
				}
			};
			loader.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_1+menu.getItemCount(), InputEvent.CTRL_DOWN_MASK));
			menu.add(loader);
		}
		return menu;
	}

	private JComponent createEditMenu() {
		JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.Edit"));
		menu.add(new JMenuItem(UndoSystem.getCommandUndo()));
		menu.add(new JMenuItem(UndoSystem.getCommandRedo()));
		return menu;
	}

	private JComponent createDemoMenu() {
		JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.Demos"));
		//menu.add(new JMenuItem(new DemoAction(this,new ODEPhysicsDemo())));
		menu.addSeparator();
		menu.add(new JMenuItem(new ShowRobotLibraryPanel(this::refreshMainMenu)));
		buildAvailableScenesTree(menu);
		return menu;
	}

	/**
	 * Searches for all files matching <code>scenes/[owner]/[repo]/[tag]/something.ro</code>
	 * builds <code>[owner]/[repo]/[tag]</code> to the JMenu tree AND adds a
	 * new SceneImportAction(this, something.ro)) to the leaf of the tree.
	 * @param menu the JMenu that is the root of the new menu tree.
	 */
	private void buildAvailableScenesTree(JMenu menu) {
		// scan 'plugins' folder for sub-folders.  make them submenus.
		File rootDirectory = new File(PathHelper.APP_PLUGINS);

		if (!rootDirectory.isDirectory()) {
			return;
		}

		boolean first=true;

		File[] level1Dirs = rootDirectory.listFiles(File::isDirectory);
		if (level1Dirs == null) return;

		for (File level1Dir : level1Dirs) {
			JMenu level1Menu = new JMenu(level1Dir.getName());

			File[] level2Dirs = level1Dir.listFiles(File::isDirectory);
			if (level2Dirs == null) continue;

			for (File level2Dir : level2Dirs) {
				JMenu level2Menu = new JMenu(level2Dir.getName());

				File[] level3Dirs = level2Dir.listFiles(File::isDirectory);
				if (level3Dirs == null) continue;

				for (File level3Dir : level3Dirs) {
					File[] roFiles = level3Dir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ro"));
					if (roFiles == null || roFiles.length == 0) continue;

					JMenu level3Menu = new JMenu(level3Dir.getName());

					for (File roFile : roFiles) {
						level3Menu.add(new JMenuItem(new ProjectImportAction(project, roFile)));
					}

					// we found something, add the parent menu.
					if(level3Menu.getItemCount()!=0) {
						level2Menu.add(level3Menu);
					}
				}

				// we found something, add the parent menu.
				if(level2Menu.getItemCount()!=0) {
					level1Menu.add(level2Menu);
				}
			}

			// we found something, add the parent menu.
			if(level1Menu.getItemCount()!=0) {
				// first time through, add a separator.
				if(first) {
					first = false;
					menu.add(new JSeparator());
				}
				menu.add(level1Menu);
			}
		}
	}

	private JComponent createHelpMenu() {
		JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.Help"));
		JMenuItem buttonViewLog = new JMenuItem(Translator.get("RobotOverlord.Menu.ShowLog"));
		buttonViewLog.addActionListener((e) -> showLogDialog() );
		menu.add(buttonViewLog);
		menu.add(new JMenuItem(new ForumsAction()));
		menu.add(new JMenuItem(new CheckForUpdateAction()));
		menu.add(new JMenuItem(new AboutAction()));
		return menu;
	}

	private void showLogDialog() {
		logFrame.setVisible(true);
	}

    private void updateSelectEntities() {
		entityTreePanel.setSelection(Clipboard.getSelectedEntities());
		renderPanel.updateSubjects(Clipboard.getSelectedEntities());
		updateComponentPanel();
	}

	public void updateComponentPanel() {
		componentManagerPanel.refreshContentsFromClipboard();
	}

	public void confirmClose() {
        int result = JOptionPane.showConfirmDialog(
				mainFrame,
				Translator.get("RobotOverlord.quitConfirm"),
				Translator.get("RobotOverlord.quitTitle"),
				JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
			preferencesSave();

			// Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
			new Thread(() -> {
				renderPanel.stopAnimationSystem();
				mainFrame.dispose();
				Log.end();
			}).start();
        }
	}

	/**
	 * Tell all Actions to check if they are active.
	 */
	private void updateActionEnableStatus() {
		updateSelectEntities();

		entityTreePanel.updateActionEnableStatus();
	}

	private void setupDropTarget() {
		logger.debug("adding drag + drop support...");
		new DropTarget(mainFrame,new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent event) {
				try {
					Transferable tr = event.getTransferable();
					DataFlavor[] flavors = tr.getTransferDataFlavors();
					for (DataFlavor flavor : flavors) {
						logger.debug("Possible flavor: {}", flavor.getMimeType());
						if (flavor.isFlavorJavaFileListType()) {
							event.acceptDrop(DnDConstants.ACTION_COPY);
							Object object = tr.getTransferData(flavor);
							if (object instanceof List<?>) {
								List<?> list = (List<?>) object;
								if (list.size() > 0) {
									object = list.get(0);
									if (object instanceof File) {
										File file = (File) object;
										if(importMesh(file.getAbsolutePath())) {
											event.dropComplete(true);
											return;
										}
										if(importScene(file)) {
											event.dropComplete(true);
											return;
										}
									}
								}
							}
						}
					}
					logger.debug("Drop failed: {}", event);
					event.rejectDrop();
				} catch (Exception e) {
					logger.error("Drop error", e);
					event.rejectDrop();
				}
			}
		});
	}

	private boolean importScene(File file) {
		ProjectImportAction action = new ProjectImportAction(project);
		return action.loadFile(file,mainFrame);
	}

	private boolean importMesh(String absolutePath) {
		if(!MeshFactory.canLoad(absolutePath)) return false;

		logger.debug("importing mesh "+absolutePath);
        try {
			// create entity.
			Entity entity = new Entity();
			entity.setName(getFilenameWithoutExtensionFromPath(absolutePath));
			// add shape, which will add pose and material.
			ShapeComponent shape = new MeshFromFile(absolutePath);
			entity.addComponent(shape);
			// move entity to camera orbit point so that it is visible.
			PoseComponent pose = entity.getComponent(PoseComponent.class);
			pose.setPosition(project.getEntityManager().getCamera().getOrbitPoint());

			// add entity to scene.
			UndoSystem.addEvent(new EntityAddEdit(project.getEntityManager(), project.getEntityManager().getRoot(),entity));
		} catch(Exception e) {
			logger.error("Error opening file",e);
			return false;
		}
		return true;
	}

	private String getFilenameWithoutExtensionFromPath(String absolutePath) {
		File f = new File(absolutePath);
		String fullName = f.getName();
		return fullName.substring(0,fullName.lastIndexOf('.'));
	}
}
