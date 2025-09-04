package com.marginallyclever.ro3.factories;

/**
 * LifeTime indicates how long a resource should be kept around.
 */
public enum Lifetime {
    // for the life of the project
    APPLICATION,
    // for the life of the current project within the application
    SCENE,
}
