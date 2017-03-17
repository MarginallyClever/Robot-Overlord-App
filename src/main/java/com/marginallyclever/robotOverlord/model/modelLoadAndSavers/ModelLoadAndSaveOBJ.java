package com.marginallyclever.robotOverlord.model.modelLoadAndSavers;

import java.io.BufferedInputStream;
import java.io.OutputStream;

import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelLoadAndSave;

public class ModelLoadAndSaveOBJ implements ModelLoadAndSave {

	@Override
	public boolean canLoad(String filename) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canLoad() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Model load(BufferedInputStream inputStream) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
	public void save(OutputStream inputStream, Model model) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
