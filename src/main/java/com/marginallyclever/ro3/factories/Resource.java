package com.marginallyclever.ro3.factories;

/**
 * A Resource is a wrapper around something that is managed by a factory.
 * It includes the resource itself and its lifetime.
 *
 * @param <T> the type of resource being managed.
 */
public record Resource<T>(T resource, Lifetime lifetime) {}
