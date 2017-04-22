package com.nestorrente.jitl.module.sql;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections4.CollectionUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.Jitl;
import com.nestorrente.jitl.module.Module;
import com.nestorrente.jitl.module.sql.accessor.index.IndexAccessor;
import com.nestorrente.jitl.module.sql.accessor.index.factory.ArrayIndexAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.index.factory.HierarchyIndexAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.index.factory.IndexAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.property.PropertyAccessor;
import com.nestorrente.jitl.module.sql.accessor.property.factory.HierarchyPropertyAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.property.factory.PropertyAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.property.factory.ReflectivePropertyAccessorFactory;
import com.nestorrente.jitl.module.sql.annotation.AffectedRows;
import com.nestorrente.jitl.module.sql.annotation.GeneratedKeys;
import com.nestorrente.jitl.module.sql.transformer.CellTransformer;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.factory.ArrayTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.BasicTypesTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ClassTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.CollectionTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.MapTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ReflectiveTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ResultSetTransformerFactory;
import com.nestorrente.jitl.util.ReflectionUtils;
import com.nestorrente.jitl.util.SqlUtils;

public class SQLModule extends Module {

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionCloser;
	private final Collection<ResultSetTransformerFactory> transformerFactories;
	private final Collection<PropertyAccessorFactory> propertyAccessorFactories;
	private final Collection<IndexAccessorFactory> indexAccessorFactories;
	private final Function<String, String> columnNameConverter;

