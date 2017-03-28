package com.nestorrente.jitl.module.sql.accessor.index.factory;

import java.lang.reflect.Array;

import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.accessor.index.IndexAccessor;

public class ArrayIndexAccessorFactory<T> implements IndexAccessorFactory {

	@Override
	public IndexAccessor<?> get(SQLModule module, Class<?> type) {
		return type.isArray() ? (o, i) -> Array.get(o, i) : null;
	}

}
