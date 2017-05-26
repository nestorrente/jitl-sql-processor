package com.nestorrente.jitl.module.sql.transformer.factory;

import java.sql.ResultSet;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.exception.TransformationException;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.RowTransformer;
import com.nestorrente.jitl.util.ReflectionUtils;

public class IterableTransformerFactory<I extends Iterable<?>> implements ResultSetTransformerFactory {

	private final Class<I> iterableImplementationClass;
	private final Supplier<? extends I> iterableImplementationSupplier;
	private final BiConsumer<? super I, ?> iterableImplementationAdder;

	// TODO allow many implementations in the same factory?
	public IterableTransformerFactory(Class<I> iterableImplementationClass, Supplier<? extends I> iterableImplementationSupplier, BiConsumer<? super I, ?> iterableImplementationAdder) {
		this.iterableImplementationClass = iterableImplementationClass;
		this.iterableImplementationSupplier = iterableImplementationSupplier;
		this.iterableImplementationAdder = iterableImplementationAdder;
	}

	@Override
	public ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type) {

		// Ensure "type" is a Iterable and it matches the current implementation
		if(!Iterable.class.isAssignableFrom(type.getRawType()) || !type.getRawType().isAssignableFrom(this.iterableImplementationClass)) {
			return null;
		}

		@SuppressWarnings("unchecked")
		TypeToken<? extends Iterable<?>> castedType = (TypeToken<? extends Iterable<?>>) type;

		TypeToken<?> elementType = ReflectionUtils.getSuperclassTypeArgument(castedType, Iterable.class, 0);

		RowTransformer<?> elementTransformer;

		try {
			elementTransformer = (RowTransformer<?>) module.getTransformer(elementType);
		} catch(ClassCastException ex) {
			throw new TransformationException("Iterable elements must be rows or cells of the result set, not an entire result set", ex);
		}

		return new IterableTransformer<>(this.iterableImplementationSupplier, elementTransformer, this.iterableImplementationAdder);

	}

	private static class IterableTransformer<J extends Iterable<?>> implements ResultSetTransformer<J> {

		private final Supplier<? extends J> iterableSupplier;
		private final RowTransformer<?> elementTransformer;
		private final BiConsumer<? super J, ?> iterableImplementationAdder;

		public IterableTransformer(Supplier<? extends J> iterableSupplier, RowTransformer<?> elementTransformer, BiConsumer<? super J, ?> iterableImplementationAdder) {
			this.iterableSupplier = iterableSupplier;
			this.elementTransformer = elementTransformer;
			this.iterableImplementationAdder = iterableImplementationAdder;
		}

		@Override
		public J transform(ResultSet resultSet) throws Exception {

			J iterable = this.iterableSupplier.get();

			@SuppressWarnings("unchecked")
			BiConsumer<Iterable<?>, Object> castedForAdd = (BiConsumer<Iterable<?>, Object>) this.iterableImplementationAdder;

			while(resultSet.next()) {
				castedForAdd.accept(iterable, this.elementTransformer.transformRow(resultSet));
			}

			return iterable;

		}

	}

}
