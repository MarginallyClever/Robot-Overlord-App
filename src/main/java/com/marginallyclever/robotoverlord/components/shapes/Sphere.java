package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.mesh.Mesh;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A sphere with a radius of 0.5 centered around the local origin.
 */
public class Sphere extends ShapeComponent {
    private final IntEntity detail = new IntEntity("Detail",32);

    public Sphere() {
        super();
        detail.addPropertyChangeListener((evt)-> {
            detail.set(Math.max(1,detail.get()));
            updateModel();
        } );
        myMesh = new Mesh();
        updateModel();
        setModel(myMesh);
    }

    // Procedurally generate a list of triangles that form a sphere subdivided by some amount.
    private void updateModel() {
        myMesh.clear();
        myMesh.renderStyle= GL2.GL_TRIANGLES;

        float r = 0.5f;

        int height = detail.get();
        int width = height*2;

        double theta, phi;
        int i, j;

        for( j=1; j<height-1; j++ ) {
            for(i=0; i<width; i++ )  {
                double tx = (double)(i)/(double)(width-1 );
                double ty = (double)(j)/(double)(height-1);
                phi   = tx * Math.PI*2;
                theta = ty * Math.PI;

                float x = (float)( Math.sin(theta) * Math.cos(phi));
                float y = (float)( Math.cos(theta));
                float z = (float)(-Math.sin(theta) * Math.sin(phi));
                myMesh.addVertex(r*x,r*y,r*z);
                myMesh.addNormal(x,y,z);
                myMesh.addTexCoord((float)tx, (float)ty);

            }
        }
        myMesh.addVertex(0,r*1,0);
        myMesh.addNormal(0,1,0);
        myMesh.addTexCoord(0.5f,0);

        myMesh.addVertex(0,r*-1,0);
        myMesh.addNormal(0,-1,0);
        myMesh.addTexCoord(0.5f,1);

        for( j=0; j<height-3; j++ ) {
            for( i=0; i<width-1; i++ )  {
                myMesh.addIndex( (j  )*width + i  );
                myMesh.addIndex( (j+1)*width + i+1);
                myMesh.addIndex( (j  )*width + i+1);
                myMesh.addIndex( (j  )*width + i  );
                myMesh.addIndex( (j+1)*width + i  );
                myMesh.addIndex( (j+1)*width + i+1);
            }
        }
        for( i=0; i<width-1; i++ )  {
            myMesh.addIndex( (height-2)*width );
            myMesh.addIndex( i );
            myMesh.addIndex( i+1 );
            myMesh.addIndex( (height-2)*width+1 );
            myMesh.addIndex( (height-3)*width + i+1 );
            myMesh.addIndex( (height-3)*width + i );
        }
    }

    public void setDetail(int v) {
        detail.set(Math.max(1,v));
        updateModel();
    }

    public double getDetail() { return detail.get(); }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.add(detail);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject jo = super.toJSON();
        jo.put("detail",detail.toJSON());
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo) throws JSONException {
        super.parseJSON(jo);
        detail.parseJSON(jo.getJSONObject("detail"));
    }
}
