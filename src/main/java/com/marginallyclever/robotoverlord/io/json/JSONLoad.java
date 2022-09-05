package com.marginallyclever.robotoverlord.io.json;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.Scene;
import com.marginallyclever.robotoverlord.io.Load;

public class JSONLoad implements Load {
	public JSONLoad() {}
	
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
	public Entity load(String filename) {
		ObjectMapper om = getObjectMapper();
		Entity ent = null;
		try {
			ent = (Scene)om.readValue(new File(filename), Scene.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ent;
	}
}
