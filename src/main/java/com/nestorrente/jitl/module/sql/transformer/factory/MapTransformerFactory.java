package com.nestorrente.jitl.module.sql.transformer.factory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.CellTransformer;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.RowTransformer;
import com.nestorrente.jitl.util.ReflectionUtils;

public class MapTransformerFactory<M extends Map<String, ?>> implements ResultSetTransformerFactory {

	private final Class<M> mapImplementationClass;
	private final Supplier<? extends M> mapImplementationSupplier;

	// TODO permitir varias implementaciones en una misma factory?
	public MapTransformerFactory(Class<M> mapImplementationClass, Supplier<? extends M> mapImplementationSupplier) {
		this.mapImplementationClass = mapImplementationClass;
		this.mapImplementationSupplier = mapImplementationSupplier;
	}

	@Override
	public ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type) {

		// Comprobamos si el tipo es un mapa y si es asignable desde la implementación que tiene esta factory
		if(!Map.class.isAssignableFrom(type.getRawType()) || !type.getRawType().isAssignableFrom(this.mapImplementationClass)) {
			return null;
		}

		@SuppressWarnings("unchecked")
		TypeToken<? extends Map<?, ?>> castedType = (TypeToken<? extends Map<?, ?>>) type;

		// TODO hacer de otra manera para no llamar 2 veces a getSuperclassTypeArgument?
		// Ver si ReflectionUtils.getSuperclassTypeArguments(...) vale la pena (ahora mismo está comentada)

		TypeToken<?> keyType = ReflectionUtils.getSuperclassTypeArgument(castedType, Map.class, 0);

		// Comprobamos también si las claves son de tipo String
		if(!String.class.equals(keyType.getRawType())) {
			return null;
		}

		TypeToken<?> valueType = ReflectionUtils.getSuperclassTypeArgument(castedType, Map.class, 1);

		CellTransformer<?> valueTransformer;

		try {
			valueTransformer = (CellTransformer<?>) module.getTransformer(valueType);
		} catch(ClassCastException ex) {
			// TODO buscar una excepción más adecuada
			throw new RuntimeException("Map values must be cells of the result set, not a row nor an entire result set", ex);
		}

		return new MapTransformer<>(module.getColumnNameConverter(), this.mapImplementationSupplier, valueTransformer);

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

				String key = this.columnNameConverter.apply(metadata.getColumnName(i));

				Object value = this.valueTransformer.transformCell(resultSet, i);

				castedForPut.put(key, value);

			}

			return map;

		}

	}

}
