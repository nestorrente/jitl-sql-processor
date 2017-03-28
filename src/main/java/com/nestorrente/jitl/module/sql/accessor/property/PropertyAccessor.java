package com.nestorrente.jitl.module.sql.accessor.property;

@FunctionalInterface
public interface PropertyAccessor<T> {
	Object access(T obj, String key);
}
