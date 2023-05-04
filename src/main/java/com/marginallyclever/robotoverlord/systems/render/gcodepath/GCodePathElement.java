package com.marginallyclever.robotoverlord.systems.render.gcodepath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A single element in a {@link GCodePath}.  For example, a line, arc, or tool change.
 * @author Dan Royer
 * @since 2.5.0
 */
public class GCodePathElement {
    /**
     * The command to send to the machine.  For example, G0, G1, G2, G3, M3, M5, M30, etc.
     */
    private String command;

    /**
     * x,y,z are used for 3-axis machines
     */
    private double x, y, z;

    /**
     * u,v,w are used for 5-axis and 6-axis machines
     */
    private double u, v, w;

    /**
     * The feedrate to use for this gcodepath element.  If null, use the default feedrate.
     */
    private Double feedrate;

    /**
     * The tool to use for this gcodepath element.  If null, use the default tool.
     */
    private Integer toolChange;

    /**
     * The I/O operation to perform.  For example, M3, M5, M30, etc.
     */
    private String ioOperation;

    /**
     * The amount of material to extrude.  If null, do not extrude.
     */
    private Double extrusion;

    public GCodePathElement(String command) {
        this.command = command;
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.u = 0;
        this.v = 0;
        this.w = 0;
        this.feedrate = null;
        this.toolChange = null;
        this.ioOperation = null;
        this.extrusion = null;
    }

    public GCodePathElement(GCodePathElement other) {
        this.command = other.command;
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.u = other.u;
        this.v = other.v;
        this.w = other.w;
        this.feedrate = other.feedrate;
        this.toolChange = other.toolChange;
        this.ioOperation = other.ioOperation;
        this.extrusion = other.extrusion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(command);
        sb.append(" X").append(x);
        sb.append(" Y").append(y);
        sb.append(" Z").append(z);
        sb.append(" U").append(u);
        sb.append(" V").append(v);
        sb.append(" W").append(w);
        if (extrusion != null) {
            sb.append(" E").append(extrusion);
        }
        if (feedrate != null) {
            sb.append(" F").append(feedrate);
        }
        if (toolChange != null) {
            sb.append(" T").append(toolChange);
        }
        if (ioOperation != null) {
            sb.append(" ").append(ioOperation);
        }
        return sb.toString();
    }

    public void setCommand(String s) {
        command = s;
    }

    public String getCommand() {
        return command;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getU() {
        return u;
    }

    public void setU(double u) {
        this.u = u;
    }

    public double getV() {
        return v;
    }

    public void setV(double v) {
        this.v = v;
    }

    public double getW() {
        return w;
    }

    public void setW(double w) {
        this.w = w;
    }

    public Double getFeedrate() {
        return feedrate;
    }

    public void setFeedrate(Double feedrate) {
        this.feedrate = feedrate;
    }

    public Double getExtrusion() {
        return extrusion;
    }

    public void setExtrusion(Double extrusion) {
        this.extrusion = extrusion;
    }

    public Integer getToolChange() {
        return toolChange;
    }

    public void setToolChange(Integer toolChange) {
        this.toolChange = toolChange;
    }

    public String getIoOperation() {
        return ioOperation;
    }

    public void setIoOperation(String ioOperation) {
        this.ioOperation = ioOperation;
    }

    public double getI() {
        return getValue("I");
    }

    public double getJ() {
        return getValue("J");
    }

    private double getValue(String key) {
        Pattern pattern = Pattern.compile("(["+key+"])(-?\\d*(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(command);

        if (matcher.find()) {
            String parameter = matcher.group(1);
            double value = Double.parseDouble(matcher.group(2));

            if (parameter.equals(key)) {
                return value;
            }
        }

        return 0;
    }

    public boolean isClockwise() {
        return command.contains("G2");
    }
}
