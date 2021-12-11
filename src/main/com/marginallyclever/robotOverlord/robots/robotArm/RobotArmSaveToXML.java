package com.marginallyclever.robotOverlord.robots.robotArm;

import java.io.File;

import javax.vecmath.Matrix3d;
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

import com.marginallyclever.convenience.log.Log;

public class RobotArmSaveToXML {
	public static void save(String filePath,RobotArmFK arm) throws Exception {
        Log.message("RobotArmSaveToXML.save() start");
        
        Document document = setupNewDocument();
        buildDocumentFromArm(document,arm);
        
        createXMLFile(document,filePath);
        
        Log.message("RobotArmSaveToXML.save() done");
	}

	private static void buildDocumentFromArm(Document document, RobotArmFK arm) {
        Element root = document.createElement("robot");
        root.setAttribute("name", arm.getName());
        document.appendChild(root);

    	Element link = document.createElement("link");
    	link.setAttribute("name", "base");
        root.appendChild(link);
        buildBaseLink(document,arm);
        
        for(int i=0;i<arm.getNumBones();++i) {
            link = buildLinkFromBone(document,arm.getName(),arm.getBone(i));	
            root.appendChild(link);
        }
        
        String prev = "base";
        for(int i=0;i<arm.getNumBones();++i) {
        	String next = arm.getBone(i).getName();
        	
        	Element joint = document.createElement("joint");
            root.appendChild(joint);
            
	        	Element parent = document.createElement("parent");
	        	joint.appendChild(parent);
	        	parent.setAttribute("link", prev);
	        	
	        	Element child = document.createElement("child");
	        	joint.appendChild(child);
	        	child.setAttribute("link", next);
	        	
	        	Element axis = document.createElement("axis");
	        	joint.appendChild(axis);
	        	// TODO
	        	
	        	Element origin = document.createElement("origin");
	        	joint.appendChild(origin);
	        	// TODO
        	
        	prev = next;
        }
	}

	private static void buildBaseLink(Document document, RobotArmFK arm) {
		// TODO Auto-generated method stub
	}

	private static Element buildLinkFromBone(Document document, String armName, RobotArmBone bone) {
		Element link = document.createElement("link");
    	link.setAttribute("name", bone.getName());

	       	Element geometry = document.createElement("geometry");
	    	Element mesh = document.createElement("mesh");
	    	geometry.appendChild(mesh);
	    	mesh.setAttribute("file","package://"+armName+bone.getShape().getModelFilename());
	    	
	    	Element inertial = document.createElement("inertial");
	    	link.appendChild(inertial);
	    	Element iorigin = document.createElement("origin");
	    	inertial.appendChild(iorigin);
	    	iorigin.setAttribute("xyz", formatXYZ(bone.getCenterOfMass()));
	    	iorigin.setAttribute("rpy", "0 0 0");
	    	Element inertia = document.createElement("inertia");
	    	inertial.appendChild(inertia);
	    	Matrix3d m = bone.getInertiaTensor();
	    	inertia.setAttribute("ixx", Double.toString(m.m00));
	    	inertia.setAttribute("ixy", Double.toString(m.m01));
	    	inertia.setAttribute("ixz", Double.toString(m.m02));
	    	inertia.setAttribute("iyy", Double.toString(m.m11));
	    	inertia.setAttribute("iyz", Double.toString(m.m12));
	    	inertia.setAttribute("izz", Double.toString(m.m22));
	    	
	    	Element mass = document.createElement("mass");
	    	inertial.appendChild(mass);
	    	mass.setAttribute("value",Double.toString(bone.getMass()));
	    	
	    	Element visual = document.createElement("visual");
	    	link.appendChild(visual);
	    	visual.setAttribute("name",bone.getName());
	    	visual.appendChild(geometry.cloneNode(true));
	    	
	    	Element material = document.createElement("material");
	    	visual.appendChild(material);
	    	material.setAttribute("name",bone.getName());
	    	
	    	Element collision = document.createElement("collision");
	    	link.appendChild(collision);
	    	collision.setAttribute("texture",bone.getShape().getMaterial().getTextureFilename());
	    	collision.appendChild(geometry);
    	
    	return link;
	}

	/**
	 * See https://danceswithcode.net/engineeringnotes/rotations_in_3d/rotations_in_3d_part2.html
	 * @param m
	 * @return
	 */
	private static String formatRPY(Matrix3d m) {
        double[] angle = new double[3];
        angle[1] = -Math.asin( m.m20 );  //Pitch

        if( m.m20 == 1 ){
        	//Gymbal lock: pitch = -90
            angle[0] = 0.0;             //yaw = 0
            angle[2] = Math.atan2( -m.m01, -m.m02 );    //Roll
            System.out.println("Gimbal lock: pitch = -90");
        } else if( m.m20 == -1 ){
            //Gymbal lock: pitch = 90    
            angle[0] = 0.0;             //yaw = 0
            angle[2] = Math.atan2( m.m01, m.m02 );    //Roll
            System.out.println("Gimbal lock: pitch = 90");
        } else{
            //General solution
            angle[0] = Math.atan2(  m.m10, m.m00 );
            angle[2] = Math.atan2(  m.m21, m.m22 );
            System.out.println("No gimbal lock");
        }
        
        //Euler angles are now in order yaw, pitch, roll

		return  Double.toString(angle[2])+" "+
				Double.toString(angle[1])+" "+
				Double.toString(angle[0]);
	}

	private static String formatXYZ(Tuple3d p) {
		return  Double.toString(p.x)+" "+
				Double.toString(p.y)+" "+
				Double.toString(p.z);
	}

	private static void createXMLFile(Document document, String filePath) throws Exception {
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

	private static Document setupNewDocument() throws Exception {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        return document;
	}
}
