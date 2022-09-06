package com.marginallyclever.robotoverlord.entities;

import com.marginallyclever.robotoverlord.mesh.ShapeEntity;

import javax.vecmath.Vector3d;
import java.io.Serial;

@Deprecated
public class TrayCabinet extends ShapeEntity {
    @Serial
    private static final long serialVersionUID = -6205115800152179820L;

    public TrayCabinet() {
        super();
        setName("Cabinet");
        setShapeFilename("/trayCabinet_resized.stl");
        getMaterial().setDiffuseColor(1, 1, 1, 1);
        getMaterial().setAmbientColor(1, 1, 1, 1);

        ShapeEntity tray = new ShapeEntity();
        addChild(tray);
        tray.setName("Tray");
        tray.setShapeFilename("/tray_resized.stl");
        tray.setPosition(new Vector3d(3.925 * -3, -8.35, 0.3 + 7));
        tray.getMaterial().setDiffuseColor(1, 1, 1, 1);
        tray.getMaterial().setAmbientColor(1, 1, 1, 1);

    }
}
