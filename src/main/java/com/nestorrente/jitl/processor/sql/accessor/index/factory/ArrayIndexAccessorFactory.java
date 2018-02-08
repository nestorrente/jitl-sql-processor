package com.nestorrente.jitl.processor.sql.accessor.index.factory;

import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.accessor.index.IndexAccessor;

import java.lang.reflect.Array;

public class ArrayIndexAccessorFactory<T> implements IndexAccessorFactory {

	@Override
	public IndexAccessor<?> get(SQLProcessor module, Class<?> type) {
		return type.isArray() ? (o, i) -> Array.get(o, i) : null;
	}

}
