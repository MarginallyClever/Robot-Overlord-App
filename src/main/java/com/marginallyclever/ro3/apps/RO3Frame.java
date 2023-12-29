package com.marginallyclever.ro3.apps;

import ModernDocking.DockingRegion;
import ModernDocking.app.AppState;
import ModernDocking.app.DockableMenuItem;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.exception.DockingLayoutException;
import ModernDocking.ext.ui.DockingUI;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.communications.application.TextInterfaceToSessionLayer;
import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.RO3;
import com.marginallyclever.ro3.UndoSystem;
import com.marginallyclever.ro3.apps.about.AboutPanel;
import com.marginallyclever.ro3.apps.actions.*;
import com.marginallyclever.ro3.apps.editorpanel.EditorPanel;
import com.marginallyclever.ro3.apps.logpanel.LogPanel;
import com.marginallyclever.ro3.apps.nodedetailview.NodeDetailView;
import com.marginallyclever.ro3.apps.nodetreeview.NodeTreeView;
import com.marginallyclever.ro3.apps.webcampanel.WebCamPanel;
import com.marginallyclever.ro3.apps.render.OpenGLPanel;
import com.marginallyclever.ro3.apps.render.Viewport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.dnd.DropTarget;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import java.util.Properties;
import java.util.prefs.Preferences;

