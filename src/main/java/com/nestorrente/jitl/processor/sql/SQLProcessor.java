package com.nestorrente.jitl.processor.sql;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.exception.TransformationException;
import com.nestorrente.jitl.exception.WrongAnnotationUseException;
import com.nestorrente.jitl.processor.Processor;
import com.nestorrente.jitl.processor.sql.accessor.index.IndexAccessor;
import com.nestorrente.jitl.processor.sql.accessor.index.factory.ArrayIndexAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.index.factory.HierarchyIndexAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.index.factory.IndexAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.property.PropertyAccessor;
import com.nestorrente.jitl.processor.sql.accessor.property.factory.HierarchyPropertyAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.property.factory.PropertyAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.property.factory.ReflectivePropertyAccessorFactory;
import com.nestorrente.jitl.processor.sql.annotation.AffectedRows;
import com.nestorrente.jitl.processor.sql.annotation.GeneratedKeys;
import com.nestorrente.jitl.processor.sql.exception.AccessException;
import com.nestorrente.jitl.processor.sql.transformer.CellTransformer;
import com.nestorrente.jitl.processor.sql.transformer.ObjectTransformer;
import com.nestorrente.jitl.processor.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.processor.sql.transformer.factory.ArrayTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.BasicTypesTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.ClassTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.CollectionTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.DateTypeTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.HierarchyTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.IterableTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.MapTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.ReflectiveTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.ResultSetTransformerFactory;
import com.nestorrente.jitl.processor.sql.util.JdbcUtils;
import com.nestorrente.jitl.util.ReflectionUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.jooq.lambda.Unchecked;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SQLProcessor extends Processor {

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionCloser;
	private final Collection<ResultSetTransformerFactory> transformerFactories;
	private final Collection<PropertyAccessorFactory> propertyAccessorFactories;
	private final Collection<IndexAccessorFactory> indexAccessorFactories;
	private final Function<String, String> columnNameConverter;

	SQLProcessor(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser, Collection<String> fileExtensions, Collection<ResultSetTransformerFactory> transformerFactories, Collection<PropertyAccessorFactory> propertyAccessorFactories, Collection<IndexAccessorFactory> indexAccessorFactories, Function<String, String> columnNameConverter) {

		// FIXME change this
		super(ImmutableList.<String>builder().addAll(fileExtensions).add("sql").build());

		this.connectionSupplier = connectionSupplier;
		this.connectionCloser = connectionCloser;

		// TODO reorganize transformers and accessors -> most used first (the reflection one must always be the last)

		// TODO añadir un transformer para que los char[] se traten como los String? Ahora mismo los char[] se traen una columna de tipo CHAR.
		this.transformerFactories = new ArrayList<>(transformerFactories);

		this.transformerFactories.add(new CollectionTransformerFactory<>(ArrayList.class, ArrayList::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(LinkedList.class, LinkedList::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(HashSet.class, HashSet::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(LinkedHashSet.class, LinkedHashSet::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(TreeSet.class, TreeSet::new));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		BiConsumer<ArrayList, Object> iterableAddMethod = ArrayList::add;
		this.transformerFactories.add(new IterableTransformerFactory<>(ArrayList.class, ArrayList::new, iterableAddMethod));

		this.transformerFactories.add(new MapTransformerFactory<>(HashMap.class, HashMap::new));
		this.transformerFactories.add(new MapTransformerFactory<>(LinkedHashMap.class, LinkedHashMap::new));
		this.transformerFactories.add(new MapTransformerFactory<>(TreeMap.class, TreeMap::new));

		this.transformerFactories.add(new ArrayTransformerFactory()); // arrays

		this.transformerFactories.add(new BasicTypesTransformerFactory()); // primitive types, its wrappers and String class

		this.transformerFactories.add(new ClassTransformerFactory<>(BigInteger.class, (CellTransformer<BigInteger>) JdbcUtils::getBigInteger));
		this.transformerFactories.add(new HierarchyTransformerFactory<>(BigDecimal.class, Number.class, (CellTransformer<BigDecimal>) ResultSet::getBigDecimal));

		// this.transformerFactories.add(new ClassTransformerFactory<>(Date.class, new DateTransformer()));
		this.transformerFactories.add(new DateTypeTransformerFactory<>(Date.class, Date::new));
		this.transformerFactories.add(new DateTypeTransformerFactory<>(java.sql.Date.class, java.sql.Date::new));
		this.transformerFactories.add(new DateTypeTransformerFactory<>(java.sql.Timestamp.class, java.sql.Timestamp::new));
		this.transformerFactories.add(new DateTypeTransformerFactory<>(java.sql.Time.class, java.sql.Time::new));

		this.transformerFactories.add(new ClassTransformerFactory<>(Object.class, new ObjectTransformer()));
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

		throw new TransformationException("No transformer found for type: " + type);

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

		throw new AccessException("No property accessor found for type: " + type);

	}

	public <T> IndexAccessor<T> getIndexAccessor(Class<T> type) {

		for(IndexAccessorFactory factory : this.indexAccessorFactories) {

			@SuppressWarnings("unchecked")
			IndexAccessor<T> accessor = (IndexAccessor<T>) factory.get(this, type);

			if(accessor != null) {
				return accessor;
			}

		}

		throw new AccessException("No property accessor found for type: " + type);

	}

	// TODO add a @ColumnName annotation for ReflectiveTransformer and remove the column name converter?
	public Function<String, String> getColumnNameConverter() {
		return this.columnNameConverter;
	}

	@Override
	public Object process(Method method, String renderedTemplate, Map<String, Object> parameters) throws Exception {

		// TODO clean and split this method to allow unit-testing

		ParsedQueryData parseData = QueryParser.parse(this, renderedTemplate, parameters);

		String postProcessedTemplate = parseData.getSqlCode();
		Collection<Object> queryParametersValues = parseData.getParametersValues();

		boolean returnAffectedRows = method.isAnnotationPresent(AffectedRows.class);
		boolean returnGeneratedKeys = method.isAnnotationPresent(GeneratedKeys.class);

		if(returnAffectedRows && returnGeneratedKeys) {
			throw new WrongAnnotationUseException("One method cannot return affected rows and generated keys at the same time");
		}

		Connection connection = this.connectionSupplier.get();

		try(PreparedStatement statement = JdbcUtils.prepareStatement(connection, postProcessedTemplate, queryParametersValues, returnGeneratedKeys)) {

			// TODO add getMoreResults() support? -> create StatementTransformer as superclass of ResultSetTransformer?
			// Be carefull. Add getMoreResults() support implies pass the PreparedStatement to the transformer,
			// and it would be better to isolate the transformer from the @GeneratedKeys annotation.
			// Maybe we can create a class which delegates all methods to the real Statement in order to abstract from all this stuff.
			// -> think about that. Would it be interesting in existing transformers?

			boolean isSelectQuery = statement.execute();

			if(returnAffectedRows) {

				if(isSelectQuery) {
					throw new WrongAnnotationUseException("Cannot return affected rows when executing a SELECT query.");
				}

				if(!ReflectionUtils.returnsInt(method)) {
					throw new WrongAnnotationUseException(String.format("Methods annotated with %s must return int or Integer types.", AffectedRows.class.getName()));
				}

				return statement.getUpdateCount();

			}

			ResultSet results;

			if(isSelectQuery) {

				if(returnGeneratedKeys) {
					throw new WrongAnnotationUseException("Cannot return generated keys when executing a SELECT query.");
				}

				results = statement.getResultSet();

			} else if(returnGeneratedKeys) {

				results = statement.getGeneratedKeys();

			} else if(ReflectionUtils.returnsVoid(method)) {

				// Nothing to return
				return null;

			} else {

				throw new TransformationException(String.format("Expected %s but no results obtained", method.getGenericReturnType()));

			}

			try {

				TypeToken<?> expectedResultType = TypeToken.of(method.getGenericReturnType());

				ResultSetTransformer<?> transformer = this.getTransformer(expectedResultType);

				if(transformer == null) {
					throw new TransformationException(String.format("There is no registered transformer for type %s", expectedResultType.getType().getTypeName()));
				}

				return transformer.transform(results);

			} finally {
				results.close();
			}

		} finally {

			// FIXME give it a way to know when an exception has been thrown -> maybe replace Consumer with BiConsumer?
			this.connectionCloser.accept(connection);

		}

	}

	/* Build methods */

	public static SQLProcessorBuilder builder(Connection connection) {
		return new SQLProcessorBuilder(() -> connection, c -> {
		});
	}

	public static SQLProcessorBuilder builder(DataSource dataSource) {
		return new SQLProcessorBuilder(Unchecked.supplier(dataSource::getConnection), Unchecked.consumer(Connection::close));
	}

	public static SQLProcessorBuilder builder(Supplier<Connection> connectionSupplier) {
		return new SQLProcessorBuilder(connectionSupplier, c -> {
		});
	}

	public static SQLProcessorBuilder builder(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		return new SQLProcessorBuilder(connectionSupplier, connectionCloser);
	}

	public static SQLProcessor defaultInstance(Connection connection) {
		return builder(connection).build();
	}

	public static SQLProcessor defaultInstance(DataSource dataSource) {
		return builder(dataSource).build();
	}

	public static SQLProcessor defaultInstance(Supplier<Connection> connectionSupplier) {
		return builder(connectionSupplier).build();
	}

	public static SQLProcessor defaultInstance(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		return builder(connectionSupplier, connectionCloser).build();
	}

}
