package com.marginallyclever.robotoverlord;

import com.marginallyclever.robotoverlord.systems.render.mesh.AABB;

import java.util.ArrayList;

/**
 * Objects that can collide with other objects in the world.
 * @author Dan Royer
 * @since 2021-02-24
 *
 */
public interface Collidable {
	/**
	 * @return a list of {@link AABB} relative to the world.
	 */
    ArrayList<AABB> getCuboidList();
}
