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
import com.marginallyclever.ro3.apps.about.AboutPanel;
import com.marginallyclever.ro3.apps.donatello.Donatello;
import com.marginallyclever.ro3.apps.editor.EditorPanel;
import com.marginallyclever.ro3.apps.log.LogPanel;
import com.marginallyclever.ro3.apps.nodedetailview.NodeDetailView;
import com.marginallyclever.ro3.apps.nodetreeview.NodeTreeView;
import com.marginallyclever.ro3.apps.ode4j.ODE4JPanel;
import com.marginallyclever.ro3.apps.viewport.OpenGLPanel;
import com.marginallyclever.ro3.apps.viewport.ViewportSettingsPanel;
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
    private final List<DockingPanel> windows = new ArrayList<>();
    private final OpenGLPanel viewportPanel;
    private final LogPanel logPanel;
    private final EditorPanel editPanel;
    private final WebCamPanel webCamPanel;
    private final ViewportSettingsPanel viewportSettingsPanel;
    private final TextInterfaceToSessionLayer textInterface;
    private final ODE4JPanel ode4jPanel;
    private final Donatello donatello;
    public static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter("RO files", "RO");
    public static String VERSION;

    public RO3Frame() {
        super();
        loadVersion();

        setTitleWithVersion();
        setLocationByPlatform(true);
        initDocking();

        logPanel = new LogPanel();
        editPanel = new EditorPanel();
        viewportPanel = new OpenGLPanel();
        viewportSettingsPanel = new ViewportSettingsPanel(viewportPanel);
        webCamPanel = new WebCamPanel();
        textInterface = new TextInterfaceToSessionLayer();
        ode4jPanel = new ODE4JPanel();
        donatello = new Donatello();

        createDefaultLayout();
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
                viewportPanel.stopAnimationSystem();
                this.dispose();
            }).start();
            return true;
        }
        return false;
    }

    /**
     * Persistent IDs were generated using <code>UUID.randomUUID().toString()</code>
     * or <a href="https://www.uuidgenerator.net/">one of many websites</a>.
     */
    private void createDefaultLayout() {
        DockingPanel renderView = new DockingPanel("8e50154c-a149-4e95-9db5-4611d24cc0cc", "3D view");
        renderView.add(viewportPanel);
        windows.add(renderView);

        DockingPanel treeView = new DockingPanel("c6b04902-7e53-42bc-8096-fa5d43289362", "Scene");
        NodeTreeView nodeTreeView = new NodeTreeView();
        treeView.add(nodeTreeView);
        windows.add(treeView);

        DockingPanel detailView = new DockingPanel("67e45223-79f5-4ce2-b15a-2912228b356f", "Details");
        NodeDetailView nodeDetailView = new NodeDetailView();
        detailView.add(nodeDetailView);
        windows.add(detailView);

        DockingPanel logView = new DockingPanel("5e565f83-9734-4281-9828-92cd711939df", "Log");
        logView.add(logPanel);
        windows.add(logView);

        DockingPanel editorView = new DockingPanel("3f8f54e1-af78-4994-a1c2-21a68ec294c9", "Editor");
        editorView.add(editPanel);
        windows.add(editorView);

        DockingPanel aboutView = new DockingPanel("976af87b-90f3-42ce-a5d6-e4ab663fbb15", "About");
        aboutView.add(new AboutPanel());
        windows.add(aboutView);

        DockingPanel webcamView = new DockingPanel("1331fbb0-ceda-4c67-b343-6539d4f939a1", "USB Camera");
        webcamView.add(webCamPanel);
        windows.add(webcamView);

        DockingPanel ode4jView = new DockingPanel("801706cf-c346-4229-a39e-b3665e5a0d94", "ODE4J");
        ode4jView.add(ode4jPanel);
        windows.add(ode4jView);

        DockingPanel textInterfaceView = new DockingPanel("7796a733-8e33-417a-b363-b28174901e40", "Serial Interface");
        textInterfaceView.add(textInterface);
        windows.add(textInterfaceView);

        DockingPanel viewportSettingsView = new DockingPanel("c0651f5b-d5f0-49ab-88f9-66ae4a8c095e", "Viewport Settings");
        viewportSettingsView.add(viewportSettingsPanel);
        windows.add(viewportSettingsView);

        // TODO all persistentIDs should match the name of the class.  Then the class can recreate the view from AppState.
        DockingPanel donatelloView = new DockingPanel("donatello", "Donatello");
        donatelloView.add(donatello);
        windows.add(donatelloView);
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
        var path = FileHelper.getUserHome()+File.separator+"RobotOverlord"+File.separator+"ro3.layout";
        logger.debug(path);
        AppState.setPersistFile(new File(path));
        AppState.setAutoPersist(true);

        try {
            AppState.restore();
        } catch (DockingLayoutException e) {
            // something happened trying to load the layout file, record it here
            logger.error("Failed to restore docking layout.", e);
        }
    }

    public List<DockingPanel> getDockingPanels() {
        return windows;
    }
}
