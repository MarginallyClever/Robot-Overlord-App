package com.marginallyclever.robotoverlord;

import com.marginallyclever.ro3.mesh.AABB;

import java.util.ArrayList;

/**
 * Objects that can collide with other objects in the world.
 *
 */
@Deprecated public interface Collidable {
	/**
	 * @return a list of {@link AABB} relative to the world.
	 */
    ArrayList<AABB> getCuboidList();
}
