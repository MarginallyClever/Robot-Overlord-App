package com.marginallyclever.ro3.apps.dialogs;

import com.marginallyclever.ro3.apps.App;
import com.marginallyclever.ro3.view.View;
import com.marginallyclever.ro3.view.ViewProvider;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * {@link AppSettingsDialog} is a {@link JPanel} that displays the {@link com.marginallyclever.ro3.view.View}
 * for every {@link com.marginallyclever.ro3.apps.App}.
 */
public class AppSettingsDialog extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(AppSettingsDialog.class);

    public AppSettingsDialog(List<App> apps) {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        for (App appInstance : apps) {
            Class<? extends App> appClass = appInstance.getClass();

            Reflections reflections = new Reflections("com.marginallyclever");
            Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(View.class);

            for (Class<?> clazz : annotated) {
                View viewAnnotation = clazz.getAnnotation(View.class);
                if (!viewAnnotation.of().equals(appClass)) continue;
                try {
                    ViewProvider<App> viewProvider = (ViewProvider<App>) clazz.getConstructor().newInstance();
                    viewProvider.setViewSubject(appInstance);
                    JPanel viewPanel = (JPanel)viewProvider;
                    JPanel container = new JPanel(new BorderLayout());
                    container.add(viewPanel, BorderLayout.NORTH);
                    tabbedPane.addTab(((JPanel) viewProvider).getName(), container);
                } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                    logger.error("Error creating view for app: " + appInstance.getClass().getSimpleName(), e);
                }
            }
        }
    }

    public void run(Component parent) {
        // show message dialog is the reason the dialog cannot be resized.
        JOptionPane.showMessageDialog(parent, this, "Settings", JOptionPane.PLAIN_MESSAGE);
    }
}