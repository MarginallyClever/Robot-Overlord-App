package com.marginallyclever.robotoverlord.systems.render.mesh.save;

import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;

import javax.vecmath.Vector3d;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Save a mesh as an STL file.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
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

	    int numTriangles = model.getNumTriangles();
		ByteBuffer dataBuffer = ByteBuffer.allocate(4);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    dataBuffer.putInt(numTriangles);
	    outputStream.write(dataBuffer.array());

	    dataBuffer = ByteBuffer.allocate(74);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);

	    for(int i=0;i<numTriangles;++i) {
	    	dataBuffer.rewind();
			for(int j=0;j<3;++j) {
				Vector3d n = model.getNormal(i*3+j);
				dataBuffer.putFloat((float)n.x);
				dataBuffer.putFloat((float)n.y);
				dataBuffer.putFloat((float)n.z);
			}
			for(int j=0;j<3;++j) {
				Vector3d v = model.getVertex(i*3+j);
				dataBuffer.putFloat((float)v.x);
				dataBuffer.putFloat((float)v.y);
				dataBuffer.putFloat((float)v.z);
			}
	    	
	    	dataBuffer.put((byte)0);
	    	dataBuffer.put((byte)0);
	    	outputStream.write(dataBuffer.array());
	    }
	}

}
