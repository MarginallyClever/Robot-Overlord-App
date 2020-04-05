package com.marginallyclever.robotOverlord.entity.scene.modelEntity.modelLoadAndSavers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.util.Iterator;

import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.Model;
import com.marginallyclever.robotOverlord.entity.scene.modelEntity.ModelLoadAndSave;

public class ModelLoadAndSaveSTL implements ModelLoadAndSave {
	@Override
	public String getEnglishName() { return "3D printing file (STL)"; }
	@Override
	public String getValidExtensions() { return "stl"; }
	@Override
	public boolean canLoad() {	return true;	}
	@Override
	public boolean canSave() {	return false;	}

	@Override
	public boolean canLoad(String filename) {
		boolean result = filename.toLowerCase().endsWith(".stl");
		//System.out.println("ModelLoadAndSaveSTL.canLoad("+filename+")="+result);
		return result;
	}

	@Override
	public boolean canSave(String filename) {
		return false;
	}

	// much help from http://www.java-gaming.org/index.php?;topic=18710.0
	@Override
	public Model load(BufferedInputStream inputStream) throws Exception {
		Model model = new Model();
		
		InputStreamReader br = null;
		try {
			if(!inputStream.markSupported()) throw new IOException("BufferedInputStream mark unsupported");
			inputStream.mark(2000);
			br = new InputStreamReader(inputStream,"UTF-8");
			CharBuffer binaryCheck = CharBuffer.allocate(80);
			br.read(binaryCheck);
			binaryCheck.rewind();
			String test = binaryCheck.toString();
			boolean isASCII = test.toLowerCase().contains("facet");
			inputStream.reset();
			
			if(!isASCII) {
				loadBinary(inputStream,model);
			} else {
			    loadASCII(inputStream,model);   
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		return model;
	}

	@Override
	public void save(OutputStream outputStream, Model model) throws Exception {
		byte[] info = new byte[80];
		for(int k=0;k<80;++k) info[k]=' ';
	    info[0]='M';
	    info[1]='C';
	    info[2]='R';
	    info[4]='D';
	    info[5]='R';
	    outputStream.write(info);

	    int numTriangles = model.vertexArray.size()/3;
		ByteBuffer dataBuffer = ByteBuffer.allocate(4);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    dataBuffer.putInt(numTriangles);
	    outputStream.write(dataBuffer.array());

	    dataBuffer = ByteBuffer.allocate(74);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    
	    Iterator<Float> vi = model.vertexArray.iterator();
	    Iterator<Float> ni = model.normalArray.iterator();
	    
	    int i;
	    for(i=0;i<numTriangles;++i) {
	    	dataBuffer.rewind();
	    	dataBuffer.putFloat(ni.next().floatValue());
	    	dataBuffer.putFloat(ni.next().floatValue());
	    	dataBuffer.putFloat(ni.next().floatValue());

	    	dataBuffer.putFloat(ni.next().floatValue());
	    	dataBuffer.putFloat(ni.next().floatValue());
	    	dataBuffer.putFloat(ni.next().floatValue());

	    	dataBuffer.putFloat(ni.next().floatValue());
	    	dataBuffer.putFloat(ni.next().floatValue());
	    	dataBuffer.putFloat(ni.next().floatValue());

	    	dataBuffer.putFloat(vi.next().floatValue());
	    	dataBuffer.putFloat(vi.next().floatValue());
	    	dataBuffer.putFloat(vi.next().floatValue());

	    	dataBuffer.putFloat(vi.next().floatValue());
	    	dataBuffer.putFloat(vi.next().floatValue());
	    	dataBuffer.putFloat(vi.next().floatValue());

	    	dataBuffer.putFloat(vi.next().floatValue());
	    	dataBuffer.putFloat(vi.next().floatValue());
	    	dataBuffer.putFloat(vi.next().floatValue());
	    	
	    	dataBuffer.put((byte)0);
	    	dataBuffer.put((byte)0);
	    	outputStream.write(dataBuffer.array());
	    }
	}


	// see https://github.com/cpedrinaci/STL-Loader/blob/master/StlFile.java#L345
	protected void loadBinary(BufferedInputStream inputStream,Model model) throws IOException {
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
		model.hasNormals=true;
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
				len = (float)MathHelper.length((double)x,(double)y,(double)z);
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
