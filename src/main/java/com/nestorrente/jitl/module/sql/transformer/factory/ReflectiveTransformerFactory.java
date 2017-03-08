package com.nestorrente.jitl.module.sql.transformer.factory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.CellTransformer;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.RowTransformer;
import com.nestorrente.jitl.util.ReflectionUtils;

public class ReflectiveTransformerFactory implements ResultSetTransformerFactory {

	@Override
	public ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type) {

		if(type.equals(Object.class) || type.isArray()) {
			return null;
		}

		return new ReflectiveTransformer<>(module, type.getRawType());

	}

	private static class ReflectiveTransformer<T> implements RowTransformer<T> {

		private final SQLModule module;
		private final Class<T> type;
		private final Objenesis objenesis;

		public ReflectiveTransformer(SQLModule module, Class<T> type) {
			this.module = module;
			this.type = type;
			this.objenesis = new ObjenesisStd();
		}

		@Override
		public T transformRow(ResultSet resultSet) throws Exception {

			ResultSetMetaData metadata = resultSet.getMetaData();
			int columnCount = metadata.getColumnCount();

			T obj = this.objenesis.newInstance(this.type);

			for(int i = 1; i <= columnCount; ++i) {

				String fieldName = this.module.getColumnNameConverter().apply(metadata.getColumnName(i));

				// TODO cachear de alguna manera? Vale la pena, teniendo en cuenta que se trata de una clase concreta en un momento concreto?
				Field field = ReflectionUtils.getField(this.type, fieldName);

				CellTransformer<?> fieldTransformer = (CellTransformer<?>) this.module.getTransformer(field.getGenericType());

				Object fieldValue = fieldTransformer.transformCell(resultSet, i);

				ReflectionUtils.setField(obj, field, fieldValue);

			}

			return obj;

		}

	}

}
