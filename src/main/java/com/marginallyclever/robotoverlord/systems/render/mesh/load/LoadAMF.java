package com.marginallyclever.robotoverlord.systems.render.mesh.load;

import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.util.ArrayList;

/**
 * Load a 3D printing file (AMF).
 * See <a href="https://en.wikipedia.org/wiki/Additive_manufacturing_file_format">Wikipedia</a>.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public class LoadAMF implements MeshLoader {
	private static final Logger logger = LoggerFactory.getLogger(LoadAMF.class);
	@Override
	public String getEnglishName() {
		return "3D printing file (AMF)";
	}
	
	@Override
	public String[] getValidExtensions() {
		return new String[]{"amf"};
	}

	// much help from https://www.sculpteo.com/en/glossary/amf-definition/
	@Override
	public void load(BufferedInputStream inputStream, Mesh model) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();
        if(!doc.getDocumentElement().getNodeName().contains("amf")) {
        	throw new Exception("I can't find AMF node!");
        }

		ArrayList<Float> vertexArray = new ArrayList<>();
		//ArrayList<Integer> faceArray = new ArrayList<>();
		
    	NodeList coordinateList = doc.getElementsByTagName("coordinates");
		int numCoordinates = coordinateList.getLength();
        logger.info(numCoordinates + " coordinates.");
        for (int a = 0; a < numCoordinates; a++) {
        	Element coordinate = (Element)coordinateList.item(a);
        	//logger.info("x: "+coordinate.getElementsByTagName("x").item(0).getTextContent());
        	//logger.info("y: "+coordinate.getElementsByTagName("y").item(0).getTextContent());
        	//logger.info("z: "+coordinate.getElementsByTagName("z").item(0).getTextContent());
        	float x = Float.parseFloat(coordinate.getElementsByTagName("x").item(0).getTextContent());
        	float y = Float.parseFloat(coordinate.getElementsByTagName("y").item(0).getTextContent());
        	float z = Float.parseFloat(coordinate.getElementsByTagName("z").item(0).getTextContent());
            vertexArray.add(x);
            vertexArray.add(y);
            vertexArray.add(z);
        }

    	NodeList volumeList = doc.getElementsByTagName("volume");
        logger.info(volumeList.getLength() + " volumes.");
        int numVolumes = volumeList.getLength();
        for (int a = 0; a < numVolumes; a++) {
        	Element volume = (Element)volumeList.item(a);
        	NodeList triangleList = volume.getElementsByTagName("triangle");

            int numTriangles = triangleList.getLength();
            logger.info("\t"+ numTriangles + " triangles.");
            for (int b = 0; b < numTriangles; b++) {
            	Element triangle = (Element)triangleList.item(b);
            	int v1 = Integer.parseInt(triangle.getElementsByTagName("v1").item(0).getTextContent());
            	int v2 = Integer.parseInt(triangle.getElementsByTagName("v2").item(0).getTextContent());
            	int v3 = Integer.parseInt(triangle.getElementsByTagName("v3").item(0).getTextContent());
                //logger.info("\t\t"+v1+", "+v2+", "+v3);
            	
            	float x1 = vertexArray.get(v1*3  );
            	float y1 = vertexArray.get(v1*3+1);
            	float z1 = vertexArray.get(v1*3+2);

            	float x2 = vertexArray.get(v2*3  );
            	float y2 = vertexArray.get(v2*3+1);
            	float z2 = vertexArray.get(v2*3+2);
            	
            	float x3 = vertexArray.get(v3*3  );
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
	}

}
