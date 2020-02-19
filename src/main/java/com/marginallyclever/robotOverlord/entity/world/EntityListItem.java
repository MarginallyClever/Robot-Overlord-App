package com.marginallyclever.robotOverlord.entity.world;

import com.marginallyclever.robotOverlord.engine.translator.Translator;
import com.marginallyclever.robotOverlord.entity.Entity;

public class EntityListItem {
	public Entity entity;

	public EntityListItem() {
		entity=null;
	}
	
	public EntityListItem(Entity arg0) {
		entity = arg0;
	}
	
	@Override
	public String toString() {
		if(entity==null) return Translator.get("null object?!");
		
		return entity.getDisplayName();
	}
}
