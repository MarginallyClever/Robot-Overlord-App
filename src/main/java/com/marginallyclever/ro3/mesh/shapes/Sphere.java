package com.marginallyclever.ro3.mesh.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

/**
 * <p>{@link Sphere} is a {@link Mesh} with a radius of 1.  The origin is at the center of the sphere.</p>
 */
public class Sphere extends Mesh {
    public float radius = 1.0f;
    public int detail = 32;  // level of detail

    public Sphere() {
        super();

        updateModel();
    }

    // Procedurally generate a list of triangles that form a sphere subdivided by some amount.
    private void updateModel() {
        this.clear();
        this.setRenderStyle(GL3.GL_TRIANGLES);

        int height = detail;
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
                this.addVertex(radius*x,radius*y,radius*z);
                this.addNormal(x,y,z);
                this.addTexCoord((float)tx, (float)ty);

            }
        }
        this.addVertex(0,radius*1,0);
        this.addNormal(0,1,0);
        this.addTexCoord(0.5f,0);

        this.addVertex(0,radius*-1,0);
        this.addNormal(0,-1,0);
        this.addTexCoord(0.5f,1);

        for( j=0; j<height-3; j++ ) {
            for( i=0; i<width-1; i++ )  {
                this.addIndex( (j  )*width + i  );
                this.addIndex( (j+1)*width + i+1);
                this.addIndex( (j  )*width + i+1);
                this.addIndex( (j  )*width + i  );
                this.addIndex( (j+1)*width + i  );
                this.addIndex( (j+1)*width + i+1);
            }
        }
        for( i=0; i<width-1; i++ )  {
            this.addIndex( (height-2)*width );
            this.addIndex( i );
            this.addIndex( i+1 );
            this.addIndex( (height-2)*width+1 );
            this.addIndex( (height-3)*width + i+1 );
            this.addIndex( (height-3)*width + i );
        }
    }

    public void setDetail(int v) {
        detail = Math.max(1,v);
    }

    public double getDetail() { return detail; }
/*
    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("detail",detail.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        detail.parseJSON(jo.getJSONObject("detail"),context);
    }
*/
}
