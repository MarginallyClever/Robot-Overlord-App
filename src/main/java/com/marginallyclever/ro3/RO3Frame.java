package com.marginallyclever.ro3;

import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.ext.ui.DockingUI;
import com.marginallyclever.communications.application.TextInterfaceToSessionLayer;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.apps.AppFactory;
import com.marginallyclever.ro3.apps.about.AboutPanel;
import com.marginallyclever.ro3.apps.brainview.BrainView;
import com.marginallyclever.ro3.apps.donatello.Donatello;
import com.marginallyclever.ro3.apps.editor.EditorPanel;
import com.marginallyclever.ro3.apps.log.LogPanel;
import com.marginallyclever.ro3.apps.nodedetailview.NodeDetailView;
import com.marginallyclever.ro3.apps.nodetreeview.NodeTreeView;
import com.marginallyclever.ro3.apps.ode4j.ODE4JPanel;
import com.marginallyclever.ro3.apps.pathtracer.PathTracerPanel;
import com.marginallyclever.ro3.apps.viewport.OpenGL3Panel;
import com.marginallyclever.ro3.apps.viewport.ViewportSettingsPanel;
import com.marginallyclever.ro3.apps.viewport.viewporttool.ViewportToolPanel;
import com.marginallyclever.ro3.apps.webcam.WebCamPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * <p>{@link RO3Frame} is the main frame for the Robot Overlord 3 application.  It contains the menu bar and docking
 * panels.  It also maintains one instance of each {@link App}.</p>
 */
public class RO3Frame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(RO3Frame.class);

    public static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter("RO files", "RO");
    public static String VERSION;

    private final List<DockingPanel> windows = new ArrayList<>();

    private final OpenGL3Panel opengl3Panel;
    private final ViewportSettingsPanel viewportSettingsPanel;
    private final ViewportToolPanel viewportToolPanel;

    public RO3Frame() {
        super();
        loadVersion();

        setTitleWithVersion();
        setLocationByPlatform(true);
        initDocking();

        opengl3Panel = new OpenGL3Panel();
        viewportSettingsPanel = new ViewportSettingsPanel(opengl3Panel);
        viewportToolPanel = new ViewportToolPanel(opengl3Panel);

        AppFactory.registerApps("com.marginallyclever.ro3.apps");
        AppFactory.listApps(System.out);

        registerDefaultWindows();
        resetDefaultLayout();
        saveAndRestoreLayout();

        UndoSystem.start();

        setJMenuBar(new MainMenu(this));

        addQuitHandler();
        addAboutHandler();
        setupDropTarget();
    }

    private void setTitleWithVersion() {
        // Retrieve the version from the manifest
        String version = this.getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = "Development Version"; // Fallback if version is not set
        }
        setTitle("Robot Overlord "+version);
    }

    private void loadVersion() {
        try(InputStream input = RO3.class.getClassLoader().getResourceAsStream("robotoverlord.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            VERSION = prop.getProperty("robotoverlord.version");
            logger.info("Robot Overlord version {}",VERSION);
        } catch(IOException e) {
            logger.error("Failed to load version number.", e);
        }
    }

    private void setupDropTarget() {
        new DropTarget(this, new RO3FrameDropTarget());
    }

    private void initDocking() {
        Docking.initialize(this);
        DockingUI.initialize();
        ModernDocking.settings.Settings.setAlwaysDisplayTabMode(true);
        ModernDocking.settings.Settings.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // create root panel
        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);
    }

    private void addQuitHandler() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
            @Override
            public void windowClosing(WindowEvent e) {
            if(confirmClose()) {
                setDefaultCloseOperation(EXIT_ON_CLOSE);
            }
            super.windowClosing(e);
            }
        });

        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
                desktop.setQuitHandler((evt, res) -> {
                    if (confirmClose()) {
                        setDefaultCloseOperation(EXIT_ON_CLOSE);
                        res.performQuit();
                    } else {
                        res.cancelQuit();
                    }
                });
            }
        }
    }

    private void addAboutHandler() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler((e) -> {
                    var panel = new AboutPanel();
                    JOptionPane.showMessageDialog(this, panel,
                            "About",
                            JOptionPane.PLAIN_MESSAGE);
                });
            }
        }
    }

    public boolean confirmClose() {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            // Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
            new Thread(() -> {
                opengl3Panel.stopAnimationSystem();
                this.dispose();
            }).start();
            return true;
        }
        return false;
    }

    /**
     * PersistentIDs must match the name of the class the {@link DockingPanel} will create by using an
     * {@link com.marginallyclever.ro3.apps.AppFactory}.  Then a AppFactory could recreate the views from AppState.
     * Also remember <a href="https://github.com/andrewauclair/ModernDocking/discussions/240#discussioncomment-10897811">this Modern Docking discussion</a>
     */
    private void registerDefaultWindows() {
        addDockingPanel("OpenGL3Panel", "OpenGL", opengl3Panel);
        addDockingPanel("NodeTreeView", "Scene",new NodeTreeView());
        addDockingPanel("NodeDetailView", "Details",new NodeDetailView());
        addDockingPanel("LogPanel", "Log",new LogPanel());
        addDockingPanel("EditorPanel", "Editor",new EditorPanel());
        addDockingPanel("AboutPanel", "About",new AboutPanel());
        addDockingPanel("WebCamPanel", "Camera",new WebCamPanel());
        addDockingPanel("ODE4JPanel", "ODE4J",new ODE4JPanel());
        addDockingPanel("TextInterfaceToSessionLayer", "Serial",new TextInterfaceToSessionLayer());
        addDockingPanel("ViewportSettingsPanel", "Viewport",viewportSettingsPanel);
        addDockingPanel("ViewportToolPanel", "Tool",viewportToolPanel);
        addDockingPanel("Donatello", "Donatello",new Donatello());
        addDockingPanel("BrainView", "BrainView", new BrainView());
        addDockingPanel("PathTracerPanel", "PathTracer", new PathTracerPanel());
    }

    private void addDockingPanel(String persistentID,String tabText,Component component) {
        DockingPanel panel = new DockingPanel(persistentID,tabText);
        panel.add(component);
        windows.add(panel);
    }

    /**
     * Reset the default layout.  These depend on the order of creation in createDefaultLayout().
     */
    public void resetDefaultLayout() {
        logger.info("Resetting layout to default.");
        setSize(1000, 750);

        for(DockingPanel w : windows) {
            Docking.undock(w);
        }
        var renderView = windows.get(0);
        var treeView = windows.get(1);
        var detailView = windows.get(2);
        var aboutView = windows.get(5);
        Docking.dock(renderView, this, DockingRegion.CENTER);
        Docking.dock(treeView, this, DockingRegion.WEST);
        Docking.dock(detailView, treeView, DockingRegion.SOUTH);
        Docking.dock(aboutView, treeView, DockingRegion.CENTER);
        logger.debug("done.");
    }

    private void saveAndRestoreLayout() {
        // now that the main frame is set up with the defaults, we can restore the layout
        var layoutPath = FileHelper.getUserHome()+File.separator+"RobotOverlord"+File.separator+"ro3.layout";
        logger.debug("layout file={}",layoutPath);
        AppState.setPersistFile(new File(layoutPath));
        AppState.setAutoPersist(true);

        try {
            AppState.restore();
        } catch (DockingLayoutException e) {
            logger.error("Failed to restore docking layout.", e);
        }
    }

    public List<DockingPanel> getDockingPanels() {
        return windows;
    }
}
