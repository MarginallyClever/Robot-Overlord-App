package com.marginallyclever.ro3.apps.actions;

import com.marginallyclever.convenience.helpers.FileHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.apps.RO3Frame;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.security.InvalidParameterException;
import java.util.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * <p>Export the scene and all the assets used to a single ZIP file for sharing on another computer.
 * This is not the same as saving the scene.</p>
 */
public class ExportScene extends AbstractAction {
    private final Logger logger = LoggerFactory.getLogger(ExportScene.class);
    public static final FileNameExtensionFilter ZIP_FILTER = new FileNameExtensionFilter("ZIP files", "zip");
    private final JFileChooser chooser;

    public ExportScene(JFileChooser chooser) {
        super();
        this.chooser = chooser;
        putValue(Action.NAME,"Export...");
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
        if( chooser == null ) throw new InvalidParameterException("file chooser cannot be null");
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

        chooser.setDialogType(JFileChooser.SAVE_DIALOG);
        if (chooser.showDialog(parentFrame,"Export") == JFileChooser.APPROVE_OPTION) {
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

    /**
     * Export the scene and all the assets used to a single file for sharing on another computer.
     * @param absolutePath the path to the file to create.
     */
    public void commitExport(String absolutePath) {
        logger.info("Exporting to {}", absolutePath);

        JSONObject json = Registry.getScene().toJSON();

        List<String> sources = Registry.textureFactory.getAllSourcesForExport();
        sources.addAll(Registry.meshFactory.getAllSourcesForExport());

        createZipAndAddAssets(absolutePath, json.toString(), sources);

        logger.info("done.");
    }

    private void createZipAndAddAssets(String outputZipFile, String json, List<String> sources) {
        // for remembering unique asset names
        Map<String, String> pathMapping = new HashMap<>();

        String rootFolderName = nameWithoutExtension(new File(outputZipFile));
        String sceneName = rootFolderName+ "." + RO3Frame.FILE_FILTER.getExtensions()[0];  // "ro3" or "r
        String newSceneName = rootFolderName+"/"+sceneName;
        pathMapping.put(sceneName,newSceneName);  // reserve this name

        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(outputZipFile))) {
            for( String originalPath : sources ) {
                String newName = createUniqueName(originalPath, pathMapping);
                logger.debug("Adding {} as {}", originalPath, newName);
                addFileToZip(originalPath, rootFolderName + "/" + newName, zipOutputStream);

                String safeOriginal = makeSafe(originalPath);
                String safeReplacement = makeSafe(newName);
                // store the json version of the string to match later.
                pathMapping.put(safeOriginal, safeReplacement);
            }

            // Modify JSON string
            String modifiedJson = replacePathsInJson(json, pathMapping);
            // Add modified JSON string to zip
            zipOutputStream.putNextEntry(new ZipEntry(newSceneName));
            byte[] jsonBytes = modifiedJson.getBytes();
            zipOutputStream.write(jsonBytes, 0, jsonBytes.length);
            zipOutputStream.closeEntry();
        } catch (FileNotFoundException e) {
            logger.error("Could not open ZIP file.", e);
        } catch (IOException e) {
            logger.error("Write error.", e);
        }
    }

    private String makeSafe(String originalPath) {
        JSONObject asset = new JSONObject();
        final String jsonAssetHead = "{\"path\":\"";
        final String jsonAssetTail = "\"}";
        asset.put("path",originalPath);
        // find the json version of the string
        String unsafeName = asset.toString();
        String safeName = unsafeName.substring(jsonAssetHead.length(), unsafeName.length() - jsonAssetTail.length());
        return safeName;
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
            logger.debug("Replacing {} with {}", entry.getKey(), entry.getValue());
            json = json.replace(entry.getKey(), entry.getValue());
        }
        return json;
    }
}
