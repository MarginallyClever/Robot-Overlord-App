package com.marginallyclever.robotoverlord.systems.render.mesh.load;

import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;


/**
 * Loads <a href="https://en.wikipedia.org/wiki/STL_(file_format)">STL files</a> into a Mesh.
 * @author Dan Royer
 * @since 1.6.0
 */
public class LoadSTL implements MeshLoader {
	@Override
	public String getEnglishName() {
		return "3D printing file (STL)";
	}
	
	@Override
	public String[] getValidExtensions() {
		return new String[]{"stl"};
	}
	
	// see http://www.java-gaming.org/index.php?;topic=18710.0
	@Override
	public void load(BufferedInputStream inputStream, Mesh model) throws Exception {
		if(!inputStream.markSupported()) {
			throw new IOException("BufferedInputStream mark unsupported");
		}
		inputStream.mark(2000);
		InputStreamReader br = new InputStreamReader(inputStream,"UTF-8");
		CharBuffer binaryCheck = CharBuffer.allocate(80);
		br.read(binaryCheck);
		binaryCheck.rewind();
		String test = binaryCheck.toString();
		boolean isASCII = test.toLowerCase().contains("facet");
		inputStream.reset();
		
		if(isASCII) loadASCII(inputStream,model);
		else 		loadBinary(inputStream,model);
	}

	// see https://github.com/cpedrinaci/STL-Loader/blob/master/StlFile.java#L345
	private void loadBinary(BufferedInputStream inputStream,Mesh model) throws IOException {
		int j;

	    byte[] headerInfo=new byte[80];             // Header data
	    inputStream.read(headerInfo);

	    byte[] arrayNumber= new byte[4];     // Holds the number of faces
	    inputStream.read(arrayNumber);
	    ByteBuffer dataBuffer = ByteBuffer.wrap(arrayNumber);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    int numTriangles = dataBuffer.getInt();
	    int byteCount = 50;
	    byte[] tempInfo = new byte[byteCount*numTriangles];     // Each face has 50 bytes of data
        inputStream.read(tempInfo);                         // We get the rest of the file
        dataBuffer = ByteBuffer.wrap(tempInfo);    // Now we have all the data in this ByteBuffer
        dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
        		
		float x,y,z;
		for(j=0;j<numTriangles;++j) {
			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();

			model.addNormal(x, y, z);
			model.addNormal(x, y, z);
			model.addNormal(x, y, z);

			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();
			model.addVertex(x, y, z);

			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();
			model.addVertex(x, y, z);

			x=dataBuffer.getFloat();
			y=dataBuffer.getFloat();
			z=dataBuffer.getFloat();
			model.addVertex(x, y, z);
			
			// attribute bytes
			dataBuffer.get();
			dataBuffer.get();
		}
	}
	
	private void loadASCII(BufferedInputStream inputStream,Mesh model) throws IOException {
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
				String [] c = line.split(" ");
				x=Float.parseFloat(c[0]);
				y=Float.parseFloat(c[1]);
				z=Float.parseFloat(c[2]);
				len = (float)Math.sqrt(x*x+y*y+z*z);
				x/=len;
				y/=len;
				z/=len;
				
				model.addNormal(x,y,z);
				model.addNormal(x,y,z);
				model.addNormal(x,y,z);
			} else if( line.startsWith(vertex) ) {
				line = line.substring(vertex.length()).trim();
				String [] c = line.split(" ");
				x=Float.parseFloat(c[0]);
				y=Float.parseFloat(c[1]);
				z=Float.parseFloat(c[2]);
				
				model.addVertex(x,y,z);
			} else {
				//throw new IOException("Unsupported line ("+lineCount+"): "+line);
				//logger.info("STL format reading unsupported line ("+lineCount+"): "+line);
				continue;
			}
			//++lineCount;
		}
	}
}
