package com.nestorrente.jitl.processor.sql.transformer.factory;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.transformer.CellTransformer;
import com.nestorrente.jitl.processor.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.processor.sql.transformer.RowTransformer;
import com.nestorrente.jitl.util.ReflectionUtils;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class ReflectiveTransformerFactory implements ResultSetTransformerFactory {

	@Override
	public ResultSetTransformer<?> get(SQLProcessor processor, TypeToken<?> type) {

		if(Object.class.equals(type.getRawType()) || type.isArray()) {
			return null;
		}

		return new ReflectiveTransformer<>(processor, type.getRawType());

	}

	private static class ReflectiveTransformer<T> implements RowTransformer<T> {

		private final SQLProcessor module;
		private final Class<T> type;
		private final Objenesis objenesis;

		public ReflectiveTransformer(SQLProcessor module, Class<T> type) {
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

				// TODO is it worth it to make a cache? Maybe not, because this will be called with many different classes
				Field field = ReflectionUtils.getField(this.type, fieldName);

				CellTransformer<?> fieldTransformer = (CellTransformer<?>) this.module.getTransformer(field.getGenericType());

				Object fieldValue = fieldTransformer.transformCell(resultSet, i);

				ReflectionUtils.setFieldValue(obj, field, fieldValue);

			}

			return obj;

		}

	}

}
