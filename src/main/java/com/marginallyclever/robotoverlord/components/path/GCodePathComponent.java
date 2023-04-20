package com.marginallyclever.robotoverlord.components.path;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.OpenGLHelper;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RenderComponent;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.parameters.StringEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;

/**
 * A {@link RenderComponent} that uses a {@link PathWalker} to render a {@link GCodePath}.
 * @author Dan Royer
 * @since 2.5.0
 */
public class GCodePathComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(GCodePathComponent.class);

    private final StringEntity filename = new StringEntity("File","");

    private GCodePath gCodePath;

    public GCodePathComponent() {
        super();

        filename.addPropertyChangeListener((e)->{
            String fn = filename.get();
            gCodePath = PathFactory.load(fn);
        });
    }

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity!=null) {
            entity.addComponent(new PoseComponent());
        }
    }

    @Override
    public void render(GL2 gl2) {
        if(gCodePath==null) return;

        boolean tex = OpenGLHelper.disableTextureStart(gl2);
        boolean light = OpenGLHelper.disableLightingStart(gl2);

        PathWalker pathWalker = new PathWalker(gCodePath,5);
        drawEntirePath(gl2,pathWalker);

        OpenGLHelper.disableTextureEnd(gl2,tex);
        OpenGLHelper.disableLightingEnd(gl2,light);
    }

    private void drawEntirePath(GL2 gl2,PathWalker pathWalker) {
        gl2.glBegin(GL2.GL_LINE_STRIP);

        double prevX = 0, prevY = 0, prevZ = 0;
        gl2.glColor4d(0, 0, 1,0.25);
        gl2.glVertex3d(prevX, prevY, prevZ);

        while (pathWalker.hasNext()) {
            pathWalker.next();
            double currentX = pathWalker.getCurrentX();
            double currentY = pathWalker.getCurrentY();
            double currentZ = pathWalker.getCurrentZ();
            GCodePathElement currentElement = pathWalker.getCurrentElement();
            String command = currentElement.getCommand();

            if (command.startsWith("G0") || command.startsWith("G1")) {
                if(currentElement.getExtrusion()==null) {
                    // rapid
                    gl2.glColor4d(0, 0, 1,0.25);
                } else {
                    // extrusion / milling movement
                    gl2.glColor3d(1, 0, 0);
                }
                gl2.glVertex3d(currentX, currentY, currentZ);
            } else if (command.startsWith("G2") || command.startsWith("G3")) {
                // arc
                gl2.glColor3d(0, 1, 0);
                gl2.glVertex3d(currentX, currentY, currentZ);
            }
        }

        gl2.glEnd();
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        ArrayList<FileFilter> filters = PathFactory.getAllExtensions();
        view.addFilename(filename,filters);
        view.addButton("Reload").addActionEventListener(e -> {
            PathFactory.reload(gCodePath);
        });

        IntEntity numCommands = new IntEntity("Commands",gCodePath==null ? 0 : gCodePath.getElements().size());
        view.add(numCommands);
    }
}
