package com.nestorrente.jitl.module.sql.accessor.index.factory;

import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.accessor.index.IndexAccessor;

public interface IndexAccessorFactory {

	IndexAccessor<?> get(SQLModule module, Class<?> type);

}
