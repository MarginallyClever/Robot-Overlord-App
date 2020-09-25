package com.marginallyclever.robotOverlord.entity.scene.dhRobotEntity.dhTool;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.marginallyclever.convenience.memento.Memento;

public class GripperMemento implements Memento {
	public double gripperAngle;
	
	public GripperMemento(double angle) {
		gripperAngle=angle;
	}
	
	@Override
	public void save(OutputStream arg0) throws IOException {
		DataOutputStream out = new DataOutputStream(arg0);
		out.writeDouble(gripperAngle);
	}

	@Override
	public void load(InputStream arg0) throws IOException {
		DataInputStream out = new DataInputStream(arg0);
		gripperAngle = out.readDouble();
	}

}
