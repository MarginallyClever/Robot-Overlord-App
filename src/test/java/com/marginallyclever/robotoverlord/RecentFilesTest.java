package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.preferences.RecentFiles;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

public class RecentFilesTest {
    private static final List<String> backupFilenames = new ArrayList<>();
    private final String [] list = { "a","b","c","d","e","f","g","h","i","j","k","l","m" };

    @BeforeAll
    public static void beforeAll() {
        RecentFiles recentFiles = new RecentFiles();
        backupFilenames.addAll(recentFiles.getFilenames());
    }

    @Test
    @Order(1)
    public void fillAndSaveTest() {
        RecentFiles recentFiles = new RecentFiles();
        for(String s : list) {
            recentFiles.add(s);
        }
        Assertions.assertEquals(RecentFiles.MAX_FILES,recentFiles.getFilenames().size());
        for(int i=0;i<RecentFiles.MAX_FILES;++i) {
            Assertions.assertEquals(list[list.length-1-i],recentFiles.getFilenames().get(i));
        }
    }

    @Test
    @Order(2)
    public void reloadTest() {
        RecentFiles recentFiles = new RecentFiles();
        Assertions.assertEquals(RecentFiles.MAX_FILES,recentFiles.getFilenames().size());
        for(int i=0;i<RecentFiles.MAX_FILES;++i) {
            Assertions.assertEquals(list[list.length-1-i],recentFiles.getFilenames().get(i));
        }
    }

    @AfterAll
    public static void AfterAll() {
        RecentFiles recentFiles = new RecentFiles();
        recentFiles.getFilenames().clear();
        recentFiles.getFilenames().addAll(backupFilenames);
        recentFiles.save();
    }
}
