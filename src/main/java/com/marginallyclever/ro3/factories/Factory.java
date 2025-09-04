package com.marginallyclever.ro3.factories;

public interface Factory {
    /**
     * Clears any cached resources that are not meant to persist for the lifetime of the application.
     * Typically, this would clear resources with a lifetime of SCENE, while retaining those with a lifetime of APPLICATION.
     */
    void reset();
}
