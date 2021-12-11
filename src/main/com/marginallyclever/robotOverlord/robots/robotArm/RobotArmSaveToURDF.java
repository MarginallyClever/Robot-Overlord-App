package com.marginallyclever.robotOverlord.robots.robotArm;

import java.io.File;
import java.util.ArrayList;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.marginallyclever.convenience.Cuboid;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.StringHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.robotOverlord.shape.Shape;

/**
 * Export the {@code RobotArm} as a URDF XML file.
 * See http://wiki.ros.org/urdf/XML
 * See https://adohaha.github.io/DH2URDF/
 * See http://docs.ros.org/en/rolling/Tutorials/URDF/Building-a-Visual-Robot-Model-with-URDF-from-Scratch.html#
 * Tested against http://www.mymodelrobot.appspot.com/
 * @author Dan Royer
 *
 */
public class RobotArmSaveToURDF {
    private Document document;
    private RobotArmFK arm;
	
	
	public void save(String filePath,RobotArmFK arm) throws Exception {
        Log.message(RobotArmSaveToURDF.class.getSimpleName()+".save() start");

        this.arm=arm;
        this.document = setupNewDocument();
        
        buildDocumentFromArm();
        
        createXMLFile(document,filePath);
        
        Log.message(RobotArmSaveToURDF.class.getSimpleName()+".save() done");
	}

	private void buildDocumentFromArm() {
        Element root = document.createElement("robot");
        root.setAttribute("name", arm.getName());
        document.appendChild(root);

		Matrix4d current = new Matrix4d();
		current.setIdentity();
    	Element link = buildBaseLink(); 
        root.appendChild(link);
        
        for(int i=0;i<arm.getNumBones();++i) {
        	RobotArmBone bone = arm.getBone(i);
			bone.updateMatrix();
			current.mul(bone.getPose());
			Matrix4d iWP = new Matrix4d(current);
			iWP.invert();
			
            link = buildLinkFromBone(bone,iWP);	
            root.appendChild(link);
        }

        /*
        String prev = "base";
        for(int i=0;i<arm.getNumBones();++i) {
        	RobotArmBone bone = arm.getBone(i);
        	Element joint = buildJointFromBone(bone,prev);
            root.appendChild(joint);
        	prev = bone.getName();
        }*/
	}

	private Element buildJointFromBone(RobotArmBone bone, String prev) {
    	Element joint = document.createElement("joint");
    	joint.setAttribute("name", bone.getName());
        
    	Element parent = document.createElement("parent");
    	parent.setAttribute("link", prev);
    	joint.appendChild(parent);
    	
    	Element child = document.createElement("child");
    	child.setAttribute("link", bone.getName());
    	joint.appendChild(child);
    	
    	Element axis = document.createElement("axis");
    	axis.setAttribute("xyz", formatXYZ(MatrixHelper.getZAxis(bone.getPose())) );
    	joint.appendChild(axis);
    	
    	Element limit = document.createElement("limit");
    	limit.setAttribute("lower",StringHelper.formatDouble(Math.toRadians(bone.getAngleMin())));
    	limit.setAttribute("upper",StringHelper.formatDouble(Math.toRadians(bone.getAngleMax())));
    	joint.appendChild(limit);
    	
    	Element origin = document.createElement("origin");
	    	origin.setAttribute("xyz", formatXYZ(MatrixHelper.getPosition(bone.getPose())) );
	    	Matrix3d m3 = new Matrix3d();
	    	bone.getPose().get(m3);
	    	origin.setAttribute("rpy", formatRPY(m3));
    	joint.appendChild(origin);
	
		return joint;
	}

	private Element buildBaseLink() {
		Element base = document.createElement("link");
    	base.setAttribute("name", "base");
    	base.setAttribute("type","fixed");

    	Matrix4d m4i = arm.getPoseWorld();
    	m4i.invert();
    	
		ArrayList<Element> visuals = buildShapeNodesFromShape(arm.getBaseShape(),"visual",m4i);
		for( Element e : visuals ) {
			addMaterialToElement(e,arm.getBaseShape());
			base.appendChild(e);
		}
		
		ArrayList<Element> collisions = buildShapeNodesFromShape(arm.getBaseShape(),"collision",m4i);
		for( Element e : collisions ) base.appendChild(e);
    	
    	return base;
	}

	private void addMaterialToElement(Element e, Shape shape) {
    	Element material = document.createElement("material");
    	//material.setAttribute("name","material");
	    	Element texture = document.createElement("texture");
	    	texture.setAttribute("filename",shape.getMaterial().getTextureFilename());
	    	//material.appendChild(texture);
	    	Element color = document.createElement("color");
	    	color.setAttribute("rgba", "1 1 1 1");
	    	material.appendChild(color);
	    //e.appendChild(material);
	}

	private Element buildLinkFromBone(RobotArmBone bone,Matrix4d iWP) {
		Element link = document.createElement("link");
    	link.setAttribute("name", bone.getName());
    	link.setAttribute("type","revolute");

		ArrayList<Element> visuals = buildShapeNodesFromShape(bone.getShape(),"visual",iWP);
		for( Element e : visuals ) {
			addMaterialToElement(e,bone.getShape());
			link.appendChild(e);
		}
		
		ArrayList<Element> collisions = buildShapeNodesFromShape(bone.getShape(),"collision",iWP);
		for( Element e : collisions ) link.appendChild(e);
    		
	    //link.appendChild(buildInertiaFromBone(document,bone));
    	
    	return link;
	}

