package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONException;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A sphere with a radius of 0.5 centered around the local origin.
 * TODO add texture coordinates
 */
public class Sphere extends ShapeComponent implements PropertyChangeListener{
    public final DoubleParameter radius = new DoubleParameter("Radius", 0.5f);
    public final IntParameter detail = new IntParameter("Detail", 32);

    public Sphere() {
        super();

        myMesh = new Mesh();
        updateModel();
        setModel(myMesh);

        radius.addPropertyChangeListener(this);
        detail.addPropertyChangeListener(this);
    }

    // Procedurally generate a list of triangles that form a sphere subdivided by some amount.
    private void updateModel() {
        myMesh.clear();
        myMesh.setRenderStyle(GL3.GL_TRIANGLES);

        float r = radius.get().floatValue();

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
    }

    public double getDetail() { return detail.get(); }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("detail",detail.toJSON(context));
        jo.put("radius",radius.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        detail.parseJSON(jo.getJSONObject("detail"),context);
        if(jo.has("radius")) radius.parseJSON(jo.getJSONObject("radius"),context);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        detail.set(Math.max(1,detail.get()));
        updateModel();
    }
}
