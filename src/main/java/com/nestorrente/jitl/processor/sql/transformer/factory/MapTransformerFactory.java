package com.nestorrente.jitl.processor.sql.transformer.factory;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.exception.TransformationException;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.transformer.CellTransformer;
import com.nestorrente.jitl.processor.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.processor.sql.transformer.RowTransformer;
import com.nestorrente.jitl.util.ReflectionUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class MapTransformerFactory<M extends Map<String, ?>> implements ResultSetTransformerFactory {

	private final Class<M> mapImplementationClass;
	private final Supplier<? extends M> mapImplementationSupplier;

	// TODO allow many implementations in the same factory?
	public MapTransformerFactory(Class<M> mapImplementationClass, Supplier<? extends M> mapImplementationSupplier) {
		this.mapImplementationClass = mapImplementationClass;
		this.mapImplementationSupplier = mapImplementationSupplier;
	}

	@Override
	public ResultSetTransformer<?> get(SQLProcessor processor, TypeToken<?> type) {

		// Ensure "type" is a Map and it matches the current implementation
		if(!Map.class.isAssignableFrom(type.getRawType()) || !type.getRawType().isAssignableFrom(this.mapImplementationClass)) {
			return null;
		}

		@SuppressWarnings("unchecked")
		TypeToken<? extends Map<?, ?>> castedType = (TypeToken<? extends Map<?, ?>>) type;

		TypeToken<?> keyType = ReflectionUtils.getSuperclassTypeArgument(castedType, Map.class, 0);

		// Accept only maps whose keys can be strings
		if(!keyType.getRawType().isAssignableFrom(String.class)) {
			return null;
		}

		TypeToken<?> valueType = ReflectionUtils.getSuperclassTypeArgument(castedType, Map.class, 1);

		CellTransformer<?> valueTransformer;

		try {
			valueTransformer = (CellTransformer<?>) processor.getTransformer(valueType);
		} catch(ClassCastException ex) {
			throw new TransformationException("Map values must be cells of the result set, not a row nor an entire result set", ex);
		}

		return new MapTransformer<>(processor.getColumnNameConverter(), this.mapImplementationSupplier, valueTransformer);

	}

	private static class MapTransformer<N extends Map<String, ?>> implements RowTransformer<N> {

		private final Function<String, String> columnNameConverter;
		private final Supplier<? extends N> mapSupplier;
		private final CellTransformer<?> valueTransformer;

		public MapTransformer(Function<String, String> columnNameConverter, Supplier<? extends N> mapSupplier, CellTransformer<?> valueTransformer) {
			this.columnNameConverter = columnNameConverter;
			this.mapSupplier = mapSupplier;
			this.valueTransformer = valueTransformer;
		}

		@Override
		public N transformRow(ResultSet resultSet) throws Exception {

			ResultSetMetaData metadata = resultSet.getMetaData();
			int columnCount = metadata.getColumnCount();

			N map = this.mapSupplier.get();

			@SuppressWarnings("unchecked")
			Map<String, Object> castedForPut = (Map<String, Object>) map;

			for(int i = 1; i <= columnCount; ++i) {

				String key = this.columnNameConverter.apply(metadata.getColumnLabel(i));

				Object value = this.valueTransformer.transformCell(resultSet, i);

				castedForPut.put(key, value);

			}

			return map;

		}

	}

}
