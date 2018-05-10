package com.marginallyclever.robotOverlord.world;

import com.marginallyclever.robotOverlord.Entity;
import com.marginallyclever.robotOverlord.Translator;

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
