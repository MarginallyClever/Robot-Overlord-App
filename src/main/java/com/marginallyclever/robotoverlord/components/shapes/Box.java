package com.marginallyclever.robotoverlord.components.shapes;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MathHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Vector3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A box with a width, height, and length of 1.  It is centered around the origin.
 */
@Deprecated
public class Box extends ShapeComponent implements PropertyChangeListener {
    public final DoubleParameter width = new DoubleParameter("width",1.0);
    public final DoubleParameter height = new DoubleParameter("height",1.0);
    public final DoubleParameter length = new DoubleParameter("length",1.0);

    public Box() {
        this(1.0,1.0,1.0);
    }

    /**
     * Create a box with the given dimensions.
     * @param width
     * @param length
     * @param height
     */
    public Box(double width,double length,double height) {
        super();

        this.width.set(width);
        this.length.set(length);
        this.height.set(height);

        this.width.addPropertyChangeListener(this);
        this.length.addPropertyChangeListener(this);
        this.height.addPropertyChangeListener(this);

        updateModel();
    }

    // Procedurally generate a list of triangles that form a box, subdivided by some amount.
    private void updateModel() {    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateModel();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json = super.toJSON(context);
        json.put("width", width.get());
        json.put("length", length.get());
        json.put("height", height.get());
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        if(jo.has("width")) width.set(jo.getDouble("width"));
        if(jo.has("length")) length.set(jo.getDouble("length"));
        if(jo.has("height")) height.set(jo.getDouble("height"));
    }
}
