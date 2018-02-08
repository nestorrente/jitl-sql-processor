package com.nestorrente.jitl.processor.sql.accessor.property.factory;

import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.accessor.property.PropertyAccessor;

public class ClassPropertyAccessorFactory<T> implements PropertyAccessorFactory {

	private final Class<T> type;
	private final PropertyAccessor<? super T> accessor;

	public ClassPropertyAccessorFactory(Class<T> type, PropertyAccessor<? super T> accessor) {
		this.type = type;
		this.accessor = accessor;
	}

	@Override
	public PropertyAccessor<?> get(SQLProcessor module, Class<?> type) {
		return this.type.equals(type) ? this.accessor : null;
	}

}
