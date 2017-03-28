package com.nestorrente.jitl.module.sql.accessor.property.factory;

import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.accessor.property.PropertyAccessor;

public interface PropertyAccessorFactory {

	PropertyAccessor<?> get(SQLModule module, Class<?> type);

}
