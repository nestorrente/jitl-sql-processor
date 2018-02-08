package com.nestorrente.jitl.processor.sql.accessor.index.factory;

import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.accessor.index.IndexAccessor;

public interface IndexAccessorFactory {

	IndexAccessor<?> get(SQLProcessor module, Class<?> type);

}
