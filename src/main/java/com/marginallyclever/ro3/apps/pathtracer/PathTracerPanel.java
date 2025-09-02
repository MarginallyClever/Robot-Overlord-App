package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * <p>{@link PathTracerPanel} controls a {@link PathTracer} and displays the results.</p>
 * <p>Special thanks to <a href='https://raytracing.github.io/books/RayTracingInOneWeekend.html'>Ray Tracing in One Weekend</a></p>
 */
public class PathTracerPanel extends JPanel
        implements SceneChangeListener, ProgressListener, PropertyChangeListener {
    private final PathTracer pathTracer;
    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private Camera activeCamera;
    private final JComboBox<String> comboBox = new JComboBox<>(new String[]{"Color","Depth","Normal"});
    private final PathTracerResultPanel centerLabel = new PathTracerResultPanel();
    private AbstractAction startButton;
    private final JLabel runTime = new JLabel("Ready");
    private final JButton saveButton = new JButton(new ImageIcon(
            Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/marginallyclever/ro3/apps/editor/icons8-save-16.png"))
                    .getScaledInstance(16,16, Image.SCALE_SMOOTH)
    ));
    private static final JFileChooser saveImageFileChooser = new JFileChooser();
    private final JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT,5,1));


    public PathTracerPanel() {
        this(new PathTracer());
    }

    public PathTracerPanel(PathTracer pathTracer) {
        super(new BorderLayout());
        this.pathTracer = pathTracer;
        pathTracer.addProgressListener(this);
        pathTracer.addPropertyChangeListener(this);
        pathTracer.setSize(1, 1);
        setupBars();
        setupCenter();
        add(toolBar, BorderLayout.NORTH);
        add(centerLabel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupCenter() {
        centerLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Mouse clicked at: " + e.getX() + ", " + e.getY());
                pathTracer.fireAndDisplayOneRay(e.getX(), e.getY(),pathTracer.getMaxDepth());
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        Registry.addSceneChangeListener(this);
        Registry.cameras.addItemAddedListener(this::addCamera);
        Registry.cameras.addItemRemovedListener(this::removeCamera);
        updateCameraList();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        Registry.removeSceneChangeListener(this);
        Registry.cameras.removeItemAddedListener(this::addCamera);
        Registry.cameras.removeItemRemovedListener(this::removeCamera);
    }

    private void updateCameraList() {
        cameraListModel.removeAllElements();
        Registry.cameras.getList().forEach(cameraListModel::addElement);
        if(cameraListModel.getSize()>0) {
            setActiveCamera(cameraListModel.getElementAt(0));
        }
    }

    private void addCamera(Object source,Camera camera) {
        if(cameraListModel.getIndexOf(camera) == -1) {
            cameraListModel.addElement(camera);
        }
    }

    private void removeCamera(Object source,Camera camera) {
        cameraListModel.removeElement(camera);
    }

    private void setupBars() {
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));
        toolBar.add(new AbstractAction() {
            {
                putValue(Action.NAME, "");
                putValue(Action.SHORT_DESCRIPTION, "Open settings panel.");
                putValue(Action.SMALL_ICON, new ImageIcon(
                        Toolkit.getDefaultToolkit().getImage(getClass().getResource("/com/marginallyclever/ro3/apps/shared/icons8-settings-16.png"))
                                .getScaledInstance(16,16, Image.SCALE_SMOOTH)
                ));
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                var parent = SwingUtilities.getWindowAncestor(PathTracerPanel.this);
                var panel = new PathTracerSettingsPanel(pathTracer);
                JOptionPane.showMessageDialog(parent, panel, "Path Tracer Settings", JOptionPane.PLAIN_MESSAGE);
                pathTracer.savePreferences();
            }
        });

        comboBox.setToolTipText("Select which render mode to display.");
        comboBox.addActionListener(e -> setCenterLabel(comboBox.getSelectedIndex()));

        addCameraSelector();

        startButton = new AbstractAction() {
            {
                // put unicode for play
                putValue(Action.NAME, "▶");
                putValue(Action.SHORT_DESCRIPTION, "Render the scene using path tracing.");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (pathTracer.isRunning()) {
                    pathTracer.stop();
                } else {
                    // make into unicode for pause
                    startButton.putValue(Action.NAME, "⏸");
                    pathTracer.setActiveCamera(getActiveCamera());
                    pathTracer.setSize(centerLabel.getWidth(), centerLabel.getHeight());
                    setCenterLabel(comboBox.getSelectedIndex());
                    runTime.setText("Preparing to render...");
                    runTime.repaint();
                    pathTracer.start();
                }
            }
        };

        toolBar.add(startButton);
        toolBar.add(comboBox);

        saveButton.setToolTipText("Save the current image to a PNG file.");
        saveButton.addActionListener(e -> saveImage());
        toolBar.add(saveButton);

        statusBar.add(runTime);
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
    }

    private void saveImage() {
        if(pathTracer.getImage() == null) {
            JOptionPane.showMessageDialog(this, "No image to save. Please render the scene first.", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }

        saveImageFileChooser.setDialogTitle("Save Rendered Image");
        String dateAndTime = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        dateAndTime += "-" + comboBox.getSelectedItem() + ".png";
        saveImageFileChooser.setSelectedFile(new java.io.File(dateAndTime));
        int userSelection = saveImageFileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = saveImageFileChooser.getSelectedFile();
            try {
                var image = getPathTracerImage(comboBox.getSelectedIndex());
                javax.imageio.ImageIO.write(image, "png", fileToSave);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setCenterLabel(int index) {
        centerLabel.setImage(getPathTracerImage(index));
    }

    private BufferedImage getPathTracerImage(int index) {
        return switch (index) {
            case 1 -> pathTracer.getDepthMap();
            case 2 -> pathTracer.getNormalMap();
            default -> pathTracer.getImage();
        };
    }

    private void addCameraSelector() {
        JComboBox<Camera> cameraSelector = new JComboBox<>();
        cameraSelector.setModel(cameraListModel);
        cameraListModel.addAll(Registry.cameras.getList());
        cameraSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Camera) {
                    setText(((Camera) value).getName());
                }
                return this;
            }
        });
        cameraSelector.setToolTipText("Select the active camera.");
        cameraSelector.addActionListener(e -> {
            setActiveCamera((Camera)cameraSelector.getSelectedItem());
        });
        cameraSelector.setSelectedIndex(0);
        toolBar.add(cameraSelector);
    }

    public Camera getActiveCamera() {
        if(Registry.cameras.getList().isEmpty()) throw new RuntimeException("No cameras available.");
        return activeCamera;
    }

    public void setActiveCamera(Camera camera) {
        activeCamera = camera;
        // set camera to registry active camera
        cameraListModel.setSelectedItem(activeCamera);
    }

    @Override
    public void beforeSceneChange(Node oldScene) {}

    @Override
    public void afterSceneChange(Node newScene) {
        // find the first active camera in the scene.
        Node scene = Registry.getScene();
        var camera = scene.findFirstChild(Camera.class);
        if(camera!=null) {
            setActiveCamera(camera);
        }
    }

    @Override
    public void onProgressUpdate(int latestProgress) {
        var elapsed = System.currentTimeMillis() - pathTracer.getStartTime();
        // display in hh:mm:ss:ms
        runTime.setText(String.format("%d samples %02d:%02d:%02d",
                latestProgress,
                elapsed / 3600000,
                (elapsed % 3600000) / 60000,
                (elapsed % 60000) / 1000));

        centerLabel.repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if("state".equals(evt.getPropertyName())) {
            if(evt.getNewValue() == SwingWorker.StateValue.DONE) {
                // set name to unicode for play
                startButton.putValue(Action.NAME, "▶");
            }
        }
    }
}
