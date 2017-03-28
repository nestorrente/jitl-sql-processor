package com.nestorrente.jitl.module.sql.accessor.index.factory;

import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.accessor.index.IndexAccessor;

public class HierarchyIndexAccessorFactory<T> implements IndexAccessorFactory {

	private final Class<T> upperBound;
	private final IndexAccessor<? super T> accessor;

	public HierarchyIndexAccessorFactory(Class<T> upperBound, IndexAccessor<? super T> accessor) {
		this.upperBound = upperBound;
		this.accessor = accessor;
	}

	@Override
	public IndexAccessor<?> get(SQLModule module, Class<?> type) {
		return this.upperBound.isAssignableFrom(type) ? this.accessor : null;
	}

}