	private ArrayList<Element> buildShapeNodesFromShape(Shape shape, String tagName,Matrix4d iWP) {
		ArrayList<Element> elements = new ArrayList<Element>();
		
		ArrayList<Cuboid> list = shape.getCuboidList();
       	for(Cuboid c : list ) {
       		Element e = document.createElement(tagName);
	    	//e.setAttribute("name","cuboid");
       		Matrix4d m = c.getPose();
       		m.mul(iWP);
       		c.setPose(m);
       		c.updatePoints();
       		e.appendChild(buildGeometryFromCuboid(c));
       		e.appendChild(buildOriginFromCuboid(c));
       		elements.add(e);
       	}
       	
   		Element mesh = document.createElement(tagName);
	    	//mesh.setAttribute("name","mesh");
		    mesh.appendChild(buildGeometryFromMesh(arm.getName(),shape.getModelFilename()));
    	//elements.add(mesh);
    	
		return elements;
	}

	private Node buildGeometryFromMesh(String armName, String filename) {
       	Element geometry = document.createElement("geometry");
	    	Element mesh = document.createElement("mesh");
	    	mesh.setAttribute("file","package://"+armName+filename);
    	geometry.appendChild(mesh);

		return geometry;
	}

	private Element buildGeometryFromCuboid(Cuboid cuboid) { 
       	Element geometry = document.createElement("geometry");
	       	Element box = document.createElement("box");
	   		box.setAttribute("size", cuboid.getExtentX()+" "+cuboid.getExtentY()+" "+cuboid.getExtentZ() );
       	geometry.appendChild(box);
   		
		return geometry;
	}

	private Element buildOriginFromCuboid(Cuboid cuboid) { 
       	Element origin = document.createElement("origin");
       	Point3d p = new Point3d();
       	p.add(cuboid.getBoundsBottom());
       	p.add(cuboid.getBoundsTop());
       	p.scale(0.5);
       	
       	origin.setAttribute("xyz", p.x+" "+p.y+" "+p.z );
   		
		return origin;
	}

	private Element buildInertiaFromBone(RobotArmBone bone) {
		Element inertial = document.createElement("inertial");
		
    	Element iorigin = document.createElement("origin");
    	inertial.appendChild(iorigin);
    	iorigin.setAttribute("xyz", formatXYZ(bone.getCenterOfMass()));
    	iorigin.setAttribute("rpy", "0 0 0");

    	Element inertia = document.createElement("inertia");
    	inertial.appendChild(inertia);
    	Matrix3d m = bone.getInertiaTensor();
    	inertia.setAttribute("ixx", StringHelper.formatDouble(m.m00));
    	inertia.setAttribute("ixy", StringHelper.formatDouble(m.m01));
    	inertia.setAttribute("ixz", StringHelper.formatDouble(m.m02));
    	inertia.setAttribute("iyy", StringHelper.formatDouble(m.m11));
    	inertia.setAttribute("iyz", StringHelper.formatDouble(m.m12));
    	inertia.setAttribute("izz", StringHelper.formatDouble(m.m22));
    	
    	Element mass = document.createElement("mass");
    	inertial.appendChild(mass);
    	mass.setAttribute("value",StringHelper.formatDouble(bone.getMass()));
    	
		return inertial;
	}

	/**
	 * See https://danceswithcode.net/engineeringnotes/rotations_in_3d/rotations_in_3d_part2.html
	 * @param m 3x3 orientation matrix 
	 * @return "a b c" where a=roll b=pitch c=yaw, all in radians.
	 */
	private String formatRPY(Matrix3d m) {
        double[] angle = new double[3];
        angle[1] = -Math.asin( m.m20 );  //Pitch

        if( m.m20 == 1 ){
        	//Gymbal lock: pitch = -90
            angle[0] = 0.0;             //yaw = 0
            angle[2] = Math.atan2( -m.m01, -m.m02 );  // Roll
            System.out.println("Gimbal lock: pitch = -90");
        } else if( m.m20 == -1 ){
            //Gymbal lock: pitch = 90    
            angle[0] = 0.0;             //yaw = 0
            angle[2] = Math.atan2( m.m01, m.m02 );   // Roll
            System.out.println("Gimbal lock: pitch = 90");
        } else{
            //General solution
            angle[0] = Math.atan2(  m.m10, m.m00 );  // Yaw
            angle[2] = Math.atan2(  m.m21, m.m22 );  // Roll
            System.out.println("No gimbal lock");
        }
        
        //Euler angles are now in order yaw, pitch, roll

		return  StringHelper.formatDouble(angle[2])+" "+
				StringHelper.formatDouble(angle[1])+" "+
				StringHelper.formatDouble(angle[0]);
	}

	private String formatXYZ(Tuple3d p) {
		return  StringHelper.formatDouble(p.x)+" "+
				StringHelper.formatDouble(p.y)+" "+
				StringHelper.formatDouble(p.z);
	}

	private void createXMLFile(Document document, String filePath) throws Exception {
        // create the xml file
        //transform the DOM Object to an XML File
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(filePath));

        // If you use
        // StreamResult result = new StreamResult(System.out);
        // the output will be pushed to the standard output ...
        // You can use that for debugging 

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(domSource, streamResult);		
	}

	private Document setupNewDocument() throws Exception {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        return document;
	}
}
