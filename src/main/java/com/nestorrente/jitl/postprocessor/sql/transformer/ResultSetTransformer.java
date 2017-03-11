package com.nestorrente.jitl.postprocessor.sql.transformer;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetTransformer<T> {

	T transform(ResultSet resultSet) throws Exception;

}
