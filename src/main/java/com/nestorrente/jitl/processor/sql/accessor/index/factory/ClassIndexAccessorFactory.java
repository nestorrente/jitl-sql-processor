package com.nestorrente.jitl.processor.sql.accessor.index.factory;

import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.accessor.index.IndexAccessor;

public class ClassIndexAccessorFactory<T> implements IndexAccessorFactory {

	private final Class<T> type;
	private final IndexAccessor<? super T> accessor;

	public ClassIndexAccessorFactory(Class<T> type, IndexAccessor<? super T> accessor) {
		this.type = type;
		this.accessor = accessor;
	}

	@Override
	public IndexAccessor<?> get(SQLProcessor module, Class<?> type) {
		return this.type.equals(type) ? this.accessor : null;
	}

}
