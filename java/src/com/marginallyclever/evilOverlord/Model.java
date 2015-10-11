package com.marginallyclever.evilOverlord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.media.opengl.GL2;


public class Model implements Serializable {
	private static LinkedList<Model> modelPool = new LinkedList<Model>();
	
	public final static int NUM_BUFFERS=2;  // verts, normals
	
	protected transient String name;
	protected transient int num_triangles;
	protected transient FloatBuffer vertices;
	protected transient FloatBuffer normals;
	protected transient int VBO[] = null;
	protected transient boolean isLoaded = false;
	protected transient boolean isBinary = false;
	protected transient float loadScale=1.0f;

	
	private Model() {}
	private Model(String filename) {
		name = filename;
	}

	public static Model loadModel(String sourceName,float loadScale) {
		Model m = loadModel(sourceName);
		m.loadScale = loadScale;
		m.isBinary = false;
		return m;
	}

	public static Model loadModelBinary(String sourceName,float loadScale) {
		Model m = loadModel(sourceName);
		m.loadScale = loadScale;
		m.isBinary = true;
		return m;
	}
	
	public static Model loadModel(String sourceName) {
		// find the existing model in the pool
		Iterator<Model> iter = modelPool.iterator();
		while(iter.hasNext()) {
			Model m = iter.next();
			if(m.name.equals(sourceName)) {
				return m;
			}
		}
		
		Model m = new Model(sourceName);
		modelPool.add(m);
		return m;
	}
	
	
	public boolean isLoaded() {
		return isLoaded;
	}


	private void load(GL2 gl2) {
		int index=name.lastIndexOf(':');
		if(index==-1) {
			this.loadFromFile(gl2,name);
		} else {
			this.loadFromZip(gl2, name.substring(0, index), name.substring(index+1,name.length()));
		}
	}
	
	private void loadFromZip(GL2 gl2,String zipName,String fname) {
		ZipInputStream zipFile=null;
		ZipEntry entry;
		InputStreamReader isr;
		BufferedReader stream;
		try {
			zipFile = new ZipInputStream(getClass().getResourceAsStream(zipName));
			isr = new InputStreamReader(zipFile);
			
		    while((entry = zipFile.getNextEntry())!=null) {
		        if( entry.getName().equals(fname) ) {
			        if(isBinary) {
				        loadFromStreamBinary(gl2,zipFile);
			        } else {
				        stream = new BufferedReader(isr);
			        	initialize(gl2,stream);
			        }
			        break;
		        }
		    }

		    if(!isBinary) {
				zipFile = new ZipInputStream(getClass().getResourceAsStream(zipName));
				isr = new InputStreamReader(zipFile);
				
			    while((entry = zipFile.getNextEntry())!=null) {
			        if( entry.getName().equals(fname) ) {
				        stream = new BufferedReader(isr);
				        loadFromStream(gl2,stream);
				        break;
			        }
			    }
		    }
		    
		    zipFile.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	// much help from http://www.java-gaming.org/index.php?;topic=18710.0
	private void loadFromFile(GL2 gl2,String fname) {
		BufferedReader br =null;
		try {
			if(isBinary) {
				loadFromStreamBinary(gl2,getClass().getResourceAsStream(fname));
			} else {
				br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fname),"UTF-8"));
			    initialize(gl2,br);
			    br.close();
				br = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(fname),"UTF-8"));
			    loadFromStream(gl2,br);   
			}
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
	
	
	private void initialize(GL2 gl2,BufferedReader br) throws IOException {
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
	
	

	private void loadFromStream(GL2 gl2,BufferedReader br) throws IOException {
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
			line = line.trim();
			String c[] = line.split(" ");
			float x=0,y=0,z=0;
			try {
				x=Float.parseFloat(c[0]);
				y=Float.parseFloat(c[1]);
				z=Float.parseFloat(c[2]);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			if(j==0) {
				float len = (float)Math.sqrt(x*x+y*y+z*z);
				x/=len;
				y/=len;
				z/=len;
				
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
				vertices.put(x*loadScale);
				vertices.put(y*loadScale);
				vertices.put(z*loadScale);
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
	    
	    isLoaded=true;
	}
	
	// see https://github.com/cpedrinaci/STL-Loader/blob/master/StlFile.java#L345
	private void loadFromStreamBinary(GL2 gl2,InputStream is) throws IOException {
		int j;

	    ByteBuffer dataBuffer;                // For reading in the correct endian
	    byte[] Info=new byte[80];             // Header data
	    byte[] Array_number= new byte[4];     // Holds the number of faces
	    byte[] Temp_Info;
	    
	    is.read(Info);
	    is.read(Array_number);
	    dataBuffer = ByteBuffer.wrap(Array_number);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    num_triangles = dataBuffer.getInt();

        Temp_Info = new byte[50*num_triangles];     // Each face has 50 bytes of data
        is.read(Temp_Info);                         // We get the rest of the file
        dataBuffer = ByteBuffer.wrap(Temp_Info);    // Now we have all the data in this ByteBuffer
        dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);  // 2 = one for vertexes, one for normals
		int totalBufferSize = num_triangles*3*3;
		vertices = FloatBuffer.allocate(totalBufferSize);
		normals = FloatBuffer.allocate(totalBufferSize);		
		
		float x,y,z;
		for(j=0;j<num_triangles;++j) {
			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();

			normals.put(x);
			normals.put(y);
			normals.put(z);
			
			normals.put(x);
			normals.put(y);
			normals.put(z);
			
			normals.put(x);
			normals.put(y);
			normals.put(z);

			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();
			vertices.put(x*loadScale);
			vertices.put(y*loadScale);
			vertices.put(z*loadScale);

			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();
			vertices.put(x*loadScale);
			vertices.put(y*loadScale);
			vertices.put(z*loadScale);

			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();
			vertices.put(x*loadScale);
			vertices.put(y*loadScale);
			vertices.put(z*loadScale);
			
			// attribute bytes
			dataBuffer.get();
			dataBuffer.get();
		}
				
		int s=(Float.SIZE/8);  // bits per float / bits per byte = bytes per float

		// bind a buffer
		vertices.rewind();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[0]);
	    // Write out vertex buffer to the currently bound VBO.
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*s, vertices, GL2.GL_STATIC_DRAW);
	    
	    // repeat for normals
		normals.rewind();
		gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, VBO[1]);
	    gl2.glBufferData(GL2.GL_ARRAY_BUFFER, totalBufferSize*s, normals, GL2.GL_STATIC_DRAW);
	    
	    isLoaded=true;
	}
	
	
	public void render(GL2 gl2) {
		if(isLoaded==false) {
			this.load(gl2);
		}
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
