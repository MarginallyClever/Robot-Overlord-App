package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.entity.EntityManager;

/**
 * A demo is a script that procedurally generates a scene (creates entities with components).
 *
 */
@Deprecated
public interface Demo {
	void execute(EntityManager entityManager);

	String getName();
}
