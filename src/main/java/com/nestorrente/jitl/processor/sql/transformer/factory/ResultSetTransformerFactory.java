package com.nestorrente.jitl.processor.sql.transformer.factory;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.transformer.ResultSetTransformer;

public interface ResultSetTransformerFactory {

	ResultSetTransformer<?> get(SQLProcessor processor, TypeToken<?> type);

}
