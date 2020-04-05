package com.marginallyclever.robotOverlord.entity.scene.modelEntity.modelLoadAndSavers;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marginallyclever.robotOverlord.entity.scene.modelEntity.Model;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelLoadAndSave;

public class ModelLoadAndSaveAMF implements ModelLoadAndSave {
	@Override
	public String getEnglishName() { return "3D printing file (AMF)"; }
	@Override
	public String getValidExtensions() { return "amf"; }
	@Override
	public boolean canLoad() {	return true;	}
	@Override
	public boolean canSave() {	return false;	}

	@Override
	public boolean canLoad(String filename) {
		boolean result = filename.toLowerCase().endsWith(".amf");
		//System.out.println("ModelLoadAndSaveSTL.canLoad("+filename+")="+result);
		return result;
	}

	@Override
	public boolean canSave(String filename) {
		return false;
	}

	// much help from https://www.sculpteo.com/en/glossary/amf-definition/
	@Override
	public Model load(BufferedInputStream inputStream) throws Exception {
		Model model = new Model();
		
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();
        if(!doc.getDocumentElement().getNodeName().contains("amf")) {
        	// uh oh!
        } else {
    		ArrayList<Float> vertexArray = new ArrayList<Float>();
    		//ArrayList<Integer> faceArray = new ArrayList<Integer>();
    		
        	NodeList coordinateList = doc.getElementsByTagName("coordinates");
    		int numCoordinates = coordinateList.getLength();
            System.out.println(numCoordinates + " coordinates.");
            for (int a = 0; a < numCoordinates; a++) {
            	Element coordinate = (Element)coordinateList.item(a);
            	//System.out.println("x: "+coordinate.getElementsByTagName("x").item(0).getTextContent());
            	//System.out.println("y: "+coordinate.getElementsByTagName("y").item(0).getTextContent());
            	//System.out.println("z: "+coordinate.getElementsByTagName("z").item(0).getTextContent());
            	float x = Float.parseFloat(coordinate.getElementsByTagName("x").item(0).getTextContent());
            	float y = Float.parseFloat(coordinate.getElementsByTagName("y").item(0).getTextContent());
            	float z = Float.parseFloat(coordinate.getElementsByTagName("z").item(0).getTextContent());
                vertexArray.add(x);
                vertexArray.add(y);
                vertexArray.add(z);
            }

        	NodeList volumeList = doc.getElementsByTagName("volume");
            System.out.println(volumeList.getLength() + " volumes.");
            int numVolumes = volumeList.getLength();
            for (int a = 0; a < numVolumes; a++) {
            	Element volume = (Element)volumeList.item(a);
            	NodeList triangleList = volume.getElementsByTagName("triangle");

                int numTriangles = triangleList.getLength();
                System.out.println("\t"+ numTriangles + " triangles.");
                for (int b = 0; b < numTriangles; b++) {
                	Element triangle = (Element)triangleList.item(b);
                	int v1 = Integer.parseInt(triangle.getElementsByTagName("v1").item(0).getTextContent());
                	int v2 = Integer.parseInt(triangle.getElementsByTagName("v2").item(0).getTextContent());
                	int v3 = Integer.parseInt(triangle.getElementsByTagName("v3").item(0).getTextContent());
                    //System.out.println("\t\t"+v1+", "+v2+", "+v3);
                	
                	float x1 = vertexArray.get(v1*3+0);
                	float y1 = vertexArray.get(v1*3+1);
                	float z1 = vertexArray.get(v1*3+2);

                	float x2 = vertexArray.get(v2*3+0);
                	float y2 = vertexArray.get(v2*3+1);
                	float z2 = vertexArray.get(v2*3+2);
                	
                	float x3 = vertexArray.get(v3*3+0);
                	float y3 = vertexArray.get(v3*3+1);
                	float z3 = vertexArray.get(v3*3+2);
                	
					model.addVertex(x1,y1,z1);
					model.addVertex(x2,y2,z2);
					model.addVertex(x3,y3,z3);
					
					// calculate normal from triangle face
					Vector3f p1 = new Vector3f(x1,y1,z1);	
					Vector3f p2 = new Vector3f(x2,y2,z2);	
					Vector3f p3 = new Vector3f(x3,y3,z3);	
					
					p2.sub(p1);
					p3.sub(p1);
					p2.normalize();
					p3.normalize();
					p1.cross(p2, p3);
					p1.normalize();
					model.addNormal(p1.x, p1.y, p1.z);
					model.addNormal(p1.x, p1.y, p1.z);
					model.addNormal(p1.x, p1.y, p1.z);
                }
            }
            model.hasNormals=true;
        }
        
		return model;
	}

	
	@Override
	public void save(OutputStream inputStream, Model model) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
