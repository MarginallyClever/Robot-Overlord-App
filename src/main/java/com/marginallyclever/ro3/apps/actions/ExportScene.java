package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.robotoverlord.systems.render.mesh.load.MeshFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>Export the scene and all the assets used to a single file for sharing on another computer.
 * This is not the same as saving the scene.</p>
 */
public class ExportScene extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(ExportScene.class);
    public static final FileNameExtensionFilter ZIP_FILTER = new FileNameExtensionFilter("ZIP files", "zip");
    private final JFileChooser chooser = new JFileChooser();

    public ExportScene() {
        super();
        putValue(Action.NAME,"Export Scene");
        putValue(Action.SMALL_ICON,new ImageIcon(Objects.requireNonNull(getClass().getResource("icons8-export-16.png"))));
        putValue(SHORT_DESCRIPTION,"Export the scene and all the assets used to a ZIP file.");
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        chooser.setFileFilter(ZIP_FILTER);
        Component source = (Component) e.getSource();
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        // make sure to add the extension if the user doesn't
        chooser.addActionListener((e2) -> {
            if (JFileChooser.APPROVE_SELECTION.equals(e2.getActionCommand())) {
                String[] extensions = ZIP_FILTER.getExtensions();
                File f = chooser.getSelectedFile();
                String fname = f.getName().toLowerCase();
                boolean matches = Arrays.stream(extensions).anyMatch((ext) -> fname.toLowerCase().endsWith("." + ext));
                if (!matches) {
                    f = new File(f.getPath() + "." + extensions[0]);  // append the first extension from ZIP_FILTER
                    chooser.setSelectedFile(f);
                }
            }
        });

        if (chooser.showSaveDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
            // check before overwriting.
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile.exists()) {
                int response = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(source),
                        "Do you want to replace the existing file?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            String absolutePath = chooser.getSelectedFile().getAbsolutePath();
            commitExport(absolutePath);
        }
    }

    private void commitExport(String absolutePath) {
        logger.info("Exporting to {}", absolutePath);

        JSONObject json = Registry.getScene().toJSON();

        List<String> sources = Registry.textureFactory.getAllSourcesForExport();
        sources.addAll(MeshFactory.getAllSourcesForExport());

        createZipAndAddAssets(absolutePath, json.toString(), sources);

        logger.error("done.");
    }

    private void createZipAndAddAssets(String outputZipFile, String json, List<String> sources) {
        // for remembering unique asset names
        Map<String, String> pathMapping = new HashMap<>();

        String rootFolderName = nameWithoutExtension(new File(outputZipFile));
        String sceneName = rootFolderName+ ".ro";
        String newSceneName = rootFolderName+"/"+sceneName;
        pathMapping.put(sceneName,newSceneName);  // reserve this name

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outputZipFile))) {
            for( String originalPath : sources ) {
                String newName = rootFolderName + "/" + createUniqueName(originalPath, pathMapping);
                addFileToZip(originalPath, newName, zos);
                pathMapping.put(originalPath, newName);
            }

            // Modify JSON string
            String modifiedJson = replacePathsInJson(json, pathMapping);
            // Add modified JSON string to zip
            zos.putNextEntry(new ZipEntry(newSceneName));
            byte[] jsonBytes = modifiedJson.getBytes();
            zos.write(jsonBytes, 0, jsonBytes.length);
            zos.closeEntry();
        } catch (FileNotFoundException e) {
            logger.error("Could not open ZIP file.", e);
        } catch (IOException e) {
            logger.error("IO error.", e);
        }
    }

    private void addFileToZip(String filePath, String newName, ZipOutputStream zos) throws IOException {
        zos.putNextEntry(new ZipEntry(newName));
        BufferedInputStream input = FileHelper.open(filePath);
        byte[] bytes = input.readAllBytes();
        zos.write(bytes, 0, bytes.length);
        zos.closeEntry();
    }

    private String createUniqueName(String originalPath, Map<String, String> pathMapping) {
        File file = new File(originalPath);
        String name = file.getName();
        int counter = 1;
        while (pathMapping.containsValue(name)) {
            name = String.format("%s_%d.%s", nameWithoutExtension(file), counter++, extension(file));
        }
        return name;
    }

    private String nameWithoutExtension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    private String extension(File file) {
        String fileName = file.getName();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    private String replacePathsInJson(String json, Map<String, String> pathMapping) {
        for (Map.Entry<String, String> entry : pathMapping.entrySet()) {
            json = json.replace(entry.getKey(), entry.getValue());
        }
        return json;
    }
}
