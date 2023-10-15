package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.robotoverlord.preferences.RecentFiles;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.actions.*;
import com.marginallyclever.robotoverlord.swing.translator.Translator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

public class MainMenu extends JMenuBar {

    private final RecentFiles recentFiles = new RecentFiles();
    private final RobotOverlord app;

    public MainMenu(RobotOverlord app) {
        super();
        this.app = app;
    }

    public void refresh() {
        this.removeAll();
        this.add(createFileMenu());
        this.add(createEditMenu());
        this.add(createDemoMenu());
        this.add(createHelpMenu());
        //mainMenu.updateUI();
        this.revalidate();
    }

    private JComponent createFileMenu() {
        JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.File"));

        Project project = app.getProject();

        menu.add(new ProjectClearAction(project));
        menu.add(new ProjectLoadAction(project));
        if(recentFiles.size()>0) menu.add(createRecentFilesMenu());
        menu.add(new ProjectImportAction(project));
        menu.add(new ProjectSaveAction(project));
        menu.add(new JSeparator());
        menu.add(new QuitAction(app));

        return menu;
    }

    private JMenu createRecentFilesMenu() {
        JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.RecentFiles"));
        final Component me = this;
        for(String filename : recentFiles.getFilenames()) {
            AbstractAction loader = new AbstractAction(filename) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ProjectLoadAction projectLoadAction = new ProjectLoadAction(app.getProject());
                    File file = new File(filename);
                    if(file.exists()) {
                        projectLoadAction.loadIntoScene(file,SwingUtilities.getWindowAncestor(me));
                        recentFiles.add(filename);
                    } else {
                        recentFiles.remove(filename);
                    }
                    refresh();
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
        menu.add(new JSeparator());
        menu.add(new JMenuItem(new EditPreferencesAction(app,this)));
        return menu;
    }

    private JComponent createDemoMenu() {
        JMenu menu = new JMenu(Translator.get("RobotOverlord.Menu.Demos"));
        //menu.add(new JMenuItem(new DemoAction(this,new ODEPhysicsDemo())));
        menu.add(new JMenuItem(new CreateVehicleAction(app.getProject().getEntityManager(),SwingUtilities.getWindowAncestor(this))));
        menu.addSeparator();
        menu.add(new JMenuItem(new ShowRobotLibraryPanel(this::refresh)));
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
                        level3Menu.add(new JMenuItem(new ProjectImportAction(app.getProject(), roFile)));
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
        buttonViewLog.addActionListener((e) -> app.showLogDialog() );
        menu.add(buttonViewLog);
        menu.add(new JMenuItem(new ForumsAction()));
        menu.add(new JMenuItem(new CheckForUpdateAction()));
        menu.add(new JMenuItem(new AboutAction(this)));
        return menu;
    }
}
