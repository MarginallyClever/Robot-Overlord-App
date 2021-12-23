package com.marginallyclever.robotOverlord.io.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.io.Save;

public class JSONSave implements Save {
	public JSONSave() {}
	
	private ObjectMapper getObjectMapper() {
		ObjectMapper om = new ObjectMapper();
		om.enable(SerializationFeature.INDENT_OUTPUT);/*
		om.setVisibility(
					om.getSerializationConfig().
					getDefaultVisibilityChecker().
					withFieldVisibility(Visibility.ANY).
					withGetterVisibility(Visibility.NONE));*/
		return om;
	}

	@Override
	public void save(String filename,Entity ent) {
		ObjectMapper om = getObjectMapper();
		try {
			om.writeValue(new File(filename), ent);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
