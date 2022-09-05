package com.marginallyclever.robotOverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.Component;
import com.marginallyclever.robotOverlord.shape.Mesh;
import com.marginallyclever.robotOverlord.shape.load.MeshFactory;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewElementButton;
import com.marginallyclever.robotOverlord.swingInterface.view.ViewPanel;
import com.marginallyclever.robotOverlord.uiExposedTypes.BooleanEntity;
import com.marginallyclever.robotOverlord.uiExposedTypes.IntEntity;

import javax.swing.filechooser.FileFilter;
import java.util.ArrayList;

public class ShapeComponent extends Component {
    // a mesh from the pool of meshes
    protected transient Mesh myMesh;

    private final IntEntity numTriangles = new IntEntity("Triangles",0);
    private final BooleanEntity hasNormals = new BooleanEntity("Has normals",false);
    private final BooleanEntity hasColors = new BooleanEntity("Has colors",false);
    private final BooleanEntity hasUVs = new BooleanEntity("Has UVs",false);

    public ShapeComponent() {
        super();
    }

    public void setModel(Mesh m) {
        myMesh = m;
        numTriangles.set(myMesh.getNumTriangles());
        hasNormals.set(myMesh.getHasNormals());
        hasColors.set(myMesh.getHasColors());
        hasUVs.set(myMesh.getHasUVs());
    }

    public Mesh getModel() {
        return myMesh;
    }

    public void render(GL2 gl2) {
        if( myMesh!=null ) myMesh.render(gl2);
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);

        if(myMesh!=null) {
            view.add(numTriangles);
            view.add(hasNormals);
            view.add(hasColors);
            view.add(hasUVs);
        }
    }
}
