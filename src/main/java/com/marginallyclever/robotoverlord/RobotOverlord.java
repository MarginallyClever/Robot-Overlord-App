package com.marginallyclever.robotoverlord;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.FPSAnimator;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.demos.DemoSpidee;
import com.marginallyclever.robotoverlord.entities.SkyBoxEntity;
import com.marginallyclever.robotoverlord.entities.ViewCube;
import com.marginallyclever.robotoverlord.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.swinginterface.*;
import com.marginallyclever.robotoverlord.swinginterface.actions.*;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.swinginterface.edits.SelectEdit;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanel;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanelEvent;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.robotoverlord.tools.EditorTool;
import com.marginallyclever.robotoverlord.tools.move.MoveTool;
import com.marginallyclever.util.PropertiesFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.vecmath.Vector2d;
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
import java.nio.IntBuffer;
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
public class RobotOverlord extends Entity {
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

	/**
	 * The scene being edited and all the entities therein.
	 */
	private final Scene scene = new Scene(System.getProperty("user.dir"));

	/**
	 * The list of entities selected in the editor.  This list is used by Actions.
	 */
	private transient final List<Entity> selectedEntities = new ArrayList<>();

	/**
	 * The list of entities copied to the clipboard.  This list is used by Actions.
	 */
	private transient Entity copiedEntities = new Entity();

	/**
	 * The list of actions registered in the editor.  This list is used for calls to
	 * {@link #updateActionEnableStatus()}.
	 */
	private final ArrayList<AbstractAction> actions = new ArrayList<>();

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
	private OpenGLRenderPanel renderPanel;

	/**
	 * The menu bar of the main frame.
	 */
    private JMenuBar mainMenu;

	/**
	 * The left contains the renderPanel.  The right contains the rightFrameSplitter.
	 */
	private final JSplitPane splitLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	/**
	 * The panel that contains the entity tree and the component panel.
	 */
	private final JSplitPane rightFrameSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	/**
	 * Tree view of all Entities in the scene.
	 */
	private final EntityTreePanel entityTree = new EntityTreePanel();

	/**
	 * Collated view of all components in all selected Entities.
	 */
	private final ComponentPanel componentPanel = new ComponentPanel(this);
	
	private EntityRenameAction entityRenameAction;
	private EntityDeleteAction entityDeleteAction;

 	private RobotOverlord() {
		super("");

		this.addComponent(new PoseComponent());

		if(GraphicsEnvironment.isHeadless()) {
			throw new RuntimeException("RobotOverlord cannot be run headless yet.");
		}

		Translator.start();
		SoundSystem.start();
		UndoSystem.start();

		preferencesLoad();

		buildMainFrame();
		buildMainMenu();
		createSimulationPanel();
		layoutComponents();
		renderPanel.startAnimationSystem();

		entityTree.addEntity(scene);
		scene.addSceneChangeListener(entityTree);

		addEntity(scene);

		SceneClearAction action = new SceneClearAction(this);
		action.clearScene();
		action.addDefaultEntities();

		Log.message("** READY **");
    }

