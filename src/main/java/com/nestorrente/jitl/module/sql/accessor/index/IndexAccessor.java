package com.nestorrente.jitl.module.sql.accessor.index;

@FunctionalInterface
public interface IndexAccessor<T> {

	Object access(T obj, int index);

}
