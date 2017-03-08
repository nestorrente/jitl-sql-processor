package com.nestorrente.jitl.module.sql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.Builder;

import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.factory.ClassTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.HierarchyTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ResultSetTransformerFactory;
import com.nestorrente.jitl.util.ArrayUtils;

public class SQLModuleBuilder implements Builder<SQLModule> {

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionConsumer; // TODO buscar un nombre mejor (se supone que se usará para cerrar la conexión)
	private final List<String> fileExtensions;
	private final List<ResultSetTransformerFactory> transformerFactories;
	private Pattern statementParameterRegex;
	private Function<String, String> columnNameConverter;

	public SQLModuleBuilder(Connection connection) {
		this(() -> connection, c -> {});
	}

	public SQLModuleBuilder(Supplier<Connection> connectionSupplier) {
		this(connectionSupplier, c -> {});
	}

	public SQLModuleBuilder(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionConsumer) {
		this.connectionSupplier = connectionSupplier;
		this.connectionConsumer = connectionConsumer;
		this.statementParameterRegex = Pattern.compile(":([A-Za-z_][A-Za-z_0-9]*)");
		this.fileExtensions = new ArrayList<>();
		this.transformerFactories = new ArrayList<>();
		this.columnNameConverter = Function.identity(); // TODO meter un converter por defecto que no sea la función identidad? (tanto en el constructor sin argumentos como en el builder)
	}

	public SQLModuleBuilder addFileExtensions(Collection<String> extensions) {
		this.fileExtensions.addAll(extensions);
		return this;
	}

	public SQLModuleBuilder addFileExtensions(String... extensions) {
		ArrayUtils.addAll(this.fileExtensions, extensions);
		return this;
	}

	public SQLModuleBuilder addTransformerFactory(ResultSetTransformerFactory factory) {
		this.transformerFactories.add(factory);
		return this;
	}

	public <T> SQLModuleBuilder addTransformerFactory(Class<T> type, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new ClassTransformerFactory<>(type, transformer));
	}

	public <T> SQLModuleBuilder addHierarchyTransformerFactory(Class<T> lowerBound, Class<? super T> upperBound, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new HierarchyTransformerFactory<>(lowerBound, upperBound, transformer));
	}

	public SQLModuleBuilder setStatementParameterRegex(String regex) {
		return this.setStatementParameterRegex(Pattern.compile(regex));
	}

	public SQLModuleBuilder setStatementParameterRegex(Pattern regex) {
		this.statementParameterRegex = regex;
		return this;
	}

	public SQLModuleBuilder setColumnNameConverter(Function<String, String> columnNameConveter) {
		this.columnNameConverter = columnNameConveter;
		return this;
	}

	@Override
	public SQLModule build() {
		Collections.reverse(this.transformerFactories);
		Collections.reverse(this.fileExtensions);
		return new SQLModule(this.connectionSupplier, this.connectionConsumer, this.fileExtensions, this.transformerFactories, this.statementParameterRegex, this.columnNameConverter);
	}

}
