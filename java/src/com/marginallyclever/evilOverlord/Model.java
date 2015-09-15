package com.marginallyclever.evilOverlord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.media.opengl.GL2;


public class Model {
	static int NUM_BUFFERS=2;  // verts, normals
	
	String name;
	int num_triangles;
	
	FloatBuffer vertices;
	FloatBuffer normals;

	int VBO[] = null;

	
	void loadFromZip(GL2 gl2,String zipName,String fname) {
		ZipFile zipFile=null;
		ZipEntry entry;
		BufferedReader stream;
		try {
		    zipFile = new ZipFile(zipName);
	
		    Enumeration<? extends ZipEntry> entries = zipFile.entries();
	
		    while(entries.hasMoreElements()){
		        entry = entries.nextElement();
		        if( entry.getName().equals(fname) ) {
			        stream = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
			        initialize(gl2,stream);
			        
			        stream = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
			        loadFromStream(gl2,stream);
			        break;
		        }
		    }
		    zipFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// much help from http://www.java-gaming.org/index.php?;topic=18710.0
	void load(GL2 gl2,String fname) {
		name=fname;

		BufferedReader br = null;
		try {
		    br = new BufferedReader(new FileReader(new File(fname)));
		    initialize(gl2,br);
		    
		    br = new BufferedReader(new FileReader(new File(fname)));
		    loadFromStream(gl2,br);   
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(br != null) br.close();
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	void initialize(GL2 gl2,BufferedReader br) throws IOException {
		String line;
		int j=0;
		while( ( line = br.readLine() ) != null ) {
			if( line.length() < 16 ) continue;
			j++;
			if(j==4) {
				j=0;
				num_triangles++;
			}
		}

		VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);  // 2 = one for vertexes, one for normals
		int totalBufferSize = num_triangles*3*3;
		vertices = FloatBuffer.allocate(totalBufferSize);  // num_triangles * points per triangle * float per point
		normals = FloatBuffer.allocate(totalBufferSize);  // num_triangles * normal per triangle (1) * float per point		
	}
	
	

	void loadFromStream(GL2 gl2,BufferedReader br) throws IOException {
		String line;
		int j;
		
		j=0;
		String facet_normal = "facet normal ";
		String vertex = "vertex ";
		while( ( line = br.readLine() ) != null ) {
			line = line.trim();
			if( line.startsWith(facet_normal) ) {
				line = line.substring(facet_normal.length());
			} else if( line.startsWith(vertex) ) {
				line = line.substring(vertex.length());
			} else {
				continue;
			}
			String c[] = line.split(" ");
			float x=Float.parseFloat(c[0]);
			float y=Float.parseFloat(c[1]);
			float z=Float.parseFloat(c[2]);					
			if(j==0) {
				normals.put(x);
				normals.put(y);
				normals.put(z);
				
				normals.put(x);
				normals.put(y);
				normals.put(z);
				
				normals.put(x);
				normals.put(y);
				normals.put(z);
			} else {
				vertices.put(x*0.1f);
				vertices.put(y*0.1f);
				vertices.put(z*0.1f);
			}
			j = (j+1)%4;
		}
		

		int s=(Float.SIZE/8);  // bits per float / bits per byte = bytes per float

		int totalBufferSize = num_triangles*3*3;
		
		// bind a buffer
		vertices.rewind();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
	    // Write out vertex buffer to the currently bound VBO.
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*s, vertices, GL2.GL_STATIC_DRAW);
	    
	    // repeat for normals
		normals.rewind();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[1]);
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*s, normals, GL2.GL_STATIC_DRAW);
	}
	
	
	void render(GL2 gl2) {
		if(VBO==null) return;
		
		gl2.glEnableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glEnableClientState(GL2.GL_NORMAL_ARRAY);

		// Bind the vertex buffer to work with
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
		gl2.glVertexPointer(3, GL2.GL_FLOAT, 0, 0);
	      
		// Bind the normal buffer to work with
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[1]);
		gl2.glNormalPointer(GL2.GL_FLOAT, 0, 0);
  
		gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, num_triangles*3);
		//gl2.glDrawArrays(GL2.GL_LINE_LOOP, 0, num_triangles*3);
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
	}
}
