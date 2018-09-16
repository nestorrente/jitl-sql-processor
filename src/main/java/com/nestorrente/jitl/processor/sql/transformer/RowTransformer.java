package com.nestorrente.jitl.processor.sql.transformer;

import java.sql.ResultSet;

@FunctionalInterface
public interface RowTransformer<T> extends ResultSetTransformer<T> {

	@Override
	default T transform(ResultSet resultSet) throws Exception {
		return resultSet.next() ? this.transformRow(resultSet) : null;
	}

	T transformRow(ResultSet resultSet) throws Exception;

}
