package com.marginallyclever.robotOverlord.shape.shapeLoadAndSavers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotOverlord.shape.Mesh;
import com.marginallyclever.robotOverlord.shape.ShapeLoadAndSave;

/**
 * 
 * @author Admin
 *
 */
// see https://en.wikipedia.org/wiki/Wavefront_.obj_file
public class ShapeLoadAndSavePLY implements ShapeLoadAndSave {
	@Override
	public String getEnglishName() { return "3D scanner data (CSV)"; }
	@Override
	public String getValidExtensions() { return "csv"; }

	@Override
	public boolean canLoad(String filename) {
		boolean result = filename.toLowerCase().endsWith(".csv");
		return result;
	}

	@Override
	public boolean canLoad() {
		return true;
	}

	@Override
	public boolean load(BufferedInputStream inputStream,Mesh model) throws Exception {
		model.renderStyle = GL2.GL_POINTS;

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
		String line;
		// eat the first line that says "X,Y,Z,SIGNAL_STRENGTH"
		line = br.readLine();
		// read the vertexes
		while( ( line = br.readLine() ) != null ) {
			line = line.trim();
			String[] tokens = line.split(",");
			float x=Float.parseFloat(tokens[0]);
			float y=Float.parseFloat(tokens[1]);
			float z=Float.parseFloat(tokens[2]);
			//float strength=Float.parseFloat(tokens[3]);
			model.addVertex(x,y,z);
		}
		
		return true;
	}

	@Override
	public boolean canSave() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSave(String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void save(OutputStream inputStream, Mesh model) throws Exception {
		// TODO Auto-generated method stub
	}

}
