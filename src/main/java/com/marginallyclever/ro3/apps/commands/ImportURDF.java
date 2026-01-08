package com.marginallyclever.ro3.apps.commands;

import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.ro3.Registry;
import com.marginallyclever.ro3.factories.Lifetime;
import com.marginallyclever.ro3.mesh.Mesh;
import com.marginallyclever.ro3.mesh.proceduralmesh.Box;
import com.marginallyclever.ro3.mesh.proceduralmesh.Cylinder;
import com.marginallyclever.ro3.mesh.proceduralmesh.ProceduralMeshFactory;
import com.marginallyclever.ro3.node.Node;
import com.marginallyclever.ro3.node.nodes.*;
import com.marginallyclever.ro3.node.nodes.limbsolver.LimbSolver;
import com.marginallyclever.ro3.node.nodes.pose.Pose;
import com.marginallyclever.ro3.node.nodes.pose.poses.Limb;
import com.marginallyclever.ro3.node.nodes.pose.poses.MeshInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.vecmath.Vector3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * <p>Loads a URDF file into the current scene.  See also <a href="https://wiki.ros.org/urdf/XML">URDF XML format</a>.</p>
 *
 * <p>In the URDF specification, the default units of measurement are strictly defined by the <a href="https://en.wikipedia.org/wiki/International_System_of_Units">International System of Units (SI)</a>.
 * This means the expected measurement is meters, where Robot Overlord is in centimeters.</p>
 */
public class ImportURDF extends AbstractUndoableEdit {
    private static final Logger logger = LoggerFactory.getLogger(ImportURDF.class);
    private final static double SCALE = 100; // URDF is in meters, RO3 is in centimeters.
    private final static String [] MOTOR_NAMES = {"X","Y","Z","A","B","C","U","V","W"};
    private final File selectedFile;
    private Node created;

    public ImportURDF(File selectedFile) {
        super();
        this.selectedFile = selectedFile;
        execute();
    }

    @Override
    public String getPresentationName() {
        return "Import " + selectedFile.getName();
    }

    @Override
    public void redo() {
        super.redo();
        execute();
    }

