package com.nestorrente.jitl.module.sql.transformer;

import java.sql.ResultSet;

import com.nestorrente.jitl.module.sql.util.SqlUtils;

public class ObjectTransformer implements CellTransformer<Object> {

	@Override
	public Object transformCell(ResultSet resultSet, int columnIndex) throws Exception {
		return SqlUtils.getObject(resultSet, columnIndex);
	}

}
