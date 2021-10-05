package com.marginallyclever.robotOverlord.shape.save;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

import com.marginallyclever.robotOverlord.shape.Mesh;

public class SaveSTL implements MeshSaver {
	@Override
	public String getEnglishName() {
		return "3D printing file (STL)";
	}
	
	@Override
	public String getValidExtensions() {
		return "stl";
	}
	
	@Override
	public void save(OutputStream outputStream, Mesh model) throws Exception {
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

}
