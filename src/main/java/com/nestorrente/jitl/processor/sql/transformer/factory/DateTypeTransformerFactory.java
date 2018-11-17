package com.nestorrente.jitl.processor.sql.transformer.factory;

import com.nestorrente.jitl.processor.sql.transformer.CellTransformer;
import com.nestorrente.jitl.processor.sql.util.JdbcUtils;
import org.jooq.lambda.Unchecked;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.OptionalLong;
import java.util.function.Function;

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

			Object value = JdbcUtils.getObject(resultSet, columnIndex);

			if(value == null) {
				return null;
			}

			if(this.dateImplementationClass.isAssignableFrom(value.getClass())) {

				@SuppressWarnings("unchecked")
				T casted = (T) value;

				return casted;

			}

			long timestamp = getTimestampFromValue(resultSet, columnIndex, value)
					.<IllegalArgumentException>orElseThrow(Unchecked.supplier(() -> {

						String columnTypeName = resultSet.getMetaData().getColumnTypeName(columnIndex);
						String className = this.dateImplementationClass.getName();

						throw new IllegalArgumentException(String.format("Cannot transform SQL's %s into Java's %s", columnTypeName, className));

					}));

			return this.timestampToDateImplementationConverter.apply(timestamp);

		}

		private OptionalLong getTimestampFromValue(ResultSet resultSet, int columnIndex, Object value) throws SQLException {

			if(value instanceof Number) {
				return OptionalLong.of(((Number) value).longValue());
			}

			if(value instanceof Date) {
				return OptionalLong.of(((Date) value).getTime());
			}

			return OptionalLong.empty();

		}

	}

}
