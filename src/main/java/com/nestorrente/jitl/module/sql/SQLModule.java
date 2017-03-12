package com.nestorrente.jitl.module.sql;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.Jitl;
import com.nestorrente.jitl.module.Module;
import com.nestorrente.jitl.module.sql.annotation.AffectedRows;
import com.nestorrente.jitl.module.sql.annotation.GeneratedKeys;
import com.nestorrente.jitl.module.sql.transformer.CellTransformer;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.factory.BasicTypesTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ClassTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.CollectionTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.MapTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ReflectiveTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ResultSetTransformerFactory;
import com.nestorrente.jitl.util.PatternUtils;
import com.nestorrente.jitl.util.ReflectionUtils;
import com.nestorrente.jitl.util.SqlUtils;
import com.nestorrente.jitl.util.StringUtils;

public class SQLModule extends Module {

	public static SQLModuleBuilder builder(Connection connection) {
		return new SQLModuleBuilder(() -> connection, c -> {});
	}

	public static SQLModuleBuilder builder(Supplier<Connection> connectionSupplier) {
		return new SQLModuleBuilder(connectionSupplier, c -> {});
	}

	public static SQLModuleBuilder builder(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		return new SQLModuleBuilder(connectionSupplier, connectionCloser);
	}

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionCloser;
	private final Collection<ResultSetTransformerFactory> transformerFactories;
	private final Pattern statementParameterRegex;
	private final Function<String, String> columnNameConverter;

	SQLModule(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser, Collection<String> fileExtensions, Collection<ResultSetTransformerFactory> transformerFactories, Pattern statementParameterRegex, Function<String, String> columnNameConverter) {

		super(ImmutableList.<String>builder().addAll(fileExtensions).add("sql").build());

		this.connectionSupplier = connectionSupplier;
		this.connectionCloser = connectionCloser;

		this.transformerFactories = new ArrayList<>(transformerFactories);
		this.transformerFactories.add(new CollectionTransformerFactory<>(ArrayList.class, ArrayList::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(LinkedList.class, LinkedList::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(HashSet.class, HashSet::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(LinkedHashSet.class, LinkedHashSet::new));
		this.transformerFactories.add(new CollectionTransformerFactory<>(TreeSet.class, TreeSet::new));
		this.transformerFactories.add(new MapTransformerFactory<>(HashMap.class, HashMap::new));
		this.transformerFactories.add(new MapTransformerFactory<>(LinkedHashMap.class, LinkedHashMap::new));
		this.transformerFactories.add(new MapTransformerFactory<>(TreeMap.class, TreeMap::new));
		this.transformerFactories.add(new BasicTypesTransformerFactory()); // primitive types, its wrappers and String class
		this.transformerFactories.add(new ClassTransformerFactory<>(Object.class, (CellTransformer<?>) (rs, i) -> SqlUtils.getObject(rs, i)));
		this.transformerFactories.add(new ReflectiveTransformerFactory()); // fallback for every class

		this.statementParameterRegex = statementParameterRegex;

		this.columnNameConverter = columnNameConverter;

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

	public Function<String, String> getColumnNameConverter() {
		return this.columnNameConverter;
	}

	@Override
	public Object postProcess(Jitl jitl, Method method, String renderedTemplate, Map<String, Object> parameters) throws Exception {

		// TODO clean this method

		Collection<Object> queryParametersValues = new ArrayList<>();

		String postProcessedTemplate = PatternUtils.replace(renderedTemplate, this.statementParameterRegex, (match, backrefs) -> this.processParameter(backrefs[1], parameters, queryParametersValues));

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

	// TODO refactor this, taking this methods away to another class?

	private String processParameter(String parameterName, Map<String, Object> parameters, Collection<Object> parametersValues) {

		if(!parameters.containsKey(parameterName)) {
			throw new IllegalArgumentException(String.format("Unknown query parameter: %s", parameterName));
		}

		Object value = parameters.get(parameterName);

		if(value instanceof Collection) {

			parametersValues.addAll((Collection<?>) value);

			return StringUtils.joinRepeating("?", ", ", ((Collection<?>) value).size());

		}

		if(ReflectionUtils.isArray(value)) {

			ReflectionUtils.addAllFromArray(parametersValues, value);

			return StringUtils.joinRepeating("?", ", ", Array.getLength(value));

		}

		// When the value isn't neither a collection or an array, we treat it as a single value
		parametersValues.add(value);
		return "?";

	}

}
