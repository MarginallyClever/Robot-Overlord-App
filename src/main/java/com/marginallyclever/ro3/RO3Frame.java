package com.marginallyclever.ro3;

import ModernDocking.DockingRegion;
import ModernDocking.app.DockableMenuItem;
import ModernDocking.app.Docking;
import ModernDocking.app.RootDockingPanel;
import ModernDocking.ext.ui.DockingUI;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.marginallyclever.ro3.logpanel.LogPanel;
import com.marginallyclever.ro3.node.NodeDetailView;
import com.marginallyclever.ro3.node.nodetreeview.NodeTreeView;
import com.marginallyclever.ro3.render.OpenGLPanel;
import com.marginallyclever.ro3.render.RenderPassPanel;
import com.marginallyclever.ro3.render.Viewport;
import com.marginallyclever.robotoverlord.swing.actions.AboutAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static javax.swing.UIManager.setLookAndFeel;

public class RO3Frame extends JFrame {
    private static final Logger logger = LoggerFactory.getLogger(RO3Frame.class);

    private OpenGLPanel renderPanel;
    private final RenderPassPanel rpp = new RenderPassPanel();
    private final LogPanel logPanel = new LogPanel();
    private final List<DockingPanel> windows = new ArrayList<>();

    public RO3Frame() {
        super("RO3");

        setLookAndFeel();

        setSize(800, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationByPlatform(true);

        initDocking();
        createPanels();
        createMenus();
        addQuitHandler();
        addAboutHandler();
    }

    private void setLookAndFeel() {
        FlatLaf.registerCustomDefaultsSource("docking");
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            //UIManager.setLookAndFeel(new FlatDarkLaf());
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            logger.error("Failed to set look and feel.");
        }
    }

    private void initDocking() {
        Docking.initialize(this);
        DockingUI.initialize();
        ModernDocking.settings.Settings.setAlwaysDisplayTabMode(true);
        ModernDocking.settings.Settings.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        RootDockingPanel root = new RootDockingPanel(this);
        add(root, BorderLayout.CENTER);
    }

    private void addQuitHandler() {
        addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
            @Override
            public void windowClosing(WindowEvent e) {
                if(confirmClose()) {
                    setDefaultCloseOperation(EXIT_ON_CLOSE);
                };
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
                    AboutAction a = new AboutAction(RO3Frame.this);
                    a.actionPerformed(null);
                });
            }
        }
    }

    private void createMenus() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menuFile = new JMenu("File");
        menuBar.add(menuFile);

        JMenu menuWindows = new JMenu("Windows");
        menuBar.add(menuWindows);
        // add each panel to the windows menu with a checkbox if the current panel is visible.
        for(DockingPanel w : windows) {
            menuWindows.add(new DockableMenuItem(w.getPersistentID(),w.getTabText()));
        }


        JMenu menuHelp = new JMenu("Help");
        menuBar.add(menuHelp);
        // add about menu item
        JMenuItem about = new JMenuItem(new AbstractAction("About") {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                AboutAction a = new AboutAction(RO3Frame.this);
                a.actionPerformed(null);
            }
        });
        menuHelp.add(about);
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

    private void createPanels() {
        renderPanel = new Viewport("3D view");
        Docking.dock(renderPanel, this, DockingRegion.CENTER);
        windows.add(renderPanel);

        NodeTreeView nodeTreeView = new NodeTreeView("Scene");
        Docking.dock(nodeTreeView,this, DockingRegion.WEST);
        windows.add(nodeTreeView);

        NodeDetailView nodeDetailView = new NodeDetailView("Details");
        Docking.dock(nodeDetailView, nodeTreeView, DockingRegion.SOUTH);
        windows.add(nodeDetailView);
        nodeTreeView.addSelectionChangeListener(nodeDetailView);

        DockingPanel logView = new DockingPanel("Log");
        logView.add(logPanel, BorderLayout.CENTER);
        windows.add(logView);

        DockingPanel renderPassesView = new DockingPanel("Render");
        renderPassesView.add(rpp, BorderLayout.CENTER);
        windows.add(renderPassesView);
    }
}
