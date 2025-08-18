package com.marginallyclever.ro3.mesh.save;

import com.marginallyclever.ro3.mesh.Mesh;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * {@link SaveSTL} is a {@link MeshSaver} Save a mesh as an STL file.
 *
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
	public void save(OutputStream outputStream, Mesh model) throws IOException {
		byte[] info = new byte[80];
		for(int k=0;k<80;++k) info[k]=' ';
	    info[0]='M';
	    info[1]='C';
	    info[2]='R';
	    info[4]='D';
	    info[5]='R';
	    outputStream.write(info);

		var vertexProvider = model.getVertexProvider();
	    int numTriangles = vertexProvider.provideCount()/3;
		ByteBuffer dataBuffer = ByteBuffer.allocate(4);
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);
	    dataBuffer.putInt(numTriangles);
	    outputStream.write(dataBuffer.array());

	    //dataBuffer = ByteBuffer.allocate(74);  // (3*3 + 3*3) * 4 + 2
		dataBuffer = ByteBuffer.allocate(50);  // (1*3 + 3*3) * 4 + 2
	    dataBuffer.order(ByteOrder.LITTLE_ENDIAN);


	    for(int i=0;i<numTriangles;++i) {
	    	dataBuffer.rewind();
			// one normal per face
			Vector3d n = vertexProvider.provideNormal(i*3);
			dataBuffer.putFloat((float)n.x);
			dataBuffer.putFloat((float)n.y);
			dataBuffer.putFloat((float)n.z);

			for(int j=0;j<3;++j) {
				Point3d v = vertexProvider.provideVertex(i*3+j);
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