	SQLModule(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser, Collection<String> fileExtensions, Collection<ResultSetTransformerFactory> transformerFactories, Collection<PropertyAccessorFactory> propertyAccessorFactories, Collection<IndexAccessorFactory> indexAccessorFactories, Function<String, String> columnNameConverter) {

		// TODO change this
		super(ImmutableList.<String>builder().addAll(fileExtensions).add("sql").build());

		this.connectionSupplier = connectionSupplier;
		this.connectionCloser = connectionCloser;

		// TODO reorganize transformers and accessors -> most used first (the reflection one must always be the last)

		this.transformerFactories = new ArrayList<>(transformerFactories);
		this.transformerFactories.add(new CollectionTransformerFactory<>(ArrayList.class, ArrayList::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(LinkedList.class, LinkedList::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(HashSet.class, HashSet::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(LinkedHashSet.class, LinkedHashSet::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(TreeSet.class, TreeSet::new));
		this.transformerFactories.add(new MapTransformerFactory<>(HashMap.class, HashMap::new));
		this.transformerFactories.add(new MapTransformerFactory<>(LinkedHashMap.class, LinkedHashMap::new));
		this.transformerFactories.add(new MapTransformerFactory<>(TreeMap.class, TreeMap::new));
		this.transformerFactories.add(new ArrayTransformerFactory()); // arrays
		this.transformerFactories.add(new BasicTypesTransformerFactory()); // primitive types, its wrappers and String class
		this.transformerFactories.add(new ClassTransformerFactory<>(Object.class, (CellTransformer<?>) (rs, i) -> SqlUtils.getObject(rs, i)));
		this.transformerFactories.add(new ReflectiveTransformerFactory()); // fallback for every class

		this.columnNameConverter = columnNameConverter;

		this.propertyAccessorFactories = new ArrayList<>(propertyAccessorFactories);
		this.propertyAccessorFactories.add(new HierarchyPropertyAccessorFactory<>(Map.class, (o, k) -> o.get(k)));
		this.propertyAccessorFactories.add(new ReflectivePropertyAccessorFactory()); // fallback for every class

		this.indexAccessorFactories = new ArrayList<>(indexAccessorFactories);
		this.indexAccessorFactories.add(new ArrayIndexAccessorFactory<>());
		this.indexAccessorFactories.add(new HierarchyIndexAccessorFactory<>(List.class, (o, i) -> o.get(i)));
		this.indexAccessorFactories.add(new HierarchyIndexAccessorFactory<>(Collection.class, (Object o, int i) -> CollectionUtils.get(o, i)));
		this.indexAccessorFactories.add(new HierarchyIndexAccessorFactory<>(Iterable.class, (Object o, int i) -> CollectionUtils.get(o, i)));
		this.indexAccessorFactories.add(new HierarchyIndexAccessorFactory<>(Iterator.class, (Object o, int i) -> CollectionUtils.get(o, i)));
		this.indexAccessorFactories.add(new HierarchyIndexAccessorFactory<>(Enumeration.class, (Object o, int i) -> CollectionUtils.get(o, i)));
		this.indexAccessorFactories.add(new HierarchyIndexAccessorFactory<>(String.class, (o, i) -> o.charAt(i)));

	}

	public <T> ResultSetTransformer<T> getTransformer(TypeToken<T> type) {

		for(ResultSetTransformerFactory factory : this.transformerFactories) {

			@SuppressWarnings("unchecked")
			ResultSetTransformer<T> transformer = (ResultSetTransformer<T>) factory.get(this, type);

			if(transformer != null) {
				return transformer;
			}

		}

		// TODO replace with a custom exception
		throw new RuntimeException("No transformer found for type: " + type);

	}

	public <T> ResultSetTransformer<T> getTransformer(Class<T> type) {
		return this.getTransformer(TypeToken.of(type));
	}

	public ResultSetTransformer<?> getTransformer(Type type) {
		return this.getTransformer(TypeToken.of(type));
	}

	public <T> ResultSetTransformer<T> getDelegateTransformer(ResultSetTransformerFactory skipFactory, TypeToken<T> type) {

		boolean skipFactoryFound = false;

		for(ResultSetTransformerFactory factory : this.transformerFactories) {

			if(!skipFactoryFound) {

				// We use == operator on purpose - we want to guarantee not the equality, but the identity
				if(factory == skipFactory) {
					skipFactoryFound = true;
				}

				continue;

			}

			@SuppressWarnings("unchecked")
			ResultSetTransformer<T> transformer = (ResultSetTransformer<T>) factory.get(this, type);

			if(transformer != null) {
				return transformer;
			}

		}

		return null;

	}

	public <T> PropertyAccessor<T> getPropertyAccessor(Class<T> type) {

		for(PropertyAccessorFactory factory : this.propertyAccessorFactories) {

			@SuppressWarnings("unchecked")
			PropertyAccessor<T> accessor = (PropertyAccessor<T>) factory.get(this, type);

			if(accessor != null) {
				return accessor;
			}

		}

		// TODO replace with a custom exception
		throw new RuntimeException("No property accessor found for type: " + type);

	}

	public <T> IndexAccessor<T> getIndexAccessor(Class<T> type) {

		for(IndexAccessorFactory factory : this.indexAccessorFactories) {

			@SuppressWarnings("unchecked")
			IndexAccessor<T> accessor = (IndexAccessor<T>) factory.get(this, type);

			if(accessor != null) {
				return accessor;
			}

		}

		// TODO replace with a custom exception
		throw new RuntimeException("No property accessor found for type: " + type);

	}

	public Function<String, String> getColumnNameConverter() {
		return this.columnNameConverter;
	}

	@Override
	public Object postProcess(Jitl jitl, Method method, String renderedTemplate, Map<String, Object> parameters) throws Exception {

		// TODO clean and split this method

		ParsedQueryData parseData = QueryParser.parse(this, renderedTemplate, parameters);

		String postProcessedTemplate = parseData.getSqlCode();
		Collection<Object> queryParametersValues = parseData.getParametersValues();

		boolean returnAffectedRows = method.isAnnotationPresent(AffectedRows.class);
		boolean returnGeneratedKeys = method.isAnnotationPresent(GeneratedKeys.class);

		if(returnAffectedRows && returnGeneratedKeys) {
			// TODO replace with a custom exception
			throw new RuntimeException("One method cannot return affected rows and generated keys at the same time");
		}

		Connection connection = this.connectionSupplier.get();

		try {

			PreparedStatement statement = SqlUtils.prepareStatement(connection, postProcessedTemplate, queryParametersValues, returnGeneratedKeys);

			// TODO add getMoreResults() support? -> create StatementTransformer as superclass of ResultSetTransformer?
			// Be carefull. Add getMoreResults() support implies pass the PreparedStatement to the transformer,
			// and it would be better to isolate the transformer from the @GeneratedKeys annotation.
			// Maybe we can create a class which delegates all methods to the real Statement in order to abstract from all this stuff.
			// -> think about that. Would it be interesting in existing transformers?

			boolean isSelectQuery = statement.execute();

			if(returnAffectedRows) {

				if(isSelectQuery) {
					// TODO replace with a custom exception
					throw new RuntimeException("Cannot return affected rows when executing a SELECT query.");
				}

				if(!ReflectionUtils.returnsInt(method)) {
					// TODO replace with a custom exception
					throw new RuntimeException(String.format("Methods annotated with %s must return int or Integer types.", AffectedRows.class.getName()));
				}

				return statement.getUpdateCount();

			}

			ResultSet results;

			if(returnGeneratedKeys) {

				if(isSelectQuery) {
					// TODO replace with a custom exception
					throw new RuntimeException("Cannot return generated keys when executing a SELECT query.");
				}

				results = statement.getGeneratedKeys();

			} else if(isSelectQuery) {

				results = statement.getResultSet();

			} else if(ReflectionUtils.returnsVoid(method)) {

				// Nothing to return
				return null;

				// TODO throw an exception instead of returning null?

			} else {

				// TODO replace with a custom exception
				throw new RuntimeException(String.format("Expected %s but no results obtained", method.getGenericReturnType()));

			}

			TypeToken<?> expectedResultType = TypeToken.of(method.getGenericReturnType());

			ResultSetTransformer<?> transformer = this.getTransformer(expectedResultType);

			if(transformer == null) {
				// TODO replace with a custom exception
				throw new RuntimeException(String.format("There is no registered transformer for type %s", expectedResultType.getType().getTypeName()));
			}

			return transformer.transform(results);

		} finally {

			// FIXME give it a way to know when an exception has been thrown -> maybe replace Consumer with BiConsumer?
			this.connectionCloser.accept(connection);

		}

	}

	/* Build methods */

	public static SQLModuleBuilder builder(Connection connection) {
		return new SQLModuleBuilder(() -> connection, c -> {});
	}

	public static SQLModuleBuilder builder(Supplier<Connection> connectionSupplier) {
		return new SQLModuleBuilder(connectionSupplier, c -> {});
	}

	public static SQLModuleBuilder builder(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		return new SQLModuleBuilder(connectionSupplier, connectionCloser);
	}

	public static SQLModule defaultInstance(Connection connection) {
		return builder(connection).build();
	}

	public static SQLModule defaultInstance(Supplier<Connection> connectionSupplier) {
		return builder(connectionSupplier).build();
	}

	public static SQLModule defaultInstance(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		return builder(connectionSupplier, connectionCloser).build();
	}

}
