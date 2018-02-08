package com.nestorrente.jitl.processor.sql.accessor.property;

@FunctionalInterface
public interface PropertyAccessor<T> {
	Object access(T obj, String key);
}
