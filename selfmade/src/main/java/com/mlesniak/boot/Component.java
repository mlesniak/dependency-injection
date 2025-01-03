package com.mlesniak.boot;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

///
/// Marker interface to determine components of our application.
///
/// For every class marked as a component, we try to resolve all
/// dependencies in its constructor.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Component {
}
