package com.marginallyclever.robotOverlord.model.modelLoadAndSavers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelLoadAndSave;

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
        	NodeList vertexList = doc.getElementsByTagName("coordinates");
            System.out.println(vertexList.getLength() + " vertexes.");

    		ArrayList<Float> vertexArray = new ArrayList<Float>();
    		
            for (int a = 0; a < vertexList.getLength(); a++) {
            	NodeList vertex = vertexList.item(a).getChildNodes();
            	vertexArray.add(Float.parseFloat(vertex.item(0).getNodeValue()));
            	vertexArray.add(Float.parseFloat(vertex.item(1).getNodeValue()));
            	vertexArray.add(Float.parseFloat(vertex.item(2).getNodeValue()));
            }

        	NodeList volumeList = doc.getElementsByTagName("volume");
            System.out.println(vertexList.getLength() + " volumes.");

    		ArrayList<Integer> faceArray = new ArrayList<Integer>();
    		
            for (int a = 0; a < volumeList.getLength(); a++) {
            	NodeList triangleList = volumeList.item(a).getChildNodes();
            	
                for (int b = 0; b < triangleList.getLength(); a++) {
                	NodeList triangle = triangleList.item(b).getChildNodes();

                	for (int c = 0; c < triangle.getLength(); c++) {
                		int t0 = Integer.parseInt(triangle.item(c).getNodeValue());
						model.addVertex(
								vertexArray.get(t0*3+0),
								vertexArray.get(t0*3+1),
								vertexArray.get(t0*3+2));
                	}
                }
            }
        }
        
		return model;
	}

	
	protected void loadASCII(BufferedInputStream inputStream,Model model) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
		
		String line;
		float x,y,z,len;
		final String facet_normal = "facet normal ";
		final String vertex = "vertex ";

		//int lineCount=0;
		
		while( ( line = br.readLine() ) != null ) {
			line = line.trim();
			if( line.startsWith(facet_normal) ) {
				line = line.substring(facet_normal.length()).trim();
				String c[] = line.split(" ");
				x=Float.parseFloat(c[0]);
				y=Float.parseFloat(c[1]);
				z=Float.parseFloat(c[2]);
				len = MathHelper.length(x,y,z);
				x/=len;
				y/=len;
				z/=len;
				
				model.addNormal(x,y,z);
				model.addNormal(x,y,z);
				model.addNormal(x,y,z);
			} else if( line.startsWith(vertex) ) {
				line = line.substring(vertex.length()).trim();
				String c[] = line.split(" ");
				x=Float.parseFloat(c[0]);
				y=Float.parseFloat(c[1]);
				z=Float.parseFloat(c[2]);
				
				model.addVertex(x,y,z);
			} else {
				//throw new IOException("Unsupported line ("+lineCount+"): "+line);
				//System.out.println("STL format reading unsupported line ("+lineCount+"): "+line);
				continue;
			}
			//++lineCount;
		}
		model.hasNormals=true;
	}
}
