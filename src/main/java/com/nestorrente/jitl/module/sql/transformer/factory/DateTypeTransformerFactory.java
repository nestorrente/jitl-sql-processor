package com.nestorrente.jitl.module.sql.transformer.factory;

import java.sql.ResultSet;
import java.util.Date;
import java.util.function.Function;

import com.nestorrente.jitl.module.sql.transformer.CellTransformer;
import com.nestorrente.jitl.module.sql.util.SqlUtils;

public class DateTypeTransformerFactory<T extends Date> extends HierarchyTransformerFactory<T> {

	public DateTypeTransformerFactory(Class<T> dateImplementationClass, Function<Long, T> timestampToDateImplementationConverter) {
		super(dateImplementationClass, Date.class, new SqlDateTypeTransformer<>(dateImplementationClass, timestampToDateImplementationConverter));
	}

	private static class SqlDateTypeTransformer<T extends Date> implements CellTransformer<T> {

		private final Class<T> dateImplementationClass;
		private final Function<Long, T> timestampToDateImplementationConverter;

		public SqlDateTypeTransformer(Class<T> dateImplementationClass, Function<Long, T> timestampToDateImplementationConverter) {
			this.dateImplementationClass = dateImplementationClass;
			this.timestampToDateImplementationConverter = timestampToDateImplementationConverter;
		}

		@Override
		public T transformCell(ResultSet resultSet, int columnIndex) throws Exception {

			Object value = SqlUtils.getObject(resultSet, columnIndex);

			if(value == null) {
				return null;
			}

			if(this.dateImplementationClass.isAssignableFrom(value.getClass())) {

				@SuppressWarnings("unchecked")
				T casted = (T) value;

				return casted;

			}

			long timestamp;

			if(value instanceof Number) {

				timestamp = ((Number) value).longValue();

			} else if(value instanceof java.util.Date) {

				java.util.Date date = (java.util.Date) value;

				timestamp = date.getTime();

			} else {

				String columnTypeName = resultSet.getMetaData().getColumnTypeName(columnIndex);
				String className = this.dateImplementationClass.getName();

				throw new IllegalArgumentException(String.format("Cannot transform SQL's %s into Java's %s", columnTypeName, className));

			}

			return this.timestampToDateImplementationConverter.apply(timestamp);

		}

	}

}
