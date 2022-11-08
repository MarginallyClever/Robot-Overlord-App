package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.mesh.Mesh;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.IntEntity;

@ComponentDependency(components={PoseComponent.class, MaterialComponent.class})
public abstract class ShapeComponent extends Component {
    // a mesh from the pool of meshes
    protected transient Mesh myMesh;

    private transient final IntEntity numTriangles = new IntEntity("Triangles",0);
    private transient final BooleanEntity hasNormals = new BooleanEntity("Has normals",false);
    private transient final BooleanEntity hasColors = new BooleanEntity("Has colors",false);
    private transient final BooleanEntity hasUVs = new BooleanEntity("Has UVs",false);

    protected ShapeComponent() {
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
