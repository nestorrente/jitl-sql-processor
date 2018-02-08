package com.nestorrente.jitl.processor.sql.transformer;

import com.nestorrente.jitl.processor.sql.util.JdbcUtils;

import java.sql.ResultSet;

public class ObjectTransformer implements CellTransformer<Object> {

	@Override
	public Object transformCell(ResultSet resultSet, int columnIndex) throws Exception {
		return JdbcUtils.getObject(resultSet, columnIndex);
	}

}