    /**
     * Load a URDF file into the current scene.
     */
    public void execute() {
        if( selectedFile == null ) throw new InvalidParameterException("Selected file is null.");
        if( !selectedFile.exists() ) throw new InvalidParameterException("File does not exist.");

        logger.info("Import URDF from {}",selectedFile.getAbsolutePath());

        // do it!
        String newCWD = selectedFile.getParent() + File.separator;
        String oldCWD = PathHelper.getCurrentWorkingDirectory();
        PathHelper.setCurrentWorkingDirectory(newCWD);

        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(selectedFile.getAbsolutePath()));
            // Add the loaded scene to the current scene.
            created = createFromXML(bis);
            Registry.getScene().addChild(created);
            Registry.getPhysics().deferredAction(created);
        } catch (IOException e) {
            logger.error("Error loading scene from JSON", e);
        }

        PathHelper.setCurrentWorkingDirectory(oldCWD);
        logger.info("done.");
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();
        reverse();
    }

    public void reverse() {
        Node parent = created.getParent();
        parent.removeChild(created);
        created = null;
    }

    public static Node createFromXML(BufferedInputStream bis) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(bis);
            doc.getDocumentElement().normalize();
            // find the 'robot' element.
            if(!doc.getDocumentElement().getNodeName().contains("robot")) {
                throw new Exception("I can't find robot node!");
            }
            org.w3c.dom.Node robot = doc.getElementsByTagName("robot").item(0);
            Node loaded = Registry.nodeFactory.create("Pose");
            loaded.setName(robot.getNodeName());

            List<Pose> links = parseLinks(doc,loaded);
            parseJoints(doc,loaded,links);

            return loaded;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse link elements from URDF XML.
     * @param doc the XML document
     * @param root the root Node to attach parsed links
     * @return the list of links
     */
    private static List<Pose> parseLinks(Document doc, Node root) {
        // find all 'link' elements
        NodeList list = doc.getElementsByTagName("link");
        logger.debug("Found "+list.getLength()+" link elements.");
        List<Pose> links = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            org.w3c.dom.Node xmlLink = list.item(i);
            var linkNode = (Pose)Registry.nodeFactory.create("Pose");
            linkNode.setName(xmlLink.getAttributes().getNamedItem("name").getNodeValue());
            parseLinkElements(xmlLink, linkNode);
            links.add(linkNode);
        }
        return links;
    }

    private static void parseLinkElements(org.w3c.dom.Node xmlLink, Node linkNode) {
        parseInertia(xmlLink,linkNode);  // parse inertia
        parseCollision(xmlLink,linkNode);  // parse collision
        parseAllVisuals(xmlLink,linkNode);  // parse visual
    }

    private static void parseInertia(org.w3c.dom.Node xmlLink, Node linkNode) {
        // TODO implement me
        logger.debug("parseInertia not implemented yet.");
    }

    private static org.w3c.dom.Node findChildByName(org.w3c.dom.Node parent, String name) {
        NodeList list = parent.getChildNodes();
        for(int i=0; i<list.getLength(); i++) {
            org.w3c.dom.Node child = list.item(i);
            if(child.getNodeName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    // find and process all 'visual' elements
    private static void parseAllVisuals(org.w3c.dom.Node xmlLink, Node linkNode) {
        NodeList list = xmlLink.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            var item = list.item(i);
            if(item.getNodeName().equals("visual")) {
                parseVisual(item, linkNode);
            }
        }
    }

    /**
     * <p><b>visual</b> (option)<br/>
     * The visual properties of the link. This element specifies the shape of the object (box, cylinder, etc.) for
     * visualization purposes. <b>Note</b>: multiple instances of visual tags can exist for the same link. The union of the
     * geometry they define forms the visual representation of the link.</p>
     *
     * @param visual the XML node
     * @param linkNode the root Node to attach parsed visual elements
     */
    private static void parseVisual(org.w3c.dom.Node visual, Node linkNode) {
        Pose p = (Pose)Registry.nodeFactory.create("Pose");

        // name is optional
        org.w3c.dom.Node nameAttr = visual.getAttributes().getNamedItem("name");
        if(nameAttr!=null) {
            String name = nameAttr.getNodeValue();
            logger.debug("  Found name: {}", name);
            p.setName(name);
        } else {
            p.setName("Visual");
        }

        // geometry is required inside visual
        MeshInstance myMesh = parseGeometry(visual);
        p.addChild(myMesh);

        // material is optional
        Material myMaterial = parseMaterial(visual);
        if(myMaterial!=null) p.addChild(myMaterial);

        // origin is optional
        adjustOrigin(visual,myMesh);

        linkNode.addChild(p);
    }

    private static void adjustOrigin(org.w3c.dom.Node xmlNode, Pose pose) {
        org.w3c.dom.Node origin = findChildByName(xmlNode,"origin");
        if(origin==null) return;
        logger.debug("  Found origin");
        // xyz is optional
        String xyzStr = origin.getAttributes().getNamedItem("xyz").getNodeValue();
        if (xyzStr != null) {
            logger.debug("    Found translation: "+xyzStr);
            String [] xyz = xyzStr.split(" ");
            pose.setPosition(new Vector3d(
                    Double.parseDouble(xyz[0]) * SCALE,
                    Double.parseDouble(xyz[1]) * SCALE,
                    Double.parseDouble(xyz[2]) * SCALE)
            );
        }
        // rpy is optional
        String rpyStr = origin.getAttributes().getNamedItem("rpy").getNodeValue();
        if(rpyStr != null) {
            logger.debug("    Found rotation: " + rpyStr);
            String[] rpy = rpyStr.split(" ");
            pose.setRotationEuler(
                    new Vector3d(
                            Math.toDegrees(Double.parseDouble(rpy[0])),
                            Math.toDegrees(Double.parseDouble(rpy[1])),
                            Math.toDegrees(Double.parseDouble(rpy[2]))
                    ),
                    MatrixHelper.EulerSequence.ZXY
            );
        }
    }

    /**
     * <b>material></b> (optional)<br/>
     * <p>The material of the visual element. It is allowed to specify a material element outside of the 'link' object, in the top level 'robot' element. From within a link element you can then reference the material by name.</p>
     * <ul>
     * <li><b>name</b><br/>
     *     name of the material
     * </li>
     * <li><b>color</b> (optional)<br/>
     * rgba The color of a material specified by set of four numbers representing red/green/blue/alpha, each in the range of [0,1].
     * </li>
     * <li><b>texture</b> (optional)<br/>
     * The texture of a material is specified by a filename</li>
     * </ul>
     * @param visual the XML node
     * @return the Material created from the visual
     */
    private static Material parseMaterial(org.w3c.dom.Node visual) {
        org.w3c.dom.Node material = findChildByName(visual,"material");
        if(material==null) return null;
        logger.debug("  Found material");
        Material myMaterial = new Material();

        // get the optional color
        org.w3c.dom.Node colorAttr = visual.getAttributes().getNamedItem("color");
        if(colorAttr!=null) {
            String colorStr = colorAttr.getNodeValue();
            logger.debug("    Found color: {}", colorStr);
            String [] rgba = colorStr.split(" ");
            int r = (int)(Double.parseDouble(rgba[0]) * 255);
            int g = (int)(Double.parseDouble(rgba[1]) * 255);
            int b = (int)(Double.parseDouble(rgba[2]) * 255);
            int a = (int)(Double.parseDouble(rgba[3]) * 255);
            myMaterial.setDiffuseColor(new Color(r,g,b,a));
        }
        // get the optional texture
        org.w3c.dom.Node textureAttr = visual.getAttributes().getNamedItem("texture");
        if(textureAttr!=null) {
            String textureStr = textureAttr.getNodeValue();
            logger.debug("    Found texture: {}", textureStr);
            myMaterial.setDiffuseTexture(Registry.textureFactory.get(Lifetime.SCENE, textureStr));
        }

        return myMaterial;
    }

    /**
     * <p><b>geometry</b> (required)<br/>
     * The shape of the visual object. This can be one of the following:</p>
     * <ul>
     *     <li><b>box</b><br/>
     *     size attribute contains the three side lengths of the box. The origin of the box is in its center.</li>
     *     <li><b>cylinder</b><br/>
     *     Specify the radius and length. The origin of the cylinder is in its center. cylinder_coordinates.png</li>
     *     <li><b>sphere</b><br/>
     *     Specify the radius. The origin of the sphere is in its center.</li>
     *     <li><b>mesh</b><br/>
     *     A trimesh element specified by a filename, and an optional scale that scales the mesh's axis-aligned-bounding-box.
     *     Any geometry format is acceptable but specific application compatibility is dependent on implementation.
     *     The recommended format for best texture and color support is Collada .dae files. The mesh file is not transferred between machines referencing the same model.
     *     It must be a local file. Prefix the filename with <code>package://&lt;packagename&gt;/&lt;path&gt;</code> to make the path to the mesh file relative to the package <code>&lt;packagename&gt;</code>.</li>
     * </ul>
     *
     * @param visual the XML node
     * @return the MeshInstance created from the geometry
     */
    private static MeshInstance parseGeometry(org.w3c.dom.Node visual) {
        // geometry is required
        org.w3c.dom.Node geometry = findChildByName(visual,"geometry");
        assert geometry != null;

        MeshInstance myMesh = new MeshInstance();

        org.w3c.dom.Node box = findChildByName(geometry,"box");
        if(box!=null) {
            String sizeStr = box.getAttributes().getNamedItem("size").getNodeValue();
            logger.debug("Found visual box with size: "+sizeStr);
            String [] dimensions = sizeStr.split(" ");
            var boxMesh = (Box)ProceduralMeshFactory.createMesh("Box");
            assert boxMesh != null;
            boxMesh.length = Double.parseDouble(dimensions[0])*SCALE;
            boxMesh.height = Double.parseDouble(dimensions[1])*SCALE;
            boxMesh.width  = Double.parseDouble(dimensions[2])*SCALE;
            boxMesh.updateModel();
            myMesh.setMesh(boxMesh);
        }
        org.w3c.dom.Node cylinder = findChildByName(geometry,"cylinder");
        if(cylinder!=null) {
            String radiusStr = cylinder.getAttributes().getNamedItem("radius").getNodeValue();
            String lengthStr = cylinder.getAttributes().getNamedItem("length").getNodeValue();
            logger.debug("Found visual cylinder with radius: "+radiusStr+" length: "+lengthStr);
            var cylinderMesh = (Cylinder)ProceduralMeshFactory.createMesh("Cylinder");
            assert cylinderMesh != null;
            cylinderMesh.setRadius(Double.parseDouble(radiusStr)*SCALE);
            cylinderMesh.setLength(Double.parseDouble(lengthStr)*SCALE);
            cylinderMesh.updateModel();
            myMesh.setMesh(cylinderMesh);
        }
        org.w3c.dom.Node sphere = findChildByName(geometry,"sphere");
        if(sphere!=null) {
            String radiusStr = sphere.getAttributes().getNamedItem("radius").getNodeValue();
            logger.debug("Found visual sphere with radius: "+radiusStr);
            var sphereMesh = (com.marginallyclever.ro3.mesh.proceduralmesh.Sphere)ProceduralMeshFactory.createMesh("Sphere");
            assert sphereMesh != null;
            sphereMesh.radius = (float)(Double.parseDouble(radiusStr)*SCALE);
            sphereMesh.updateModel();
            myMesh.setMesh(sphereMesh);
        }
        org.w3c.dom.Node mesh = findChildByName(geometry,"mesh");
        if(mesh!=null) {
            String filename = mesh.getAttributes().getNamedItem("filename").getNodeValue();
            logger.debug("Found visual mesh with filename: "+filename);
            if(filename.startsWith("package://")) {
                filename = filename.replace("package://","");
                // remaining filename is relative to current working directory?
                filename = PathHelper.getCurrentWorkingDirectory() + filename;
            }
            // does the mesh already exist in the registry?

            Vector3d scale = new Vector3d(SCALE,SCALE,SCALE); // default scale
            // optional scale
            org.w3c.dom.Node scaleAttr = mesh.getAttributes().getNamedItem("scale");
            if(scaleAttr!=null) {
                String scaleStr = scaleAttr.getNodeValue();
                logger.debug("  Found scale: " + scaleStr);
                String[] scaleComponents = scaleStr.split(" ");
                scale.x *= Double.parseDouble(scaleComponents[0]);
                scale.y *= Double.parseDouble(scaleComponents[1]);
                scale.z *= Double.parseDouble(scaleComponents[2]);
            }
            Mesh fileMesh = Registry.meshFactory.get(Lifetime.SCENE,filename,scale);
            myMesh.setMesh(fileMesh);
        }

        return myMesh;
    }

    private static void parseCollision(org.w3c.dom.Node xmlLink, Node linkNode) {
        // TODO implement me
        logger.debug("parseCollision not implemented yet.");
    }

    /**
     * Parse joint elements from URDF XML.  They are declared in no particular order, so special care is taken to
     * build the hierarchy properly.
     * @param doc the XML document
     * @param root the root Node to attach parsed joints
     * @param links the list of links
     */
    private static void parseJoints(Document doc, Node root, List<Pose> links) {
        List<Motor> motors = new ArrayList<>();
        List<Pose> children = new ArrayList<>();

        // find all 'joint' elements
        NodeList list = doc.getElementsByTagName("joint");
        logger.debug("Found "+list.getLength()+" joint elements.");
        int motorCount=0;
        // Iterates joint elements; creates motors and hinge joints
        for (int i = 0; i < list.getLength(); i++) {
            org.w3c.dom.Node xmlJoint = list.item(i);
            // name is required
            String name = xmlJoint.getAttributes().getNamedItem("name").getNodeValue();
            // type is required
            String type = xmlJoint.getAttributes().getNamedItem("type").getNodeValue();

            // parent and child names are required
            Pose parent = findLinkPoseWithName(links, Objects.requireNonNull(findChildByName(xmlJoint, "parent")).getAttributes().getNamedItem("link").getNodeValue());
            Pose child = findLinkPoseWithName(links, Objects.requireNonNull(findChildByName(xmlJoint, "child")).getAttributes().getNamedItem("link").getNodeValue());
            assert child != null;
            assert parent != null;
            parent.addChild(child);
            children.add(child);

            // create an axle
            Pose axle = (Pose) Registry.nodeFactory.create("Pose");
            axle.setName(name);
            child.addChild(axle);

            // handle revolute joints
            // TODO handle other types of joints
            if(type.equals("revolute") || type.equals("continuous")) {
                // create joint that connects child to axle
                HingeJoint j = (HingeJoint) Registry.nodeFactory.create("HingeJoint");
                j.setName("Joint " + motorCount);
                child.addChild(j);
                j.setAxle(axle);

                // only revolute joints have limits.
                if(type.equals("revolute")) {
                    // optional joint limit
                    parseJointLimits(xmlJoint, j);
                }

                // attach motor to drive the joint
                Motor m = (Motor) Registry.nodeFactory.create("Motor");
                m.setName(MOTOR_NAMES[motorCount]);
                child.addChild(m);
                m.setHinge(j);
                motors.add(m);

                // warn if the optional axis param is not 0,0,1
                org.w3c.dom.Node axis = findChildByName(xmlJoint,"axis");
                if(axis!=null) {
                    String xyz = axis.getAttributes().getNamedItem("xyz").getNodeValue();
                    if(!xyz.equals("0 0 1")) {
                        logger.warn("Joint axis is not the expected 0 0 1: {}", xyz);
                    }
                }
                motorCount++;
            }

            // origin is optional
            adjustOrigin(xmlJoint,child);
        }

        // now that all links have been created, set up the hierarchy properly.
        // for any given axle, all siblings that are not a joint or a motor must become a child of the axle.
        for(Motor m : motors) {
            Pose axle = m.getHinge().getAxle();
            Pose parent = (Pose)axle.getParent();
            // move all siblings of axle to be children of axle
            List<Node> siblings = new ArrayList<>(parent.getChildren());
            for(Node n : siblings) {
                if(n!=axle && !(n instanceof MechanicalJoint) && !(n instanceof Motor)) {
                    parent.removeChild(n);
                    axle.addChild(n);
                }
            }
        }

        // links with no parents are at the root - attach them accordingly.
        for(Pose p : links) {
            if(p.getParent()==null) {
                root.addChild(p);
            }
        }

        // TODO The following assumes every URDF is for a single-limb robot arm and it shouldn't.

        // Sort the motors such that they are in the order of the joint hierarchy.
        List<Motor> sortedMotors = sortMotorsByHierarchy(motors,root);

        // add the sorted motors to a limb in the root of the URDF.
        Limb limb = (Limb)Registry.nodeFactory.create("Limb");
        root.addChild(limb);

        // the child-most link is the end effector.  that is to say the child with the most parents.
        Pose endEffector = null;
        int maxDepth = -1;
        for(Pose c : children) {
            // count hierarchy depth
            int depth = 0;
            Node current = c;
            while(current.getParent()!=null) {
                depth++;
                current = current.getParent();
            }
            if(depth>maxDepth) {
                maxDepth = depth;
                endEffector = c;
            }
        }
        limb.setEndEffector(endEffector);


        int max = Math.min(sortedMotors.size(), 6);
        for(int i=0;i<max;i++) {
            Motor m = sortedMotors.get(i);
            limb.setJoint(i,m);
        }

        // Add a limbSolver, put a target beneath limbsolver, and associate limbsolver with limb.
        LimbSolver limbSolver = (LimbSolver)Registry.nodeFactory.create("LimbSolver");
        limbSolver.setLimb(limb);
        root.addChild(limbSolver);
        Pose target = (Pose)Registry.nodeFactory.create("Pose");
        target.setName("target");
        limbSolver.addChild(target);
        limbSolver.setTarget(target);
        limbSolver.moveTargetToEndEffector();
    }

    /**
     * Parses joint limits from XML node
     */
    private static void parseJointLimits(org.w3c.dom.Node xmlJoint, HingeJoint j) {
        var limit = findChildByName(xmlJoint,"limit");
        if(limit!=null) {
            logger.debug("  Found limit");
            // Set maximum angle from XML attribute
            var upper = limit.getAttributes().getNamedItem("upper");
            if(upper!=null) {
                logger.debug("    Found upper: " + upper.getNodeValue());
                j.setMaxAngle(Math.toDegrees(Double.parseDouble(upper.getNodeValue())));
            }
            // Set minimum angle from XML attribute
            var lower = limit.getAttributes().getNamedItem("lower");
            if(lower!=null) {
                logger.debug("    Found lower: " + lower.getNodeValue());
                j.setMinAngle(Math.toDegrees(Double.parseDouble(lower.getNodeValue())));
            }
        }
    }

    /**
     * <p>Sort motors so that the order matches the order of the joint hierarchy as described by parentChildMap.
     * Motors are connected to HingeJoints connected to Axles somewhere in the hierarchy.  The first joint is the hinge
     * closest to the root, the second the nearest to the first, and so on.</p>
     * @param motors the list of motors to sort
     * @return the sorted list of motors
     */
    private static List<Motor> sortMotorsByHierarchy(List<Motor> motors,Node root) {
        List<Motor> sortedMotors = new ArrayList<>();
        // get a list of all axles so that we can find them in the hierarchy
        Map<Pose,Motor> axleToMotor = new HashMap<>();
        for(Motor m : motors) {
            axleToMotor.put(m.getHinge().getAxle(),m);
        }

        // starting from the root, walk the hierarchy looking for axles.
        Queue<Node> toVisit = new LinkedList<>();
        toVisit.add(root);
        while(!toVisit.isEmpty()) {
            Node current = toVisit.poll();
            Motor m = axleToMotor.get(current);
            if(m!=null) {
                // found an axle motor, add it to the sorted list.
                sortedMotors.add(m);
            }
            toVisit.addAll(current.getChildren());
        }

        return sortedMotors;
    }

    /**
     * Find a link Pose by name from a list of links.
     * @param links the list of links
     * @param name the name to find
     * @return the Pose with the given name, or null if not found
     */
    private static  Pose findLinkPoseWithName(List<Pose> links, String name) {
        for(Pose p : links) {
            if(p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
}
