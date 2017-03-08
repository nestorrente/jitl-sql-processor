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
import com.nestorrente.jitl.module.JitlModule;
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

public class SQLModule extends JitlModule {

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionConsumer; // TODO buscar un nombre mejor (se supone que se usará para cerrar la conexión)
	private final Collection<ResultSetTransformerFactory> transformerFactories;
	private final Pattern statementParameterRegex;
	private final Function<String, String> columnNameConverter;

	SQLModule(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionConsumer, Collection<String> fileExtensions, Collection<ResultSetTransformerFactory> transformerFactories, Pattern statementParameterRegex, Function<String, String> columnNameConverter) {

		super(ImmutableList.<String>builder().addAll(fileExtensions).add("sql").build());

		this.connectionSupplier = connectionSupplier;
		this.connectionConsumer = connectionConsumer;

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

		// TODO buscar una excepción más adecuada
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

		// TODO limpiar un poco este método

		Collection<Object> queryParametersValues = new ArrayList<>();

		String postProcessedTemplate = PatternUtils.replace(renderedTemplate, this.statementParameterRegex, (match, backrefs) -> this.processParameter(backrefs[1], parameters, queryParametersValues));

		boolean returnAffectedRows = method.isAnnotationPresent(AffectedRows.class);
		boolean returnGeneratedKeys = method.isAnnotationPresent(GeneratedKeys.class);

		if(returnAffectedRows && returnGeneratedKeys) {
			// TODO cambiar por una más adecuada
			throw new RuntimeException("One method cannot return affected rows and generated keys at the same time");
		}

		Connection connection = this.connectionSupplier.get();

		try {

			PreparedStatement statement = SqlUtils.prepareStatement(connection, postProcessedTemplate, queryParametersValues, returnGeneratedKeys);

			if(returnAffectedRows) {
				// FIXME controlar que el tipo que devuelve el método es válido?
				return statement.executeUpdate();
			}

			ResultSet results;

			if(statement.execute()) {
				results = statement.getResultSet();
			} else if(returnGeneratedKeys) {
				results = statement.getGeneratedKeys();
			} else {
				return null;
			}

			TypeToken<?> expectedResultType = TypeToken.of(method.getGenericReturnType());

			ResultSetTransformer<?> transformer = this.getTransformer(expectedResultType);

			if(transformer == null) {
				// TODO cambiar por una excepción más apropiada
				throw new RuntimeException(String.format("There is no registered transformer for type %s", expectedResultType.getType().getTypeName()));
			}

			return transformer.transform(results);

		} finally {

			this.connectionConsumer.accept(connection);

		}

	}

	// TODO refactorizar, llevando estos métodos a otro lugar?

	private String processParameter(String parameterName, Map<String, Object> parameters, Collection<Object> parametersValues) {

		if(!parameters.containsKey(parameterName)) {
			throw new IllegalArgumentException(String.format("Unknown query parameter: %s", parameterName));
		}

		Object value = parameters.get(parameterName);

		if(value instanceof Collection) {

			parametersValues.addAll((Collection<?>) value);

			return StringUtils.join("?", ", ", ((Collection<?>) value).size());

		}

		if(ReflectionUtils.isArray(value)) {

			ReflectionUtils.addAllFromArray(parametersValues, value);

			return StringUtils.join("?", ", ", Array.getLength(value));

		}

		// when the value isn't neither a collection or an array, we treat it as a single value
		parametersValues.add(value);
		return "?";

	}

}
