package com.nestorrente.jitl.module.sql.accessor.index.factory;

import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.accessor.index.IndexAccessor;

public class ClassIndexAccessorFactory<T> implements IndexAccessorFactory {

	private final Class<T> type;
	private final IndexAccessor<? super T> accessor;

	public ClassIndexAccessorFactory(Class<T> type, IndexAccessor<? super T> accessor) {
		this.type = type;
		this.accessor = accessor;
	}

	@Override
	public IndexAccessor<?> get(SQLModule module, Class<?> type) {
		return this.type.equals(type) ? this.accessor : null;
	}

}