	private void preferencesLoad() {
		SceneImportAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, System.getProperty("user.dir")));
		SceneLoadAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, System.getProperty("user.dir")));
		SceneSaveAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, System.getProperty("user.dir")));
	}

	private void preferencesSave() {
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, SceneImportAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, SceneLoadAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, SceneSaveAction.getLastDirectory());
	}

	private void createSimulationPanel() {
		renderPanel = new OpenGLRenderPanel(this,scene);
	}

	public static void main(String[] argv) {
		logFrame = LogPanel.createFrame();
		Log.start();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}
		
	    //Schedule a job for the event-dispatching thread:
	    //creating and showing this application's GUI.
	    javax.swing.SwingUtilities.invokeLater(RobotOverlord::new);
	}

	private JComponent buildEntityManagerPanel() {
        Log.message("buildEntityManagerPanel()");

		entityTree.addEntityTreePanelListener((e)-> {
			if (e.eventType == EntityTreePanelEvent.SELECT) {
				setSelectedEntities(e.subjects);
			}
		});

		entityTree.setPopupMenu(buildEntityTreePopupMenu());

		return entityTree;
	}

	private JPopupMenu buildEntityTreePopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		EntityAddChildAction EntityaddChildAction = new EntityAddChildAction(this);

		for( AbstractAction action : actions ) {
			if(action instanceof EntityCopyAction || action instanceof EntityPasteAction) {
				popupMenu.add(action);
			}
		}

		entityRenameAction = new EntityRenameAction(this);
		entityRenameAction.setEnabled(false);

		actions.add(EntityaddChildAction);
		actions.add(entityRenameAction);

		popupMenu.add(EntityaddChildAction);
		popupMenu.add(entityRenameAction);
		popupMenu.add(entityDeleteAction);

		popupMenu.add(new ComponentAddAction(this));

		return popupMenu;
	}

	private void layoutComponents() {
        Log.message("layoutComponents()");
        
		// the right hand stuff			        
		rightFrameSplitter.add(buildEntityManagerPanel());
		rightFrameSplitter.add(new JScrollPane(componentPanel));
		// make sure the master panel can't be squished.
        Dimension minimumSize = new Dimension(360,300);
        rightFrameSplitter.setMinimumSize(minimumSize);
        // if the window resizes, give top and bottom halves equal share of the real estate
		rightFrameSplitter.setResizeWeight(0.25);

        Log.message("build splitters");
        splitLeftRight.add(renderPanel);
        splitLeftRight.add(rightFrameSplitter);
        // if the window resizes, give left half as much real estate as it can get.
        splitLeftRight.setResizeWeight(1);

        mainFrame.add(splitLeftRight);
        
        mainFrame.setJMenuBar(mainMenu);
 	}

	private void buildMainFrame() {
		Log.message("buildMainFrame()");
		// start the main application frame - the largest visible rectangle on the screen with the minimize/maximize/close buttons.
        mainFrame = new JFrame( APP_TITLE + " " + VERSION ); 
    	mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setLayout(new java.awt.BorderLayout());
        mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);
    	setWindowSizeAndPosition();
		setupDropTarget();
        mainFrame.addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
        	@Override
        	public void windowClosing(WindowEvent e) {
        		confirmClose();
        		super.windowClosing(e);
        	}
        	
    		// switch back to this window
        	@Override
            public void windowActivated(WindowEvent e) {
        		super.windowActivated(e);
        	}

    		// switch away to another window
        	@Override
            public void windowDeactivated(WindowEvent e) {
        		super.windowDeactivated(e);
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
	}

	private void setWindowSizeAndPosition() {
		Log.message("Set window size and position");

    	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int windowW = prefs.getInt(KEY_WINDOW_WIDTH, dim.width);
		int windowH = prefs.getInt(KEY_WINDOW_HEIGHT, dim.height);
		int windowX = prefs.getInt(KEY_WINDOW_X, (dim.width - windowW)/2);
		int windowY = prefs.getInt(KEY_WINDOW_Y, (dim.height - windowH)/2);
		mainFrame.setBounds(windowX, windowY,windowW, windowH);
		boolean isFullscreen = prefs.getBoolean("isFullscreen",false);
		if(isFullscreen) {
			mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
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
		
	public Scene getScene() {
		return scene;
	}
	
	public List<Entity> getSelectedEntities() {
		return new ArrayList<>(selectedEntities);
	}

	private void buildMainMenu() {
		Log.message("buildMainMenu()");
		
		mainMenu = new JMenuBar();
		mainMenu.removeAll();
		mainMenu.add(createFileMenu());
		mainMenu.add(createDemoMenu());
		mainMenu.add(createEditMenu());
		mainMenu.add(createHelpMenu());
        mainMenu.updateUI();
	}

	private JComponent createFileMenu() {
		JMenu menu = new JMenu(APP_TITLE);

		menu.add(new SceneClearAction(this));
		menu.add(new SceneLoadAction(this));
		menu.add(new SceneImportAction(this));
		menu.add(new SceneSaveAction(this));
		menu.add(new JSeparator());
		menu.add(new QuitAction(this));

		return menu;
	}

	private JComponent createDemoMenu() {
		JMenu menu = new JMenu("Demos");
		menu.add(new JMenuItem(new DemoAction(this,new DemoSpidee())));
		//menu.add(new JMenuItem(new DemoAction(this,new ODEPhysicsDemo())));
		return menu;
	}

	private JComponent createEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.add(new JMenuItem(UndoSystem.getCommandUndo()));
		menu.add(new JMenuItem(UndoSystem.getCommandRedo()));
		menu.add(new JSeparator());

		EntityCopyAction entityCopyAction = new EntityCopyAction(this);
		EntityPasteAction entityPasteAction = new EntityPasteAction(this);
		entityDeleteAction = new EntityDeleteAction(this);
		EntityCutAction entityCutAction = new EntityCutAction(entityDeleteAction, entityCopyAction);

		menu.add(entityCopyAction);
		menu.add(entityPasteAction);
		menu.add(entityCutAction);
		menu.add(entityDeleteAction);

		actions.add(entityCopyAction);
		actions.add(entityPasteAction);
		actions.add(entityCutAction);
		actions.add(entityDeleteAction);

		return menu;
	}

	private JComponent createHelpMenu() {
		JMenu menu = new JMenu("Help");
		JMenuItem buttonViewLog = new JMenuItem("Show Log");
		buttonViewLog.addActionListener((e) -> showLogDialog() );
		menu.add(buttonViewLog);
		menu.add(new JMenuItem(new AboutControlsAction()));
		menu.add(new JMenuItem(new ForumsAction()));
		menu.add(new JMenuItem(new CheckForUpdateAction()));
		menu.add(new JMenuItem(new AboutAction()));
		return menu;
	}

	private void showLogDialog() {
		logFrame.setVisible(true);
	}

    private void updateSelectEntities() {
		if( entityRenameAction != null ) entityRenameAction.setEnabled(false);

		renderPanel.updateSubjects();
		updateComponentPanel();
	}

	public void updateComponentPanel() {
		componentPanel.refreshContents(getSelectedEntities());
	}

	public void confirmClose() {
        int result = JOptionPane.showConfirmDialog(
				mainFrame,
				Translator.get("RobotOverlord.quitConfirm"),
				Translator.get("RobotOverlord.quitTitle"),
				JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
        	mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			preferencesSave();

        	// Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
	        new Thread(() -> {
				renderPanel.stopAnimationSystem();
				mainFrame.dispose();
			}).start();
        }
	}
	
	/**
	 * Deep search for a child with this name.
	 * @param name the name to match
	 * @return the entity.  null if nothing found.
	 */
	public Entity findEntityWithName(String name) {
		ArrayList<Entity> list = new ArrayList<>();
		list.add(scene);
		while( !list.isEmpty() ) {
			Entity obj = list.remove(0);
			String objectName = obj.getName();
			if(name.equals(objectName)) return obj;
			list.addAll(obj.getChildren());
		}
		return null;
	}

	public Viewport getViewport() {
		return renderPanel.getViewport();
	}

	public CameraComponent getCamera() {
		return findFirstComponentRecursive(CameraComponent.class);
	}

	public void setSelectedEntity(Entity entity) {
		List<Entity> list = new ArrayList<>();
		if(entity!=null) list.add(entity);
		setSelectedEntities(list);
	}

	public void setSelectedEntities(List<Entity> list) {
		selectedEntities.clear();
		selectedEntities.addAll(list);
		entityTree.setSelection(list);
		updateSelectEntities();
		updateActionEnableStatus();
	}

	public void setCopiedEntities(Entity container) {
		copiedEntities=container;
		updateActionEnableStatus();
	}

	public Entity getCopiedEntities() {
		return copiedEntities;
	}

	/**
	 * Tell all Actions to check if they are active.
	 */
	private void updateActionEnableStatus() {
		for(AbstractAction a : actions) {
			if(a instanceof EditorAction) {
				((EditorAction)a).updateEnableStatus();
			}
		}
	}

	private void setupDropTarget() {
		logger.debug("adding drag + drop support...");
		new DropTarget(mainFrame,new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					Transferable tr = dtde.getTransferable();
					DataFlavor[] flavors = tr.getTransferDataFlavors();
					for (DataFlavor flavor : flavors) {
						logger.debug("Possible flavor: {}", flavor.getMimeType());
						if (flavor.isFlavorJavaFileListType()) {
							dtde.acceptDrop(DnDConstants.ACTION_COPY);
							Object object = tr.getTransferData(flavor);
							if (object instanceof List<?>) {
								List<?> list = (List<?>) object;
								if (list.size() > 0) {
									object = list.get(0);
									if (object instanceof File) {
										File file = (File) object;
										if(loadMesh(file.getAbsolutePath())) {
											dtde.dropComplete(true);
											return;
										}
										if(importScene(file)) {
											dtde.dropComplete(true);
											return;
										}
									}
								}
							}
						}
					}
					logger.debug("Drop failed: {}", dtde);
					dtde.rejectDrop();
				} catch (Exception e) {
					logger.error("Drop error", e);
					dtde.rejectDrop();
				}
			}
		});
	}

	private boolean importScene(File file) {
		SceneImportAction action = new SceneImportAction(this);
		return action.loadFile(file);
	}

	private boolean loadMesh(String absolutePath) {
		if(!MeshFactory.canLoad(absolutePath)) return false;

		logger.debug("loadMesh({})",absolutePath);
        try {
			// create entity.
			Entity entity = new Entity();
			entity.setName(getFilenameWithoutExtensionFromPath(absolutePath));
			// add shape, which will add pose and material.
			ShapeComponent shape = new MeshFromFile(absolutePath);
			entity.addComponent(shape);
			// move entity to camera orbit point so it's visible.
			PoseComponent pose = entity.findFirstComponent(PoseComponent.class);
			pose.setPosition(getCamera().getOrbitPoint());

			// add entity to scene.
			UndoSystem.addEvent(this,new EntityAddEdit(getScene(),entity));
			//robotOverlord.setSelectedEntity(entity);
		}
		catch(Exception e) {
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
