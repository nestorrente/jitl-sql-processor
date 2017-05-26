package com.nestorrente.jitl.module.sql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.Builder;

import com.nestorrente.jitl.module.sql.accessor.index.IndexAccessor;
import com.nestorrente.jitl.module.sql.accessor.index.factory.ClassIndexAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.index.factory.HierarchyIndexAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.index.factory.IndexAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.property.PropertyAccessor;
import com.nestorrente.jitl.module.sql.accessor.property.factory.ClassPropertyAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.property.factory.HierarchyPropertyAccessorFactory;
import com.nestorrente.jitl.module.sql.accessor.property.factory.PropertyAccessorFactory;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.factory.ClassTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.HierarchyTransformerFactory;
import com.nestorrente.jitl.module.sql.transformer.factory.ResultSetTransformerFactory;
import com.nestorrente.jitl.util.ArrayUtils;

public class SQLModuleBuilder implements Builder<SQLModule> {

	private final Supplier<Connection> connectionSupplier;
	private final Consumer<Connection> connectionCloser;
	private final List<String> fileExtensions;
	private final List<ResultSetTransformerFactory> transformerFactories;
	private final List<PropertyAccessorFactory> propertyAccessorFactories;
	private final List<IndexAccessorFactory> indexAccessorFactories;
	private Function<String, String> columnNameConverter;

	public SQLModuleBuilder(Supplier<Connection> connectionSupplier, Consumer<Connection> connectionCloser) {
		this.connectionSupplier = connectionSupplier;
		this.connectionCloser = connectionCloser;
		this.fileExtensions = new ArrayList<>();
		this.transformerFactories = new ArrayList<>();
		this.propertyAccessorFactories = new ArrayList<>();
		this.indexAccessorFactories = new ArrayList<>();
		this.columnNameConverter = Function.identity();
	}

	public SQLModuleBuilder addFileExtensions(Collection<String> extensions) {
		this.fileExtensions.addAll(extensions);
		return this;
	}

	public SQLModuleBuilder addFileExtensions(String... extensions) {
		ArrayUtils.addAll(this.fileExtensions, extensions);
		return this;
	}

	// TODO create a Iterable transformer factory? Maybe the Collection one can be adapted for any Iterable instead of any Collection
	// TODO create Iterator and Enumeration transformer factories?
	public SQLModuleBuilder addTransformerFactory(ResultSetTransformerFactory factory) {
		this.transformerFactories.add(factory);
		return this;
	}

	public <T> SQLModuleBuilder addTransformer(Class<T> type, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new ClassTransformerFactory<>(type, transformer));
	}

	public <T> SQLModuleBuilder addHierarchyTransformer(Class<T> lowerBound, Class<? super T> upperBound, ResultSetTransformer<? extends T> transformer) {
		return this.addTransformerFactory(new HierarchyTransformerFactory<>(lowerBound, upperBound, transformer));
	}

	public SQLModuleBuilder addPropertyAccessorFactory(PropertyAccessorFactory factory) {
		this.propertyAccessorFactories.add(factory);
		return this;
	}

	public <T> SQLModuleBuilder addPropertyAccessorFactory(Class<T> type, PropertyAccessor<? super T> transformer) {
		return this.addPropertyAccessorFactory(new ClassPropertyAccessorFactory<>(type, transformer));
	}

	public <T> SQLModuleBuilder addHierarchyPropertyAccessorFactory(Class<T> upperBound, PropertyAccessor<? super T> transformer) {
		return this.addPropertyAccessorFactory(new HierarchyPropertyAccessorFactory<>(upperBound, transformer));
	}

	public SQLModuleBuilder addIndexAccessorFactory(IndexAccessorFactory factory) {
		this.indexAccessorFactories.add(factory);
		return this;
	}

	public <T> SQLModuleBuilder addIndexAccessorFactory(Class<T> type, IndexAccessor<? super T> transformer) {
		return this.addIndexAccessorFactory(new ClassIndexAccessorFactory<>(type, transformer));
	}

	public <T> SQLModuleBuilder addHierarchyIndexAccessorFactory(Class<T> upperBound, IndexAccessor<? super T> transformer) {
		return this.addIndexAccessorFactory(new HierarchyIndexAccessorFactory<>(upperBound, transformer));
	}

	public SQLModuleBuilder setColumnNameConverter(Function<String, String> columnNameConveter) {
		this.columnNameConverter = columnNameConveter;
		return this;
	}

	@Override
	public SQLModule build() {
		Collections.reverse(this.transformerFactories);
		Collections.reverse(this.fileExtensions);
		return new SQLModule(this.connectionSupplier, this.connectionCloser, this.fileExtensions, this.transformerFactories, this.propertyAccessorFactories, this.indexAccessorFactories, this.columnNameConverter);
	}

}
