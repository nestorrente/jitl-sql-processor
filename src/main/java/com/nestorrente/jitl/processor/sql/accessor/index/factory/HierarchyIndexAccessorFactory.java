package com.nestorrente.jitl.processor.sql.accessor.index.factory;

import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.accessor.index.IndexAccessor;

public class HierarchyIndexAccessorFactory<T> implements IndexAccessorFactory {

	private final Class<T> upperBound;
	private final IndexAccessor<? super T> accessor;

	public HierarchyIndexAccessorFactory(Class<T> upperBound, IndexAccessor<? super T> accessor) {
		this.upperBound = upperBound;
		this.accessor = accessor;
	}

	@Override
	public IndexAccessor<?> get(SQLProcessor module, Class<?> type) {
		return this.upperBound.isAssignableFrom(type) ? this.accessor : null;
	}

}
