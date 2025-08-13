package com.marginallyclever.ro3.apps.pathtracer;

import com.marginallyclever.ro3.PanelHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.SceneChangeListener;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.pose.poses.Camera;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * <p>{@link PathTracerPanel} controls a {@link PathTracer} and displays the results.</p>
 * <p>Special thanks to <a href='https://raytracing.github.io/books/RayTracingInOneWeekend.html'>Ray Tracing in One Weekend</a></p>
 */
public class PathTracerPanel extends JPanel implements SceneChangeListener, ProgressListener {
    private final PathTracer pathTracer;
    private final JToolBar toolBar = new JToolBar();
    private final DefaultComboBoxModel<Camera> cameraListModel = new DefaultComboBoxModel<>();
    private Camera activeCamera;
    private final JLabel centerLabel = new JLabel();
    private final JProgressBar progressBar = new JProgressBar();
    private final JLabel runTime = new JLabel();

    public PathTracerPanel() {
        this(new PathTracer());
    }

    public PathTracerPanel(PathTracer pathTracer) {
        super(new BorderLayout());
        this.pathTracer = pathTracer;
        pathTracer.addProgressListener(this);
        progressBar.setStringPainted(true);
        setupToolbar();
        add(toolBar, BorderLayout.NORTH);
        add(centerLabel,BorderLayout.CENTER);
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

    private void setupToolbar() {
        // add the same camera selection that appears in ViewportPanel

        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT,5,1));
        addCameraSelector();

        var spp = PanelHelper.addNumberFieldInt("Samples per pixel",pathTracer.getSamplesPerPixel());
        spp.addPropertyChangeListener("value",e->pathTracer.setSamplesPerPixel(((Number)e.getNewValue()).intValue()));
        toolBar.add(spp);

        var md = PanelHelper.addNumberFieldInt("Max Depth",pathTracer.getMaxDepth());
        md.addPropertyChangeListener("value",e->pathTracer.setMaxDepth(((Number)e.getNewValue()).intValue()));
        toolBar.add(md);

        toolBar.add(new AbstractAction() {
            {
                putValue(Action.NAME, "Start");
                putValue(Action.SHORT_DESCRIPTION, "Render the scene using path tracing.");
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if(pathTracer.isRunning()) {
                    pathTracer.stop();
                    putValue(Action.NAME, "Start");
                } else {
                    putValue(Action.NAME, "Stop");
                    pathTracer.setActiveCamera(getActiveCamera());
                    pathTracer.setSize(getWidth(), getHeight());
                    centerLabel.setIcon(new ImageIcon(pathTracer.getImage()));
                    //centerLabel.setIcon(new ImageIcon(pathTracer.getDepthMap()));
                    progressBar.setValue(0);
                    runTime.setText(String.format("%02d:%02d:%02d:%03d", 0, 0, 0, 0));
                    pathTracer.render();
                }
            }
        });
        toolBar.add(progressBar);
        toolBar.add(runTime);
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
        // Update progress bar here
        progressBar.setValue(latestProgress);

        var elapsed = System.currentTimeMillis() - pathTracer.getStartTime();
        // display in hh:mm:ss:ms
        runTime.setText(String.format("%02d:%02d:%02d:%03d",
                elapsed / 3600000,
                (elapsed % 3600000) / 60000,
                (elapsed % 60000) / 1000,
                elapsed % 1000));
    }
}
