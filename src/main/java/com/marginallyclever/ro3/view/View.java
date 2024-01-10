package com.marginallyclever.ro3.view;

import java.lang.annotation.*;

/**
 * Classes annotated with this annotation are saying that they implement the {@link ViewProvider} interface for class
 * 'of'.
 */
@Target(ElementType.TYPE) // or any other applicable target
@Retention(RetentionPolicy.RUNTIME) // or SOURCE or CLASS depending on usage
public @interface View {
    /**
     * @return the class for which this is a view of.
     */
    Class<?> of();
}