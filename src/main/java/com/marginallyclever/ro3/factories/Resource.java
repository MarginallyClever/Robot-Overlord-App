package com.marginallyclever.ro3.factories;

/**
 * A Resource is a wrapper around something that is managed by a factory.
 * It includes the item itself and its lifetime.
 *
 * @param <T> the type of item being managed.
 */
public record Resource<T>(T item, Lifetime lifetime) {}
