package com.nestorrente.jitl.module.sql.transformer.factory;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;

public interface ResultSetTransformerFactory {

	ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type);

}
