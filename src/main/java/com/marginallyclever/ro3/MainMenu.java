package com.marginallyclever.ro3;

import ModernDocking.app.DockableMenuItem;
import com.marginallyclever.ro3.apps.actions.*;
import com.marginallyclever.ro3.apps.shared.PersistentJFileChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Locale;
import java.util.Objects;
import java.util.prefs.Preferences;

public class MainMenu extends JMenuBar {
    private final RO3Frame frame;
    private final JFileChooser fileChooser;
    private boolean isMacOS = false;
    private int SHORTCUT_CTRL = InputEvent.CTRL_DOWN_MASK;
    private int SHORTCUT_ALT = InputEvent.ALT_DOWN_MASK;

    public MainMenu(RO3Frame frame) {
        super();
        this.frame = frame;

        fileChooser = new PersistentJFileChooser();
        fileChooser.setFileFilter(RO3Frame.FILE_FILTER);

        setSystemLookAndFeelForMacos();
        add(buildFileMenu());
        add(buildEditMenu());
        add(buildWindowsMenu());
        add(buildHelpMenu());
        updateUI();
    }

    private void setSystemLookAndFeelForMacos() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if ((os.contains("mac")) || (os.contains("darwin"))) {
            isMacOS=true;
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            SHORTCUT_CTRL = InputEvent.META_DOWN_MASK;
            SHORTCUT_ALT = InputEvent.META_DOWN_MASK;
        }
    }

    private JMenu buildFileMenu() {
        // "load", "load recent", and "new" enable/disable save.
        var loadRecentMenu = new RecentFilesMenu(Preferences.userNodeForPackage(LoadScene.class));
        var save = new SaveScene(loadRecentMenu);
        save.setEnabled(false);
        var load = new LoadScene(loadRecentMenu,null,fileChooser);
        load.setSaveScene(save);
        loadRecentMenu.setSaveScene(save);
        var saveAs = new SaveAsScene(loadRecentMenu,fileChooser);
        saveAs.setSaveScene(save);

        JMenu menuFile = new JMenu("File");
        menuFile.add(new JMenuItem(new NewScene(save)));
        menuFile.add(new JSeparator());
        menuFile.add(new JMenuItem(load));
        menuFile.add(loadRecentMenu);
        menuFile.add(new JMenuItem(new ImportScene(fileChooser)));
        menuFile.add(new JMenuItem(save));
        menuFile.add(new JMenuItem(saveAs));
        menuFile.add(new JMenuItem(new ExportScene(fileChooser)));

        //addSettingsMenu(menuFile);

        if(!isMacOS) {
            menuFile.add(new JSeparator());
            menuFile.add(new JMenuItem(new AbstractAction("Quit") {
                {
                    putValue(Action.NAME, "Quit");
                    putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, SHORTCUT_CTRL));
                    putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("apps/icons8-stop-16.png"))));
                    putValue(Action.SHORT_DESCRIPTION, "Quit the application.");
                }

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (frame.confirmClose()) {
                        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                    }
                }
            }));
        }

        return menuFile;
    }

    private JMenu buildWindowsMenu() {
        JMenu menuWindows = new JMenu("Windows");
        // add each panel to the windows menu with a checkbox if the current panel is visible.
        int index=0;
        for(DockingPanel w : frame.getDockingPanels()) {
            DockableMenuItem item = new DockableMenuItem(w.getPersistentID(),w.getTabText());
            menuWindows.add(item);
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1 + index, InputEvent.SHIFT_DOWN_MASK));
            index++;
        }

        menuWindows.add(new JSeparator());
        menuWindows.add(new JMenuItem(new AbstractAction() {
            {
                putValue(Action.NAME, "Reset default layout");
                // no accelerator key.
                putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("apps/icons8-reset-16.png"))));
                putValue(Action.SHORT_DESCRIPTION, "Reset the layout to the default.");
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                frame.resetDefaultLayout();
            }
        }));
        return menuWindows;
    }

    private Component buildEditMenu() {
        JMenu menu = new JMenu("Edit");
        menu.add(new JMenuItem(UndoSystem.getCommandUndo()));
        menu.add(new JMenuItem(UndoSystem.getCommandRedo()));
        //menu.add(new JSeparator());
        return menu;
    }

    private Component buildHelpMenu() {
        JMenu menuHelp = new JMenu("Help");

        var openManual = new BrowseURLAction("https://mcr.dozuki.com/c/Robot_Overlord_3");
        openManual.putValue(Action.NAME, "Read the friendly manual");
        openManual.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("apps/icons8-open-book-16.png"))));
        openManual.putValue(Action.SHORT_DESCRIPTION, "Read the friendly manual.  It has pictures and everything!");
        menuHelp.add(new JMenuItem(openManual));

        var visitForum = new BrowseURLAction("https://discord.gg/VQ82jNvDBP");
        visitForum.putValue(Action.NAME, "Visit Forums");
        visitForum.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("apps/icons8-discord-16.png"))));
        visitForum.putValue(Action.SHORT_DESCRIPTION, "Join us on Discord!");
        menuHelp.add(new JMenuItem(visitForum));

        var visitIssues = new BrowseURLAction("https://github.com/MarginallyClever/Robot-Overlord-App/issues");
        visitIssues.putValue(Action.NAME, "Report an Issue");
        visitIssues.putValue(Action.SMALL_ICON, new ImageIcon(Objects.requireNonNull(getClass().getResource("apps/icons8-bug-16.png"))));
        visitIssues.putValue(Action.SHORT_DESCRIPTION, "Report an issue on GitHub");
        menuHelp.add(new JMenuItem(visitIssues));

        menuHelp.add(new JMenuItem(new CheckForUpdateAction()));

        return menuHelp;
    }
}
