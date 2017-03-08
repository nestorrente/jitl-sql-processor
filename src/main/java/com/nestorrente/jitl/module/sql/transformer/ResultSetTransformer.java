package com.nestorrente.jitl.module.sql.transformer;

import java.sql.ResultSet;

@FunctionalInterface
public interface ResultSetTransformer<T> {

	T transform(ResultSet resultSet) throws Exception;

}
