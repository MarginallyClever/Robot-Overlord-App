package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A component that depends on other components existing in the same {@link Entity}.
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