public class RO3Frame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(RO3Frame.class);
    private final List<DockingPanel> windows = new ArrayList<>();
    private final JFileChooser fileChooser;
    private final OpenGLPanel renderPanel;
    private final LogPanel logPanel;
    private final EditorPanel editPanel;
    private final WebCamPanel webCamPanel;
    private final TextInterfaceToSessionLayer textInterface;

    public static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter("RO files", "RO");
    public static String VERSION;

    public RO3Frame() {
        super("Robot Overlord 3");
        loadVersion();
        setLookAndFeel();
        initDocking();

        logPanel = new LogPanel();
        editPanel = new EditorPanel();
        renderPanel = new Viewport();
        fileChooser = new JFileChooser();
        webCamPanel = new WebCamPanel();
        textInterface = new TextInterfaceToSessionLayer();

        createDefaultLayout();
        saveAndRestoreLayout();

        UndoSystem.start();

        createMenus();
        addQuitHandler();
        addAboutHandler();
        setupFileChooser();
        setupDropTarget();
    }

    private void loadVersion() {
        try(InputStream input = RO3.class.getClassLoader().getResourceAsStream("robotoverlord.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            VERSION = prop.getProperty("robotoverlord.version");
        } catch(IOException e) {
            logger.error("Failed to load version number.", e);
        }
    }

    private void setupDropTarget() {
        new DropTarget(this, new RO3FrameDropTarget());
    }

    private void setupFileChooser() {
        fileChooser.setFileFilter(FILE_FILTER);
        // TODO: fileChooser.setSelectedFile(most recently touched file?);
    }

    private void setLookAndFeel() {
        FlatLaf.registerCustomDefaultsSource("docking");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // option 2: UIManager.setLookAndFeel(new FlatDarkLaf());
            // option 3: UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            logger.error("Failed to set look and feel.");
        }
    }

    private void initDocking() {
        Docking.initialize(this);
        DockingUI.initialize();
        ModernDocking.settings.Settings.setAlwaysDisplayTabMode(true);
        ModernDocking.settings.Settings.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
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
                desktop.setAboutHandler((e) ->{
                    var panel = new AboutPanel();
                    JOptionPane.showMessageDialog(this, panel,
                            "About",
                            JOptionPane.PLAIN_MESSAGE);
                });
            }
        }
    }

    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        menuBar.add(buildFileMenu());
        //menuBar.add(buildEditMenu());
        menuBar.add(buildWindowsMenu());
        menuBar.add(buildHelpMenu());
    }

    private Component buildEditMenu() {
        JMenu menu = new JMenu("Edit");
        // TODO turn actions into edits for undo/redo, then add the matching actions back in.
        menu.add(new JMenuItem(UndoSystem.getCommandUndo()));
        menu.add(new JMenuItem(UndoSystem.getCommandRedo()));
        return menu;
    }

    private Component buildHelpMenu() {
        JMenu menuHelp = new JMenu("Help");
        var openManual = new BrowseURLAction("https://mcr.dozuki.com/c/Robot_Overlord_3");
        openManual.putValue(Action.NAME, "Read the friendly manual");
        openManual.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-open-book-16.png"))));
        openManual.putValue(Action.SHORT_DESCRIPTION, "Read the friendly manual.  It has pictures and everything!");
        menuHelp.add(new JMenuItem(openManual));

        var visitForum = new BrowseURLAction("https://discord.gg/VQ82jNvDBP");
        visitForum.putValue(Action.NAME, "Visit Forums");
        visitForum.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-discord-16.png"))));
        visitForum.putValue(Action.SHORT_DESCRIPTION, "Join us on Discord!");
        menuHelp.add(new JMenuItem(visitForum));

        menuHelp.add(new JMenuItem(new CheckForUpdateAction()));

        return menuHelp;
    }

    private JMenu buildWindowsMenu() {
        JMenu menuWindows = new JMenu("Windows");
        // add each panel to the windows menu with a checkbox if the current panel is visible.
        int index=0;
        for(DockingPanel w : windows) {
            DockableMenuItem item = new DockableMenuItem(w.getPersistentID(),w.getTabText());
            menuWindows.add(item);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, InputEvent.SHIFT_DOWN_MASK));
            index++;
        }
        return menuWindows;
    }

    private JMenu buildFileMenu() {
        JMenu menuFile = new JMenu("File");
        menuFile.add(new JMenuItem(new NewScene()));

        menuFile.add(new JSeparator());
        RecentFilesMenu loadRecentMenu = new RecentFilesMenu(Preferences.userNodeForPackage(LoadScene.class));
        menuFile.add(new JMenuItem(new LoadScene(loadRecentMenu,null,fileChooser)));
        menuFile.add(loadRecentMenu);
        menuFile.add(new JMenuItem(new ImportScene(fileChooser)));
        menuFile.add(new JMenuItem(new SaveScene(loadRecentMenu,fileChooser)));
        menuFile.add(new JMenuItem(new ExportScene()));

        menuFile.add(new JSeparator());
        menuFile.add(new JMenuItem(new AbstractAction("Quit") {
            {
                putValue(Action.NAME, "Quit");
                putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-stop-16.png"))));
                putValue(Action.SHORT_DESCRIPTION, "Quit the application.");
            }
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if(confirmClose()) {
                    setDefaultCloseOperation(EXIT_ON_CLOSE);
                    RO3Frame.this.dispatchEvent(new WindowEvent(RO3Frame.this, WindowEvent.WINDOW_CLOSING));
                }
            }
        }));

        return menuFile;
    }

    public boolean confirmClose() {
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to quit?", "Quit", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            // Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
            new Thread(() -> {
                renderPanel.stopAnimationSystem();
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
        setSize(800, 600);
        setLocationByPlatform(true);

        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);

        DockingPanel renderView = new DockingPanel("8e50154c-a149-4e95-9db5-4611d24cc0cc", "3D view");
        renderView.add(renderPanel, BorderLayout.CENTER);
        Docking.dock(renderView, this, DockingRegion.CENTER);
        windows.add(renderView);

        DockingPanel treeView = new DockingPanel("c6b04902-7e53-42bc-8096-fa5d43289362", "Scene");
        NodeTreeView nodeTreeView = new NodeTreeView();
        treeView.add(nodeTreeView, BorderLayout.CENTER);
        Docking.dock(treeView, this, DockingRegion.WEST);
        windows.add(treeView);

        DockingPanel detailView = new DockingPanel("67e45223-79f5-4ce2-b15a-2912228b356f", "Details");
        NodeDetailView nodeDetailView = new NodeDetailView();
        detailView.add(nodeDetailView, BorderLayout.CENTER);
        Docking.dock(detailView, treeView, DockingRegion.SOUTH);
        windows.add(detailView);

        DockingPanel logView = new DockingPanel("5e565f83-9734-4281-9828-92cd711939df", "Log");
        logView.add(logPanel, BorderLayout.CENTER);
        windows.add(logView);

        DockingPanel editorView = new DockingPanel("3f8f54e1-af78-4994-a1c2-21a68ec294c9", "Editor");
        editorView.add(editPanel, BorderLayout.CENTER);
        windows.add(editorView);

        DockingPanel aboutView = new DockingPanel("976af87b-90f3-42ce-a5d6-e4ab663fbb15", "About");
        aboutView.add(new AboutPanel(), BorderLayout.CENTER);
        Docking.dock(aboutView,treeView,DockingRegion.CENTER);
        windows.add(aboutView);

        DockingPanel webcamView = new DockingPanel("1331fbb0-ceda-4c67-b343-6539d4f939a1", "USB Camera");
        webcamView.add(webCamPanel, BorderLayout.CENTER);
        windows.add(webcamView);

        DockingPanel textInterfaceView = new DockingPanel("7796a733-8e33-417a-b363-b28174901e40", "Text Interface");
        textInterfaceView.add(textInterface, BorderLayout.CENTER);
        windows.add(textInterfaceView);
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
}
