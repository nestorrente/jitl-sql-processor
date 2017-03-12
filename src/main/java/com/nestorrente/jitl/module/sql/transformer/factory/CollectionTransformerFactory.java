package com.nestorrente.jitl.module.sql.transformer.factory;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.function.Supplier;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.RowTransformer;
import com.nestorrente.jitl.util.ReflectionUtils;

public class CollectionTransformerFactory<C extends Collection<?>> implements ResultSetTransformerFactory {

	private final Class<C> collectionImplementationClass;
	private final Supplier<? extends C> collectionImplementationSupplier;

	// TODO allow many implementations in the same factory?
	public <T> CollectionTransformerFactory(Class<C> collectionImplementationClass, Supplier<? extends C> collectionImplementationSupplier) {
		this.collectionImplementationClass = collectionImplementationClass;
		this.collectionImplementationSupplier = collectionImplementationSupplier;
	}

	@Override
	public ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type) {

		// Comprobamos si el tipo es una colección y si es asignable desde la implementación que tiene esta factory
		if(!Collection.class.isAssignableFrom(type.getRawType()) || !type.getRawType().isAssignableFrom(this.collectionImplementationClass)) {
			return null;
		}

		@SuppressWarnings("unchecked")
		TypeToken<? extends Collection<?>> castedType = (TypeToken<? extends Collection<?>>) type;

		TypeToken<?> elementType = ReflectionUtils.getSuperclassTypeArgument(castedType, Collection.class, 0);

		RowTransformer<?> elementTransformer;

		try {
			elementTransformer = (RowTransformer<?>) module.getTransformer(elementType);
		} catch(ClassCastException ex) {
			// TODO replace with a custom exception
			throw new RuntimeException("Collection elements must be rows or cells of the result set, not an entire result set", ex);
		}

		return new CollectionTransformer<>(this.collectionImplementationSupplier, elementTransformer);

	}

	private static class CollectionTransformer<D extends Collection<?>> implements ResultSetTransformer<D> {

		private final Supplier<? extends D> collectionSupplier;
		private final RowTransformer<?> elementTransformer;

		public CollectionTransformer(Supplier<? extends D> collectionSupplier, RowTransformer<?> elementTransformer) {
			this.collectionSupplier = collectionSupplier;
			this.elementTransformer = elementTransformer;
		}

		@Override
		public D transform(ResultSet resultSet) throws Exception {

			D collection = this.collectionSupplier.get();

			@SuppressWarnings("unchecked")
			Collection<Object> castedForAdd = (Collection<Object>) collection;

			while(resultSet.next()) {
				castedForAdd.add(this.elementTransformer.transformRow(resultSet));
			}

			return collection;

		}

	}

}
