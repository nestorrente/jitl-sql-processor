package com.nestorrente.jitl.module.sql.accessor.property.factory;

import java.lang.reflect.Field;

import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.accessor.property.PropertyAccessor;
import com.nestorrente.jitl.util.ReflectionUtils;

public class ReflectivePropertyAccessorFactory implements PropertyAccessorFactory {

	@Override
	public PropertyAccessor<?> get(SQLModule module, Class<?> type) {

		if(type.equals(Object.class) || type.isArray()) {
			return null;
		}

		return new ReflectivePropertyAccessor<>(type);

	}

	private static class ReflectivePropertyAccessor<T> implements PropertyAccessor<T> {

		private final Class<T> type;

		public ReflectivePropertyAccessor(Class<T> type) {
			this.type = type;
		}

		@Override
		public Object access(T obj, String key) {

			try {

				// TODO is it worth it to make a cache? Maybe not, because this will be called with many different classes
				Field field = ReflectionUtils.getField(this.type, key);

				return ReflectionUtils.getFieldValue(obj, field);

			} catch(NoSuchFieldException ex) {
				throw new RuntimeException(ex);
			}

		}

	}

}
