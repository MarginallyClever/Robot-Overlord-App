package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentDependency {
    Class<? extends Component> [] components();
}
