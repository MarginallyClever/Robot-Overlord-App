package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.EntityManager;

public interface Demo {
	void execute(EntityManager entityManager);

	String getName();
}
