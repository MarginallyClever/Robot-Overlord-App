package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.entityManager.EntityManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A component that depends on other components existing in the same {@link com.marginallyclever.robotoverlord.Entity}.
 * The {@link EntityManager} uses this Annotation to load those other
 * {@link Component}s at the moment this Component is attached.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentDependency {
    Class<? extends Component> [] components();
}
