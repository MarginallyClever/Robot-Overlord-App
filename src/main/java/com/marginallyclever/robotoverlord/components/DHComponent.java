package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.OpenGLHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.BooleanParameter;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONException;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * A Pose component contains the local transform of an Entity - its position, rotation, and scale relative to its
 * parent.
 * @author Dan Royer
 * @since 2022-08-04
 */
@ComponentDependency(components={PoseComponent.class})
public class DHComponent extends ShapeComponent implements PropertyChangeListener {
    public final BooleanParameter isRevolute = new BooleanParameter("Revolute",true);
    public final DoubleParameter myD = new DoubleParameter("D",0.0);
    public final DoubleParameter myR = new DoubleParameter("R",0.0);
    public final DoubleParameter alpha = new DoubleParameter("Alpha",0.0);
    public final DoubleParameter theta = new DoubleParameter("Theta",0.0);
    public final DoubleParameter jointMax = new DoubleParameter("Max",180.0);
    public final DoubleParameter jointMin = new DoubleParameter("Min",-180.0);
    public final DoubleParameter jointHome = new DoubleParameter("Home",0.0);

    @Override
    public void onAttach() {
        refreshLocalMatrix();
        myMesh = new Mesh();
        MaterialComponent mat = getEntity().getComponent(MaterialComponent.class);
        mat.drawOnTop.set(true);
    }

