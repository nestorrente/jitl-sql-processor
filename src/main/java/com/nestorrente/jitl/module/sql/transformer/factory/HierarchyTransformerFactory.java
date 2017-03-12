package com.nestorrente.jitl.module.sql.transformer.factory;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;

public class HierarchyTransformerFactory<T> implements ResultSetTransformerFactory {

	private final Class<T> lowerBound;
	private final Class<? super T> upperBound;
	private final ResultSetTransformer<? extends T> transformer;

	// TODO allow multiple upperBounds?
	public HierarchyTransformerFactory(Class<T> lowerBound, Class<? super T> upperBound, ResultSetTransformer<? extends T> transformer) {
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		this.transformer = transformer;
	}

	@Override
	public ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type) {

		Class<?> clazz = type.getRawType();

		if(clazz.isAssignableFrom(this.lowerBound) && this.upperBound.isAssignableFrom(clazz)) {
			return this.transformer;
		}

		return null;

	}

}
