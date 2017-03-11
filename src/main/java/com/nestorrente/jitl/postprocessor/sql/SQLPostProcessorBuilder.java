package com.nestorrente.jitl.postprocessor.sql;

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

import com.nestorrente.jitl.postprocessor.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.postprocessor.sql.transformer.factory.ClassTransformerFactory;
import com.nestorrente.jitl.postprocessor.sql.transformer.factory.HierarchyTransformerFactory;
import com.nestorrente.jitl.postprocessor.sql.transformer.factory.ResultSetTransformerFactory;
import com.nestorrente.jitl.util.ArrayUtils;

public class SQLPostProcessorBuilder implements Builder<SQLPostProcessor> {

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionCloser;
	private final List<String> fileExtensions;
	private final List<ResultSetTransformerFactory> transformerFactories;
	private Pattern statementParameterRegex;
	private Function<String, String> columnNameConverter;

	public SQLPostProcessorBuilder(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		this.connectionSupplier = connectionSupplier;
		this.connectionCloser = connectionCloser;
		this.statementParameterRegex = Pattern.compile(":([A-Za-z_][A-Za-z_0-9]*)");
		this.fileExtensions = new ArrayList<>();
		this.transformerFactories = new ArrayList<>();
		this.columnNameConverter = Function.identity();
	}

	public SQLPostProcessorBuilder addFileExtensions(Collection<String> extensions) {
		this.fileExtensions.addAll(extensions);
		return this;
	}

	public SQLPostProcessorBuilder addFileExtensions(String... extensions) {
		ArrayUtils.addAll(this.fileExtensions, extensions);
		return this;
	}

	public SQLPostProcessorBuilder addTransformerFactory(ResultSetTransformerFactory factory) {
		this.transformerFactories.add(factory);
		return this;
	}

	public <T> SQLPostProcessorBuilder addTransformerFactory(Class<T> type, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new ClassTransformerFactory<>(type, transformer));
	}

	public <T> SQLPostProcessorBuilder addHierarchyTransformerFactory(Class<T> lowerBound, Class<? super T> upperBound, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new HierarchyTransformerFactory<>(lowerBound, upperBound, transformer));
	}

	public SQLPostProcessorBuilder setStatementParameterRegex(String regex) {
		return this.setStatementParameterRegex(Pattern.compile(regex));
	}

	public SQLPostProcessorBuilder setStatementParameterRegex(Pattern regex) {
		this.statementParameterRegex = regex;
		return this;
	}

	public SQLPostProcessorBuilder setColumnNameConverter(Function<String, String> columnNameConveter) {
		this.columnNameConverter = columnNameConveter;
		return this;
	}

	@Override
	public SQLPostProcessor build() {
		Collections.reverse(this.transformerFactories);
		Collections.reverse(this.fileExtensions);
		return new SQLPostProcessor(this.connectionSupplier, this.connectionCloser, this.fileExtensions, this.transformerFactories, this.statementParameterRegex, this.columnNameConverter);
	}

}
