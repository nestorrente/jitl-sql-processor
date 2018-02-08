package com.nestorrente.jitl.processor.sql.accessor.index;

@FunctionalInterface
public interface IndexAccessor<T> {

	Object access(T obj, int index);

}
