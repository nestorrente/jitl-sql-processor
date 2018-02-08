package com.nestorrente.jitl.processor.sql.accessor.property.factory;

import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.accessor.property.PropertyAccessor;

public class HierarchyPropertyAccessorFactory<T> implements PropertyAccessorFactory {

	private final Class<T> upperBound;
	private final PropertyAccessor<? super T> accessor;

	// TODO allow multiple upperBounds?
	public HierarchyPropertyAccessorFactory(Class<T> upperBound, PropertyAccessor<? super T> accessor) {
		this.upperBound = upperBound;
		this.accessor = accessor;
	}

	@Override
	public PropertyAccessor<?> get(SQLProcessor module, Class<?> type) {
		return this.upperBound.isAssignableFrom(type) ? this.accessor : null;
	}

}
