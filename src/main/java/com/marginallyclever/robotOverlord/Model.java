package com.marginallyclever.robotOverlord;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;

import com.jogamp.opengl.GL2;


public class Model implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7136313382885361812L;

	private static LinkedList<Model> modelPool = new LinkedList<Model>();
	
	public final static int NUM_BUFFERS=2;  // verts, normals
	
	protected transient String name = null;
	protected transient int numTriangles;
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


	private void load() {
		File f = new File(name);
		int index = f.getName().lastIndexOf(":");
		if(index==-1) {
			loadFromFile(name);
		} else {
			index = name.lastIndexOf(":");
			loadFromZip(name.substring(0, index), name.substring(index+1,name.length()));
		}
	}
	
	protected InputStream getInputStream(String fname) throws IOException {
		InputStream s = getClass().getResourceAsStream(fname);
		if( s==null ) {
			s = new FileInputStream(new File(fname));
		}
		return s;
	}
	
	private void loadFromZip(String zipName,String fname) {
		ZipInputStream zipFile=null;
		ZipEntry entry;
		InputStreamReader isr;
		BufferedReader stream;
		try {
			// check if the file is binary or ASCII
			zipFile = new ZipInputStream(getInputStream(zipName));
			isr = new InputStreamReader(zipFile);
			
		    while((entry = zipFile.getNextEntry())!=null) {
		        if( entry.getName().equals(fname) ) {
			        stream = new BufferedReader(isr);
					CharBuffer binaryCheck = CharBuffer.allocate(5);
					stream.read(binaryCheck);
					binaryCheck.rewind();
					isBinary = !binaryCheck.toString().equalsIgnoreCase("SOLID");
					break;
		        }
		    }
		    
		    zipFile.close();
		    
		    // now load the file enough to initialize
			zipFile = new ZipInputStream(getInputStream(zipName));
			isr = new InputStreamReader(zipFile);
			
		    while((entry = zipFile.getNextEntry())!=null) {
		        if( entry.getName().equals(fname) ) {
			        if(isBinary) {
				        loadFromStreamBinary(zipFile);
			        } else {
				        stream = new BufferedReader(isr);
			        	initialize(stream);
			        }
			        break;
		        }
		    }

		    if(!isBinary) {
				zipFile = new ZipInputStream(getInputStream(zipName));
				isr = new InputStreamReader(zipFile);
				
			    while((entry = zipFile.getNextEntry())!=null) {
			        if( entry.getName().equals(fname) ) {
				        stream = new BufferedReader(isr);
				        loadFromStream(stream);
				        break;
			        }
			    }
		    }
		    
		    zipFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// much help from http://www.java-gaming.org/index.php?;topic=18710.0
	private void loadFromFile(String fname) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(getInputStream(fname),"UTF-8"));
			CharBuffer binaryCheck = CharBuffer.allocate(5);
			br.read(binaryCheck);
			br.close();
			binaryCheck.rewind();
			isBinary = !binaryCheck.toString().equalsIgnoreCase("SOLID");
		
			if(isBinary) {
				loadFromStreamBinary(getInputStream(fname));
			} else {
				br = new BufferedReader(new InputStreamReader(getInputStream(fname),"UTF-8"));
			    initialize(br);
			    br.close();
				br = new BufferedReader(new InputStreamReader(getInputStream(fname),"UTF-8"));
			    loadFromStream(br);   
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
	
	private void initialize(BufferedReader br) throws IOException {
		String line;
		int j=0;
		while( ( line = br.readLine() ) != null ) {
			if( line.length() < 16 ) continue;
			j++;
			if(j==4) {
				j=0;
				numTriangles++;
			}
		}

		int totalBufferSize = numTriangles*3*3;
		vertices = FloatBuffer.allocate(totalBufferSize);  // num_triangles * points per triangle * float per point
		normals = FloatBuffer.allocate(totalBufferSize);  // num_triangles * normal per triangle (1) * float per point		
	}
	
	private void loadFromStream(BufferedReader br) throws IOException {
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
		
	    isLoaded=true;
	}
	
	public void saveToStreamAsBinary(OutputStream os) throws IOException {
		byte[] info = new byte[80];
	    info[0]='S';
	    info[1]='O';
	    info[2]='L';
	    info[3]='I';
	    info[4]='D';
	    os.write(info);

		ByteBuffer dataBuffer = ByteBuffer.allocate(4);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    dataBuffer.putInt(numTriangles);
	    os.write(dataBuffer.array());

	    dataBuffer = ByteBuffer.allocate(50);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    int i,j=0;
	    for(i=0;i<numTriangles;++i) {
	    	dataBuffer.rewind();
	    	dataBuffer.putFloat(normals.get(i*3+0));
	    	dataBuffer.putFloat(normals.get(i*3+1));
	    	dataBuffer.putFloat(normals.get(i*3+2));
	    	
	    	dataBuffer.putFloat(normals.get(i*3+0));
	    	dataBuffer.putFloat(normals.get(i*3+1));
	    	dataBuffer.putFloat(normals.get(i*3+2));
	    	
	    	dataBuffer.putFloat(normals.get(i*3+0));
	    	dataBuffer.putFloat(normals.get(i*3+1));
	    	dataBuffer.putFloat(normals.get(i*3+2));

	    	dataBuffer.putFloat(vertices.get(j++));
	    	dataBuffer.putFloat(vertices.get(j++));
	    	dataBuffer.putFloat(vertices.get(j++));
	    	
	    	dataBuffer.putFloat(vertices.get(j++));
	    	dataBuffer.putFloat(vertices.get(j++));
	    	dataBuffer.putFloat(vertices.get(j++));
	    	
	    	dataBuffer.putFloat(vertices.get(j++));
	    	dataBuffer.putFloat(vertices.get(j++));
	    	dataBuffer.putFloat(vertices.get(j++));
	    	
	    	dataBuffer.put((byte)0);
	    	dataBuffer.put((byte)0);
	    	os.write(dataBuffer.array());
	    }
	}
	
	// see https://github.com/cpedrinaci/STL-Loader/blob/master/StlFile.java#L345
	private void loadFromStreamBinary(InputStream is) throws IOException {
		int j;

	    byte[] headerInfo=new byte[80];             // Header data
	    is.read(headerInfo);

	    byte[] arrayNumber= new byte[4];     // Holds the number of faces
	    is.read(arrayNumber);
	    ByteBuffer dataBuffer = ByteBuffer.wrap(arrayNumber);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    numTriangles = dataBuffer.getInt();

	    byte[] tempInfo = new byte[50*numTriangles];     // Each face has 50 bytes of data
        is.read(tempInfo);                         // We get the rest of the file
        dataBuffer = ByteBuffer.wrap(tempInfo);    // Now we have all the data in this ByteBuffer
        dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        
		int totalBufferSize = numTriangles*3*3;
		vertices = FloatBuffer.allocate(totalBufferSize);
		normals = FloatBuffer.allocate(totalBufferSize);		
		
		float x,y,z;
		for(j=0;j<numTriangles;++j) {
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
		
	    isLoaded=true;
	}
	
	private void createBuffers(GL2 gl2) {
		VBO = new int[NUM_BUFFERS];
		gl2.glGenBuffers(NUM_BUFFERS, VBO, 0);  // 2 = one for vertexes, one for normals
	}
	
	private void updateBuffers(GL2 gl2) {
		int totalBufferSize = numTriangles*3*3;
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
	}
	
	public void render(GL2 gl2) {
		if(isLoaded==false) {
			this.load();
			if(isLoaded) {
				createBuffers(gl2);
				updateBuffers(gl2);
			}
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
  
		gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, numTriangles*3);
		//gl2.glDrawArrays(GL2.GL_LINE_LOOP, 0, num_triangles*3);
		gl2.glDisableClientState(GL2.GL_VERTEX_ARRAY);
		gl2.glDisableClientState(GL2.GL_NORMAL_ARRAY);
	}

	
	private float lengthDifferenceSquared(float p1x,float p1y,float p1z,float p2x,float p2y,float p2z) {
		float dx = p2x-p1x;
		float dy = p2y-p1y;
		float dz = p2z-p1z;

		return lengthSquared(dx,dy,dz);
	}
	
	private float lengthSquared(float dx,float dy,float dz) {
		return dx*dx+dy*dy+dz*dz;
	}
	
	private float length(float dx,float dy,float dz) {
		return (float)Math.sqrt(lengthSquared(dx,dy,dz));
	}
	
	/**
	 * Smooth normals.  Find points within vertexEpsilon of each other, sharing normals within normalEpsilon of each other, and then 
	 * @param gl2
	 * @param vertexEpsilon
	 * @param normalEpsilon
	 */
	public void smoothNormals(GL2 gl2,float vertexEpsilon,float normalEpsilon) {
		float vertexEpsilonSquared = vertexEpsilon * vertexEpsilon;
		float normalEpsilonSquared = normalEpsilon * normalEpsilon;
		
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		boolean [] skip = new boolean[numTriangles*3];

		int numVertexes = numTriangles*3;

		int i,j;
		for(i=0;i<numVertexes;++i) {
			if(skip[i]) continue;

			// find vertices that are in the same position
			float p1x = vertices.get(i*3+0);
			float p1y = vertices.get(i*3+1);
			float p1z = vertices.get(i*3+2);

			float n1x = normals.get(i*3+0);
			float n1y = normals.get(i*3+1);
			float n1z = normals.get(i*3+2);

			indexList.clear();
			indexList.add(i);
			
			for(j=i+1;j<numVertexes;++j) {
				if(skip[j]) continue;

				float p2x = vertices.get(j*3+0);
				float p2y = vertices.get(j*3+1);
				float p2z = vertices.get(j*3+2);
				if( lengthDifferenceSquared(p1x,p1y,p1z,p2x,p2y,p2z) <= vertexEpsilonSquared ) {

					float n2x = normals.get(j*3+0);
					float n2y = normals.get(j*3+1);
					float n2z = normals.get(j*3+2);
					if( lengthDifferenceSquared(n1x,n1y,n1z,n2x,n2y,n2z) <= normalEpsilonSquared ) {
						indexList.add(j);
					}
				}
			}
			
			if(indexList.size()>1) {
				n1x=0;
				n1y=0;
				n1z=0;

				int size = indexList.size();
				int k;
				for(k=0;k<size;++k) {
					j = indexList.get(k)*3;
					n1x += normals.get(j+0);
					n1y += normals.get(j+1);
					n1z += normals.get(j+2);
				}
				float len = length(n1x,n1y,n1z);
				n1x /= len;
				n1y /= len;
				n1z /= len;

				for(k=0;k<size;++k) {
					j = indexList.get(k);
					skip[j]=true;
					j*=3;
					normals.put(j+0, n1x);
					normals.put(j+1, n1y);
					normals.put(j+2, n1z);
				}
			}
		}

		updateBuffers(gl2);
	}
	
	
	/**
	 * Translate all the vertexes by a given amount
	 * @param gl2
	 * @param dx amount to translate on X axis
	 * @param dy amount to translate on Y axis
	 * @param dz amount to translate on Z axis
	 */
	public void adjustOrigin(GL2 gl2,float dx,float dy,float dz) {
		int numVertexes = numTriangles*3;

		int i,j;
		for(i=0;i<numVertexes;++i) {
			j=i*3;

			float px = vertices.get(j+0);	vertices.put(j+0, px+dx);
			float py = vertices.get(j+1);	vertices.put(j+1, py+dy);
			float pz = vertices.get(j+2);	vertices.put(j+2, pz+dz);
		}
		
		updateBuffers(gl2);
	}
}
