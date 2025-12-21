package com.example.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Record to have a Refreshable ConfigurationProperties wrapper generated.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface RefreshableRecord {
    /**
     * The property prefix to bind to (e.g., "app.feature").
     */
    String prefix();
}
