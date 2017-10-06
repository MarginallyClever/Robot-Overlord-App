package com.marginallyclever.robotOverlord.model;

import java.io.BufferedInputStream;
import java.io.OutputStream;

public interface ModelLoadAndSave {
	public String getEnglishName();
	public String getValidExtensions();
	
	public boolean canLoad();
	public boolean canLoad(String filename);
	public Model load(BufferedInputStream inputStream) throws Exception;

	public boolean canSave();
	public boolean canSave(String filename);
	public void save(OutputStream inputStream,Model model) throws Exception;
}
