package com.nestorrente.jitl.module.sql.transformer.factory;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.transformer.RowTransformer;

public class ArrayTransformerFactory implements ResultSetTransformerFactory {

	@Override
	public ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type) {

		// Ensure "type" is an array
		if(!type.isArray()) {
			return null;
		}

		TypeToken<?> componentType = type.getComponentType();

		RowTransformer<?> componentTransformer;

		try {
			componentTransformer = (RowTransformer<?>) module.getTransformer(componentType);
		} catch(ClassCastException ex) {
			// TODO replace with a custom exception
			throw new RuntimeException("Array elements must be rows or cells of the result set, not an entire result set", ex);
		}

		return new ArrayTransformer(componentType.getRawType(), componentTransformer);

	}

	private static class ArrayTransformer implements ResultSetTransformer<Object> {

		private final Class<?> componentType;
		private final RowTransformer<?> componentTransformer;

		public ArrayTransformer(Class<?> componentType, RowTransformer<?> componentTransformer) {
			this.componentType = componentType;
			this.componentTransformer = componentTransformer;
		}

		@Override
		public Object transform(ResultSet resultSet) throws Exception {

			List<Object> tempList = new ArrayList<>();

			while(resultSet.next()) {
				tempList.add(this.componentTransformer.transformRow(resultSet));
			}

			Object array = Array.newInstance(this.componentType, tempList.size());

			int index = 0;
			for(Object element : tempList) {
				Array.set(array, index++, element);
			}

			return array;

		}

	}

}
