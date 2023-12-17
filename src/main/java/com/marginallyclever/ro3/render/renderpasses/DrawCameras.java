package com.marginallyclever.ro3.render.renderpasses;

import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.node.nodes.Camera;
import com.marginallyclever.ro3.render.RenderPass;
import com.marginallyclever.robotoverlord.systems.render.ShaderProgram;

public class DrawCameras implements RenderPass {
    int activeStatus = ALWAYS;

    /**
     * @return NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public int getActiveStatus() {
        return activeStatus;
    }

    /**
     * @param status NEVER, SOMETIMES, or ALWAYS
     */
    @Override
    public void setActiveStatus(int status) {
        activeStatus = status;
    }

    /**
     * @return the localized name of this overlay
     */
    @Override
    public String getName() {
        return "Cameras";
    }

    @Override
    public void draw(ShaderProgram shader) {
        for(Camera camera : Registry.cameras.getList() ) {
            // draw the frustum of the camera

        }
    }
}
