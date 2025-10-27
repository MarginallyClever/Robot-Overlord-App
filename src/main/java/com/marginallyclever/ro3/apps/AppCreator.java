package com.marginallyclever.ro3.apps;

/**
 * Functional interface for creating App instances in an {@link AppFactory}.
 */
public interface AppCreator {
    /**
     * Create an instance of the App.
     * @return the App instance
     */
    App createApp();
}
