package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.robots.Robot;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

/**
 * A Three-wheeled robot is composed of two driving wheels, and a third free spinning wheel. 
 * Steering is achieved my varying speed of the two driving wheels.
 * @author Giacomo Rossetoo
 * @since 2023-06-03
 */
@ComponentDependency(components = {PoseComponent.class})
public class ThreeWheeledRobotComponent extends WheeledRobotComponent {
  
}
