package com.marginallyclever.robotOverlord.shape.shapeLoadAndSavers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.vecmath.Vector3d;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.marginallyclever.robotOverlord.shape.Mesh;
import com.marginallyclever.robotOverlord.shape.ShapeLoadAndSave;

/**
 * https://en.wikipedia.org/wiki/3D_Manufacturing_Format
 * @author Dan Royer
 */
public class ShapeLoadAndSave3MF implements ShapeLoadAndSave {

	@Override
	public String getEnglishName() {
		return "3D Manufacturing Format (3MF)";
	}

	@Override
	public String getValidExtensions() {
		return "3mf";
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean canLoad(String filename) {
		boolean result = filename.toLowerCase().endsWith(".3mf");
		return result;
	}

	@Override
	public Mesh load(BufferedInputStream inputStream) throws Exception {
        Mesh model = new Mesh();
        
		BufferedInputStream stream2 = openZipAndFind3DFile(inputStream);
		Element modelNode = buildTreeAndReturnRootNode(stream2);
        double scale = getScale(modelNode);
                
        //System.out.println("finding model/resources/object...");
        Element resources = (Element)modelNode.getElementsByTagName("resources").item(0);
        NodeList objects = resources.getElementsByTagName("object");
        //System.out.println(objects.getLength() + " elements found.");
        for(int i=0;i<objects.getLength();++i) {
        	Element object = (Element)objects.item(i);
        	parseObject(object,scale,model);
        }
        //System.out.println("done.");
        
        model.buildNormals();
        
		return model;
	}
    
	private void parseObject(Element object,double scale, Mesh model) throws Exception {
    	String objectUUID = object.getAttribute("p:UUID");
    	//System.out.println("parsing object "+objectUUID);
    	
    	String objectType = object.getAttribute("type");
    	if(!objectType.contentEquals("model")) {
        	throw new Exception("Object "+objectUUID+" has unsupported model type '"+objectType+"'.");
    	}
    	
        Element mesh = (Element)object.getElementsByTagName("mesh").item(0);
    	//System.out.println("mesh found.");

        ArrayList<Vector3d> vertexes = collectMeshVertices(mesh,scale);
        
        //buildIndexedTriangles(mesh,model,vertexes);
        buildTriangles(mesh,model,vertexes);
	}

	@SuppressWarnings("unused")
	private void buildIndexedTriangles(Element mesh, Mesh model,ArrayList<Vector3d> vertexes) {
		int n = model.getNumVertices();
		for( Vector3d vA : vertexes ) {
			model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
		}
		
    	Element triangles = (Element)mesh.getElementsByTagName("triangles").item(0);
    	NodeList allTriangles = triangles.getElementsByTagName("triangle");
    	//System.out.println(allTriangles.getLength() + " indexed triangles found.");
    	for(int t=0;t<allTriangles.getLength();++t) {
    		Element t1 = (Element)allTriangles.item(t);
    		model.addIndex(n+Integer.valueOf(t1.getAttribute("v1")));
    		model.addIndex(n+Integer.valueOf(t1.getAttribute("v2")));
    		model.addIndex(n+Integer.valueOf(t1.getAttribute("v3")));
    	}
	}

	@SuppressWarnings("unused")
	private void buildTriangles(Element mesh, Mesh model,ArrayList<Vector3d> vertexes) {
		Vector3d vA;
		
    	Element triangles = (Element)mesh.getElementsByTagName("triangles").item(0);
    	NodeList allTriangles = triangles.getElementsByTagName("triangle");
    	//System.out.println(allTriangles.getLength() + " triangles found.");
    	for(int t=0;t<allTriangles.getLength();++t) {
    		Element t1 = (Element)allTriangles.item(t);
    		int v1 = Integer.valueOf(t1.getAttribute("v1"));
    		int v2 = Integer.valueOf(t1.getAttribute("v2"));
    		int v3 = Integer.valueOf(t1.getAttribute("v3"));
    		vA = vertexes.get(v1);	model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
    		vA = vertexes.get(v2);	model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
    		vA = vertexes.get(v3);	model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
    	}
	}
	
	private ArrayList<Vector3d> collectMeshVertices(Element mesh, double scale) {
        ArrayList<Vector3d> collectedVertexes = new ArrayList<Vector3d>();
    	Element vertices = (Element)mesh.getElementsByTagName("vertices").item(0);
    	NodeList allVertices = vertices.getElementsByTagName("vertex");
    	//System.out.println(allVertices.getLength() + " vertices found.");
    	for(int v=0;v<allVertices.getLength();++v) {
    		Element v1 = (Element)allVertices.item(v);
    		double x = scale * Double.valueOf(v1.getAttribute("x"));
    		double y = scale * Double.valueOf(v1.getAttribute("y"));
    		double z = scale * Double.valueOf(v1.getAttribute("z"));
    		collectedVertexes.add(new Vector3d(x,y,z));
    	}

		return collectedVertexes;
	}

	private Element buildTreeAndReturnRootNode(BufferedInputStream stream2) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// process XML securely, avoid attacks like XML External Entities (XXE)
		dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(stream2);
		
		// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();

        Element modelNode = (Element)doc.getDocumentElement();
        
		return modelNode;
	}

	private double getScale(Element modelNode) {
		//System.out.println("searching for scale...");
        String units = modelNode.getAttribute("units");
        // 3MF format description says "Valid values are micron, millimeter, centimeter, inch, foot, and meter."
        double scale=1;
        switch(units) {
        case "micron": scale=0.001;  break;
        case "millimeter": scale = 1;  break;
        case "centimeter": scale = 10;  break;
        case "inch": scale = 25.4;  break;
        case "foot": scale = 304.8;  break;
        case "meter": scale = 1000;  break;
        } 
        scale *= 0.1;
        //System.out.println("scale is now x"+scale);
		return scale;
	}

	private BufferedInputStream openZipAndFind3DFile(BufferedInputStream inputStream) throws IOException {
		ZipInputStream zipFile = new ZipInputStream(inputStream);
		ZipEntry entry;
		
		while((entry = zipFile.getNextEntry())!=null) {
	        if( entry.getName().toLowerCase().endsWith(".model") ) {
	        	File f = readZipIntoTempFile(zipFile,entry,"3dmodel", "model");
                return new BufferedInputStream(new FileInputStream(f));
	        }
		}
		
		return null;
	}

	private File readZipIntoTempFile(ZipInputStream zipFile,ZipEntry entry, String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix,suffix);
        f.setReadable(true);
        f.setWritable(true);
        f.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(f);
		byte[] buffer = new byte[2048];
		int len;
        while ((len = zipFile.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();

		return f;
	}

	@Override
	public boolean canSave() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSave(String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void save(OutputStream outputStream, Mesh model) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
