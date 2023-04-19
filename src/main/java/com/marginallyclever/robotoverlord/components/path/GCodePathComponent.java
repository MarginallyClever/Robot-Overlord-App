package com.marginallyclever.robotoverlord.components.path;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import com.marginallyclever.robotoverlord.components.shapes.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.parameters.StringEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;

/**
 * A {@link RenderComponent} that renders a {@link GCodePath}.
 * @author Dan Royer
 * @since 2.5.0
 */
public class GCodePathComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(GCodePathComponent.class);

    private final StringEntity filename = new StringEntity("File","");

    private GCodePath myPath;

    @Override
    public void render(GL2 gl2) {

    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        ArrayList<FileFilter> filters = TurtlePathFactory.getAllExtensions();
        view.addFilename(filename,filters);
        view.addButton("Reload").addActionEventListener(e -> {
            TurtlePathFactory.reload(myPath);
        });
    }
}
