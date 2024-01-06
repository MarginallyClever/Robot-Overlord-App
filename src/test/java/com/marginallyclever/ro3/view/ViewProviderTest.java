package com.marginallyclever.ro3.view;

import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This test ensures that all classes annotated with {@link View} implement {@link ViewProvider}.
 */
public class ViewProviderTest {
    private static final Logger logger = LoggerFactory.getLogger(ViewProviderTest.class);

    @Test
    public void testViewProviders() {
        int count=0;
        for (Class<?> cls : getAllClasses()) {
            count++;
            logger.info("View {}",cls.getSimpleName());
            assert !ViewProvider.class.isAssignableFrom(cls) :
                    "Class " + cls.getName() + " must implement ViewProvider";
        }
        logger.info("Found {} views.",count);
    }

    private Set<Class<?>> getAllClasses() {
        Reflections reflections = new Reflections("com.marginallyclever", Scanners.TypesAnnotated);
        return reflections.getTypesAnnotatedWith(View.class);
    }
}
