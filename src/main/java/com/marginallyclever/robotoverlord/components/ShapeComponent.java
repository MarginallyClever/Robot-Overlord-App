package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.mesh.Mesh;
import com.marginallyclever.robotoverlord.parameters.BooleanEntity;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

@ComponentDependency(components={PoseComponent.class, MaterialComponent.class})
public abstract class ShapeComponent extends RenderComponent {
    // a mesh from the pool of meshes
    protected transient Mesh myMesh;

    private transient final IntEntity numTriangles = new IntEntity("Triangles",0);
    private transient final BooleanEntity hasNormals = new BooleanEntity("Has normals",false);
    private transient final BooleanEntity hasColors = new BooleanEntity("Has colors",false);
    private transient final BooleanEntity hasUVs = new BooleanEntity("Has UVs",false);

    protected ShapeComponent() {
        super();
    }

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity!=null) {
            entity.addComponent(new PoseComponent());
            entity.addComponent(new MaterialComponent());
        }
    }

    public void setModel(Mesh m) {
        myMesh = m;
        if(m==null) {
            numTriangles.set(0);
            hasNormals.set(false);
            hasColors.set(false);
            hasUVs.set(false);
        } else {
            numTriangles.set(myMesh.getNumTriangles());
            hasNormals.set(myMesh.getHasNormals());
            hasColors.set(myMesh.getHasColors());
            hasUVs.set(myMesh.getHasUVs());
        }
    }

    public Mesh getModel() {
        return myMesh;
    }

    public void render(GL2 gl2) {
        if( !getEnabled() || !getVisible() || myMesh==null ) return;
        myMesh.render(gl2);
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.add(numTriangles);
        view.add(hasNormals);
        view.add(hasColors);
        view.add(hasUVs);
    }
}
