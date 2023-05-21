package com.marginallyclever.robotoverlord.demos;

import com.marginallyclever.robotoverlord.entity.EntityManager;

/**
 * A demo is a script that procedurally generates a scene (creates entities with components).
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public interface Demo {
	void execute(EntityManager entityManager);

	String getName();
}
