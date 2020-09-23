package com.marginallyclever.robotOverlord.entity.scene.robotEntity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.marginallyclever.convenience.memento.Memento;

public class SkycamMemento implements Memento {
	public Matrix4d relative;
	public Vector3d size;
	
	public SkycamMemento() {
		relative = new Matrix4d();
		size = new Vector3d();
	}
	
	@Override
	public void save(OutputStream arg0) throws IOException {
		DataOutputStream out = new DataOutputStream(arg0);
		
		out.writeDouble(size.x);
		out.writeDouble(size.y);
		out.writeDouble(size.z);
		
		out.writeDouble(relative.m00);
		out.writeDouble(relative.m01);
		out.writeDouble(relative.m02);
		out.writeDouble(relative.m03);

		out.writeDouble(relative.m10);
		out.writeDouble(relative.m11);
		out.writeDouble(relative.m12);
		out.writeDouble(relative.m13);

		out.writeDouble(relative.m20);
		out.writeDouble(relative.m21);
		out.writeDouble(relative.m22);
		out.writeDouble(relative.m23);

		out.writeDouble(relative.m30);
		out.writeDouble(relative.m31);
		out.writeDouble(relative.m32);
		out.writeDouble(relative.m33);
	}

	@Override
	public void load(InputStream arg0) throws IOException {
		DataInputStream in = new DataInputStream(arg0);

		size.x = in.readDouble();
		size.y = in.readDouble();
		size.z = in.readDouble();
		
		relative.m00 = in.readDouble();
		relative.m01 = in.readDouble();
		relative.m02 = in.readDouble();
		relative.m03 = in.readDouble();

		relative.m10 = in.readDouble();
		relative.m11 = in.readDouble();
		relative.m12 = in.readDouble();
		relative.m13 = in.readDouble();

		relative.m20 = in.readDouble();
		relative.m21 = in.readDouble();
		relative.m22 = in.readDouble();
		relative.m23 = in.readDouble();

		relative.m30 = in.readDouble();
		relative.m31 = in.readDouble();
		relative.m32 = in.readDouble();
		relative.m33 = in.readDouble();
	}

}
