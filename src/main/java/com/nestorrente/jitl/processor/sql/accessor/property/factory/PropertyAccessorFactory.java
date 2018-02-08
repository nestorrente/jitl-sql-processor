package com.nestorrente.jitl.processor.sql.accessor.property.factory;

import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.accessor.property.PropertyAccessor;

public interface PropertyAccessorFactory {

	PropertyAccessor<?> get(SQLProcessor module, Class<?> type);

}
