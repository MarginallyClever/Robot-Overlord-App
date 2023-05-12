package com.marginallyclever.robotoverlord.systems.render.mesh.load;

import com.marginallyclever.convenience.ColorRGB;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.vecmath.Vector3d;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * https://en.wikipedia.org/wiki/3D_Manufacturing_Format
 * @author Dan Royer
 */
public class Load3MF implements MeshLoader {
	private class ColorGroup {
		public int id;
		public ArrayList<ColorRGB> colors = new ArrayList<ColorRGB>();
	};
	
	ArrayList<ColorGroup> colorGroups = new ArrayList<ColorGroup>();
	
	@Override
	public String getEnglishName() {
		return "3D Manufacturing Format (3MF)";
	}

	@Override
	public String[] getValidExtensions() {
		return new String[]{"3mf"};
	}

	@Override
	public void load(BufferedInputStream inputStream, Mesh model) throws Exception {
		BufferedInputStream stream2 = openZipAndFind3DFile(inputStream);
		Element modelNode = buildTreeAndReturnRootNode(stream2);
        double scale = getScale(modelNode);

        parseMaterials(modelNode);
        parseAllObjects(model,modelNode,scale);
	}
    
	private void parseMaterials(Element modelNode) {
		colorGroups.clear();
		
        Element resources = (Element)modelNode.getElementsByTagName("resources").item(0);
        NodeList colorGroupNodes = resources.getElementsByTagName("m:colorgroup");
        for(int i=0;i<colorGroupNodes.getLength();++i) {
        	ColorGroup g = loadColorGroup((Element)colorGroupNodes.item(i));
        	colorGroups.add(g);
        }
	}

	private ColorGroup loadColorGroup(Element group) {
    	ColorGroup g = new ColorGroup();
    	g.id = Integer.valueOf(group.getAttribute("id"));
        NodeList colorNodes = group.getElementsByTagName("m:color");
        for(int i=0;i<colorNodes.getLength();++i) {
        	Element cn = (Element)colorNodes.item(i);
        	String hex = cn.getAttribute("color");
        	ColorRGB c = ColorRGB.parse(hex);
        	g.colors.add(c);
        }
    	
		return g;
	}

	private void parseAllObjects(Mesh model,Element modelNode, double scale) throws Exception {
        //logger.info("finding model/resources/object...");
        Element resources = (Element)modelNode.getElementsByTagName("resources").item(0);
        NodeList objects = resources.getElementsByTagName("object");
        //logger.info(objects.getLength() + " elements found.");
        for(int i=0;i<objects.getLength();++i) {
        	Element object = (Element)objects.item(i);
        	parseObject(object,scale,model);
        }
        //logger.info("done.");
	}

	private void parseObject(Element object,double scale, Mesh model) throws Exception {
    	String objectUUID = object.getAttribute("p:UUID");
    	//logger.info("parsing object "+objectUUID);
    	
    	ColorRGB defaultColor = parseObjectColor(object);
    	String objectType = object.getAttribute("type");
    	if(!objectType.contentEquals("model")) {
        	throw new Exception("Object "+objectUUID+" has unsupported model type '"+objectType+"'.");
    	}
    	
        Element mesh = (Element)object.getElementsByTagName("mesh").item(0);
    	//logger.info("mesh found.");

        ArrayList<Vector3d> vertexes = collectMeshVertices(mesh,scale);
        
        //buildIndexedTriangles(mesh,model,vertexes);
        buildTriangles(mesh,model,vertexes,defaultColor);
	}

	private ColorRGB parseObjectColor(Element object) throws Exception {
		int pid = Integer.valueOf(object.getAttribute("pid"));
		int pindex = Integer.valueOf(object.getAttribute("pindex"));
		return getColor(pid,pindex);
	}

	private ColorRGB getColor(int pid, int pindex) {
		for( ColorGroup g : colorGroups ) {
			if(g.id == pid) return g.colors.get(pindex);
		}
		
		return new ColorRGB(255,255,255);  // white
	}
	
	@SuppressWarnings("unused")
	private void buildIndexedTriangles(Element mesh, Mesh model,ArrayList<Vector3d> vertexes) {
		int n = model.getNumVertices();
		for( Vector3d vA : vertexes ) {
			model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
		}
		
    	Element triangles = (Element)mesh.getElementsByTagName("triangles").item(0);
    	NodeList allTriangles = triangles.getElementsByTagName("triangle");
    	//logger.info(allTriangles.getLength() + " indexed triangles found.");
    	for(int t=0;t<allTriangles.getLength();++t) {
    		Element t1 = (Element)allTriangles.item(t);
    		model.addIndex(n+Integer.valueOf(t1.getAttribute("v1")));
    		model.addIndex(n+Integer.valueOf(t1.getAttribute("v2")));
    		model.addIndex(n+Integer.valueOf(t1.getAttribute("v3")));
    	}
	}

	private void buildTriangles(Element mesh, Mesh model,ArrayList<Vector3d> vertexes,ColorRGB defaultColor) {
		Vector3d vA;
		
    	Element triangles = (Element)mesh.getElementsByTagName("triangles").item(0);
    	NodeList allTriangles = triangles.getElementsByTagName("triangle");
    	//logger.info(allTriangles.getLength() + " triangles found.");
    	for(int t=0;t<allTriangles.getLength();++t) {
    		Element t1 = (Element)allTriangles.item(t);
    		int v1 = Integer.valueOf(t1.getAttribute("v1"));
    		int v2 = Integer.valueOf(t1.getAttribute("v2"));
    		int v3 = Integer.valueOf(t1.getAttribute("v3"));
    		vA = vertexes.get(v1);	model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
    		vA = vertexes.get(v2);	model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
    		vA = vertexes.get(v3);	model.addVertex((float)vA.x,(float)vA.y,(float)vA.z);
    		
    		ColorRGB myColor;
    		if( t1.hasAttribute("pid") ) {
    			int pid = Integer.valueOf(t1.getAttribute("pid"));
    			int p1 = Integer.valueOf("p1");
    			myColor = getColor(pid,p1);
    		} else myColor = defaultColor;
    		
    		float r = (float)myColor.red/255.0f;
    		float g = (float)myColor.green/255.0f;
    		float b = (float)myColor.blue/255.0f;
    		model.addColor(r,g,b,1);
    		model.addColor(r,g,b,1);
    		model.addColor(r,g,b,1);
    	}
	}
	
	private ArrayList<Vector3d> collectMeshVertices(Element mesh, double scale) {
        ArrayList<Vector3d> collectedVertexes = new ArrayList<Vector3d>();
    	Element vertices = (Element)mesh.getElementsByTagName("vertices").item(0);
    	NodeList allVertices = vertices.getElementsByTagName("vertex");
    	//logger.info(allVertices.getLength() + " vertices found.");
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
		//logger.info("searching for scale...");
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
        //logger.info("scale is now x"+scale);
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
}
