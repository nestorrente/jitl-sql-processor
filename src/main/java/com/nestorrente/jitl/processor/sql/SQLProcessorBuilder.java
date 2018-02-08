package com.nestorrente.jitl.processor.sql;

import com.nestorrente.jitl.processor.sql.accessor.index.IndexAccessor;
import com.nestorrente.jitl.processor.sql.accessor.index.factory.ClassIndexAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.index.factory.HierarchyIndexAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.index.factory.IndexAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.property.PropertyAccessor;
import com.nestorrente.jitl.processor.sql.accessor.property.factory.ClassPropertyAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.property.factory.HierarchyPropertyAccessorFactory;
import com.nestorrente.jitl.processor.sql.accessor.property.factory.PropertyAccessorFactory;
import com.nestorrente.jitl.processor.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.processor.sql.transformer.factory.ClassTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.HierarchyTransformerFactory;
import com.nestorrente.jitl.processor.sql.transformer.factory.ResultSetTransformerFactory;
import com.nestorrente.jitl.util.ArrayUtils;
import org.apache.commons.lang3.builder.Builder;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class SQLProcessorBuilder implements Builder<SQLProcessor> {

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionCloser;
	private final List<String> fileExtensions;
	private final List<ResultSetTransformerFactory> transformerFactories;
	private final List<PropertyAccessorFactory> propertyAccessorFactories;
	private final List<IndexAccessorFactory> indexAccessorFactories;
	private Function<String, String> columnNameConverter;

	public SQLProcessorBuilder(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		this.connectionSupplier = connectionSupplier;
		this.connectionCloser = connectionCloser;
		this.fileExtensions = new ArrayList<>();
		this.transformerFactories = new ArrayList<>();
		this.propertyAccessorFactories = new ArrayList<>();
		this.indexAccessorFactories = new ArrayList<>();
		this.columnNameConverter = Function.identity();
	}

	public SQLProcessorBuilder addFileExtensions(Collection<String> extensions) {
		this.fileExtensions.addAll(extensions);
		return this;
	}

	public SQLProcessorBuilder addFileExtensions(String... extensions) {
		ArrayUtils.addAll(this.fileExtensions, extensions);
		return this;
	}

	// TODO create a Iterable transformer factory? Maybe the Collection one can be adapted for any Iterable instead of any Collection
	// TODO create Iterator and Enumeration transformer factories?
	public SQLProcessorBuilder addTransformerFactory(ResultSetTransformerFactory factory) {
		this.transformerFactories.add(factory);
		return this;
	}

	public <T> SQLProcessorBuilder addTransformer(Class<T> type, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new ClassTransformerFactory<>(type, transformer));
	}

	public <T> SQLProcessorBuilder addHierarchyTransformer(Class<T> lowerBound, Class<? super T> upperBound, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new HierarchyTransformerFactory<>(lowerBound, upperBound, transformer));
	}

	public SQLProcessorBuilder addPropertyAccessorFactory(PropertyAccessorFactory factory) {
		this.propertyAccessorFactories.add(factory);
		return this;
	}

	public <T> SQLProcessorBuilder addPropertyAccessorFactory(Class<T> type, PropertyAccessor<? super T> transformer) {
		return this.addPropertyAccessorFactory(new ClassPropertyAccessorFactory<>(type, transformer));
	}

	public <T> SQLProcessorBuilder addHierarchyPropertyAccessorFactory(Class<T> upperBound, PropertyAccessor<? super T> transformer) {
		return this.addPropertyAccessorFactory(new HierarchyPropertyAccessorFactory<>(upperBound, transformer));
	}

	public SQLProcessorBuilder addIndexAccessorFactory(IndexAccessorFactory factory) {
		this.indexAccessorFactories.add(factory);
		return this;
	}

	public <T> SQLProcessorBuilder addIndexAccessorFactory(Class<T> type, IndexAccessor<? super T> transformer) {
		return this.addIndexAccessorFactory(new ClassIndexAccessorFactory<>(type, transformer));
	}

	public <T> SQLProcessorBuilder addHierarchyIndexAccessorFactory(Class<T> upperBound, IndexAccessor<? super T> transformer) {
		return this.addIndexAccessorFactory(new HierarchyIndexAccessorFactory<>(upperBound, transformer));
	}

	public SQLProcessorBuilder setColumnNameConverter(Function<String, String> columnNameConveter) {
		this.columnNameConverter = columnNameConveter;
		return this;
	}

	@Override
	public SQLProcessor build() {
		Collections.reverse(this.transformerFactories);
		Collections.reverse(this.fileExtensions);
		return new SQLProcessor(this.connectionSupplier, this.connectionCloser, this.fileExtensions, this.transformerFactories, this.propertyAccessorFactories, this.indexAccessorFactories, this.columnNameConverter);
	}

}
