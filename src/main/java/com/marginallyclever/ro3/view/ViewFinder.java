package com.marginallyclever.ro3.view;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds all classes annotated with {@link View} that are for the given target class.
 */
public class ViewFinder {
    public static Set<ViewProvider<?>> findViews(Class<?> targetClass) {
        Reflections reflections = new Reflections("com.marginallyclever", Scanners.TypesAnnotated);
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(View.class);

        return annotatedClasses.stream()
                .filter(cls -> cls.getAnnotation(View.class).of().equals(targetClass))
                .filter(ViewProvider.class::isAssignableFrom)
                .map(cls -> {
                    try {
                        return (ViewProvider<?>) cls.getDeclaredConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException |
                             NoSuchMethodException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());
    }
}
