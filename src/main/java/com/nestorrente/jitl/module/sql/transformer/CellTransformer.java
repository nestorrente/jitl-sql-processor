package com.nestorrente.jitl.module.sql.transformer;

import java.sql.ResultSet;

@FunctionalInterface
public interface CellTransformer<T> extends RowTransformer<T> {

	@Override
	default T transformRow(ResultSet resultSet) throws Exception {
		return this.transformCell(resultSet, 1);
	}

	/**
	 * El ResultSet ya está en la fila adecuada.
	 */
	T transformCell(ResultSet resultSet, int columnIndex) throws Exception;

}
