package com.nestorrente.jitl.postprocessor.sql.transformer.factory;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.postprocessor.sql.SQLPostProcessor;
import com.nestorrente.jitl.postprocessor.sql.transformer.ResultSetTransformer;

public interface ResultSetTransformerFactory {

	ResultSetTransformer<?> get(SQLPostProcessor postProcessor, TypeToken<?> type);

}
