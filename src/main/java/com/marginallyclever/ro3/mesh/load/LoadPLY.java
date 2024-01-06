package com.marginallyclever.ro3.mesh.load;

import com.jogamp.opengl.GL3;
import com.marginallyclever.ro3.mesh.Mesh;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * <p>{@link LoadPLY} is a {@link MeshLoader} that loads a
 * <a href="https://en.wikipedia.org/wiki/PLY_(file_format)">PLY</a> file into a {@link Mesh}.</p>
 * @author Dan Royer
 * @since 2.5.0
 */
public class LoadPLY implements MeshLoader {
	@Override
	public String getEnglishName() {
		return "3D scanner data (CSV)";
	}
	
	@Override
	public String[] getValidExtensions() {
		return new String[]{"csv"};
	}

	@Override
	public void load(BufferedInputStream inputStream, Mesh model) throws Exception {
		model.setRenderStyle( GL3.GL_POINTS );

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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
	}
}
