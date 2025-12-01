package com.marginallyclever.ro3.apps;

import org.junit.jupiter.api.Test;

public class TestAppFactory {
    /**
     * Create an AppFactory, register the Apps in com.marginallyclever.ro3.apps, list them to System.out, and quit.
     */
    @Test
    public void createAppFactory() {
        AppFactory.registerApps("com.marginallyclever.ro3.apps");
        assert AppFactory.getRegisteredAppsCount() > 0 : "No apps registered";
        AppFactory.listApps(System.out);
    }
}
