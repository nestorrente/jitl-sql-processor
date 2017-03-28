package com.nestorrente.jitl.module.sql.accessor.property.factory;

import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.accessor.property.PropertyAccessor;

public class HierarchyPropertyAccessorFactory<T> implements PropertyAccessorFactory {

	private final Class<T> upperBound;
	private final PropertyAccessor<? super T> accessor;

	// TODO allow multiple upperBounds?
	public HierarchyPropertyAccessorFactory(Class<T> upperBound, PropertyAccessor<? super T> accessor) {
		this.upperBound = upperBound;
		this.accessor = accessor;
	}

	@Override
	public PropertyAccessor<?> get(SQLModule module, Class<?> type) {
		return this.upperBound.isAssignableFrom(type) ? this.accessor : null;
	}

}