    public DHComponent() {
        super();
        myD.addPropertyChangeListener(this);
        myR.addPropertyChangeListener(this);
        alpha.addPropertyChangeListener(this);
        theta.addPropertyChangeListener(this);
        setVisible(false);
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("D",myD.toJSON(context));
        jo.put("R",myR.toJSON(context));
        jo.put("Alpha", alpha.toJSON(context));
        jo.put("Theta", theta.toJSON(context));
        jo.put("ThetaMax", jointMax.toJSON(context));
        jo.put("ThetaMin", jointMin.toJSON(context));
        jo.put("ThetaHome", jointHome.toJSON(context));
        jo.put("Revolute", isRevolute.toJSON(context));
        jo.put("Home", jointHome.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
        super.parseJSON(jo,context);
        myD.parseJSON(jo.getJSONObject("D"),context);
        myR.parseJSON(jo.getJSONObject("R"),context);
        alpha.parseJSON(jo.getJSONObject("Alpha"),context);
        theta.parseJSON(jo.getJSONObject("Theta"),context);
        if(jo.has("ThetaMax")) jointMax.parseJSON(jo.getJSONObject("ThetaMax"),context);
        if(jo.has("ThetaMin")) jointMin.parseJSON(jo.getJSONObject("ThetaMin"),context);
        if(jo.has("ThetaHome")) jointHome.parseJSON(jo.getJSONObject("ThetaHome"),context);
        if(jo.has("Revolute")) isRevolute.parseJSON(jo.getJSONObject("Revolute"),context);
        if(jo.has("Home")) jointHome.parseJSON(jo.getJSONObject("Home"),context);
        refreshLocalMatrix();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        refreshLocalMatrix();
    }

    private void refreshLocalMatrix() {
        setLocalMatrix(getLocalMatrix());
    }

    private void setLocalMatrix(Matrix4d localMatrix) {
        Entity entity = getEntity();
        if(entity==null) return;

        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        if(pose==null) {
            pose = new PoseComponent();
            getEntity().addComponent(pose);
        }

        pose.setLocalMatrix4(localMatrix);
    }

    /**
     * @return the local transform of this entity, calculated from its D,R, alpha, and theta.
     */
    private Matrix4d getLocalMatrix() {
        Matrix4d m = new Matrix4d();
        double rt = Math.toRadians(theta.get());
        double ra = Math.toRadians(alpha.get());
        double ct = Math.cos(rt);
        double ca = Math.cos(ra);
        double st = Math.sin(rt);
        double sa = Math.sin(ra);

        double r = myR.get();

        m.m00 = ct;		m.m01 = -st*ca;		m.m02 = st*sa;		m.m03 = r*ct;
        m.m10 = st;		m.m11 = ct*ca;		m.m12 = -ct*sa;		m.m13 = r*st;
        m.m20 = 0;		m.m21 = sa;			m.m22 = ca;			m.m23 = myD.get();
        m.m30 = 0;		m.m31 = 0;			m.m32 = 0;			m.m33 = 1;

        return m;
    }

    @Override
    public String toString() {
        return super.toString()
                +",d="+myD.get()
                +",r="+myR.get()
                +",alpha="+ alpha.get()
                +",theta="+ theta.get()
                +",jointMax="+ jointMax.get()
                +",jointMin="+ jointMin.get()
                +",jointHome="+ jointHome.get()
                +",revolute="+ isRevolute.get()
                +",\n";
    }

    public void setJointValueWRTLimits(double t) {
        if (isRevolute.get()) setRevoluteWRTLimits(t);
        else setPrismaticWRTLimits(t);
    }

    private void setRevoluteWRTLimits(double angle) {
        // if max angle and min angle overlap then there is no limit on this joint.
        double max = jointMax.get();
        double min = jointMin.get();

        double bMiddle = (max+min)/2.0;
        double bMax = Math.abs(max-bMiddle);
        double bMin = Math.abs(min-bMiddle);
        if(bMin+bMax<360) {
            // prevent pushing the arm to an illegal angle
            angle = Math.max(Math.min(angle, max), min);
        }

        theta.set(angle % 360);
    }

    private void setPrismaticWRTLimits(double d) {
        // if max angle and min angle overlap then there is no limit on this joint.
        double max = jointMax.get();
        double min = jointMin.get();

        myD.set(Math.max(Math.min(d, max), min));
    }

    /**
     * @return the local pose of this entity.
     */
    public Matrix4d getLocal() {
        PoseComponent pose = getEntity().getComponent(PoseComponent.class);
        if(pose==null) return null;
        return pose.getLocal();
    }

    /**
     * Set the joint parameters.
     * @param d distance from previous joint along Z axis.  This value changes in a prismatic joint.
     * @param r distance from previous joint along X axis
     * @param alpha angle from previous joint, rotation around X axis
     * @param theta angle from previous joint, rotation around Z axis.  This value changes in a revolute joint.
     * @param jointMax maximum value of moving joint
     * @param jointMin minimum value of moving joint
     * @param isRevolute true if joint is revolute, false if joint is prismatic
     */
    public void set(double d, double r, double alpha, double theta, double jointMax, double jointMin, boolean isRevolute) {
        this.myD.set(d);
        this.myR.set(r);
        this.alpha.set(alpha);
        this.theta.set(theta);
        this.jointMax.set(jointMax);
        this.jointMin.set(jointMin);
        this.isRevolute.set(isRevolute);
        refreshLocalMatrix();
    }

    public void setD(double d) {
        if(!isRevolute.get()) {
            setPrismaticWRTLimits(d);
        } else {
            myD.set(d);
        }
    }

    public double getD() { return myD.get(); }

    public void setR(double r) {
        myR.set(r);
    }

    public double getR() {
        return myR.get();
    }

    public void setAlpha(double a) {
        alpha.set(a);
    }

    public double getAlpha() { return alpha.get(); }

    public void setTheta(double angle) {
        if(isRevolute.get()) {
            setRevoluteWRTLimits(angle);
        } else {
            theta.set(angle);
        }
    }

    public double getTheta() {
        return theta.get();
    }

    public void setJointMax(double v) {
        jointMax.set(v);
    }

    public double getJointMax() {
        return jointMax.get();
    }

    public void setJointMin(double v) {
        jointMin.set(v);
    }

    public double getJointMin() {
        return jointMin.get();
    }

    public double getJointHome() {
        return jointHome.get();
    }

    public void setJointHome(double t) {
        jointHome.set(t);
    }

    public double getJointValue() {
        if(isRevolute.get())
            return getTheta();
        else
            return getD();
    }

    public void setJointValue(double t) {
        if(isRevolute.get())
            setTheta(t);
        else
            setD(t);
    }

    @Override
    public void render(GL3 gl) {
        boolean tex = OpenGLHelper.disableTextureStart(gl);
        int onTop = OpenGLHelper.drawAtopEverythingStart(gl);

        Matrix4d m = getLocal();
        //m.invert();
        m.transpose();

        float rt = (float)Math.toRadians(theta.get());
        float ct = (float)Math.cos(rt);
        float st = (float)Math.sin(rt);
        float r = myR.get().floatValue();

        Point3d d = new Point3d(-r*ct, -r*st,0);
        Point3d dr = new Point3d(-r*ct, -r*st,-myD.get().floatValue());
        m.transform(d);
        m.transform(dr);

        myMesh.clear();
        myMesh.setRenderStyle(GL3.GL_LINES);
        myMesh.addColor(0,1,1,1);            myMesh.addVertex(0,0,0);
        myMesh.addColor(0,1,1,1);            myMesh.addVertex((float)d.x,(float)d.y,(float)d.z);
        myMesh.addColor(1,1,0,1);            myMesh.addVertex((float)d.x,(float)d.y,(float)d.z);
        myMesh.addColor(1,1,0,1);            myMesh.addVertex((float)dr.x,(float)dr.y,(float)dr.z);

        myMesh.addColor(1,0,0,1); myMesh.addVertex(0,0,0);
        myMesh.addColor(1,0,0,1); myMesh.addVertex(5,0,0);
        myMesh.addColor(0,1,0,1); myMesh.addVertex(0,0,0);
        myMesh.addColor(0,1,0,1); myMesh.addVertex(0,5,0);
        myMesh.addColor(0,0,1,1); myMesh.addVertex(0,0,0);
        myMesh.addColor(0,0,1,1); myMesh.addVertex(0,0,5);
        myMesh.render(gl);

        OpenGLHelper.drawAtopEverythingEnd(gl, onTop);
        OpenGLHelper.disableTextureEnd(gl, tex);
    }

    public void setRevolute(boolean b) {
        isRevolute.set(b);
    }
    public boolean isRevolute() {
        return isRevolute.get();
    }
}
