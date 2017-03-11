package com.nestorrente.jitl.postprocessor.sql.transformer.factory;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.postprocessor.sql.SQLPostProcessor;
import com.nestorrente.jitl.postprocessor.sql.transformer.CellTransformer;
import com.nestorrente.jitl.postprocessor.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.postprocessor.sql.transformer.RowTransformer;
import com.nestorrente.jitl.util.ReflectionUtils;

public class ReflectiveTransformerFactory implements ResultSetTransformerFactory {

	@Override
	public ResultSetTransformer<?> get(SQLPostProcessor postProcessor, TypeToken<?> type) {

		if(type.equals(Object.class) || type.isArray()) {
			return null;
		}

		return new ReflectiveTransformer<>(postProcessor, type.getRawType());

	}

	private static class ReflectiveTransformer<T> implements RowTransformer<T> {

		private final SQLPostProcessor postProcessor;
		private final Class<T> type;
		private final Objenesis objenesis;

		public ReflectiveTransformer(SQLPostProcessor postProcessor, Class<T> type) {
			this.postProcessor = postProcessor;
			this.type = type;
			this.objenesis = new ObjenesisStd();
		}

		@Override
		public T transformRow(ResultSet resultSet) throws Exception {

			ResultSetMetaData metadata = resultSet.getMetaData();
			int columnCount = metadata.getColumnCount();

			T obj = this.objenesis.newInstance(this.type);

			for(int i = 1; i <= columnCount; ++i) {

				String fieldName = this.postProcessor.getColumnNameConverter().apply(metadata.getColumnName(i));

				// TODO is it worth it to make a cache? Maybe not, because this will be called with many different classes
				Field field = ReflectionUtils.getField(this.type, fieldName);

				CellTransformer<?> fieldTransformer = (CellTransformer<?>) this.postProcessor.getTransformer(field.getGenericType());

				Object fieldValue = fieldTransformer.transformCell(resultSet, i);

				ReflectionUtils.setField(obj, field, fieldValue);

			}

			return obj;

		}

	}

}
