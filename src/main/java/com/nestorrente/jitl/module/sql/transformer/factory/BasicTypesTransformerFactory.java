package com.nestorrente.jitl.module.sql.transformer.factory;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.CellTransformer;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;

public class BasicTypesTransformerFactory implements ResultSetTransformerFactory {

	private final CellTransformer<Void> voidTransformer = (rs, i) -> null;
	private final CellTransformer<Character> charTransformer = (rs, i) -> rs.getString(i).charAt(0);
	private final CellTransformer<Boolean> booleanTransformer = (rs, i) -> rs.getBoolean(i);
	private final CellTransformer<Byte> byteTransformer = (rs, i) -> rs.getByte(i);
	private final CellTransformer<Short> shortTransformer = (rs, i) -> rs.getShort(i);
	private final CellTransformer<Integer> intTransformer = (rs, i) -> rs.getInt(i);
	private final CellTransformer<Long> longTransformer = (rs, i) -> rs.getLong(i);
	private final CellTransformer<Float> floatTransformer = (rs, i) -> rs.getFloat(i);
	private final CellTransformer<Double> doubleTransformer = (rs, i) -> rs.getDouble(i);
	private final CellTransformer<BigInteger> bigIntegerTransformer = (rs, i) -> rs.getBigDecimal(i).toBigIntegerExact();
	private final CellTransformer<BigDecimal> bigDecimalTransformer = (rs, i) -> rs.getBigDecimal(i);
	private final CellTransformer<String> stringTransformer = (rs, i) -> rs.getString(i);

	@Override
	public ResultSetTransformer<?> get(SQLModule module, TypeToken<?> type) {

		Class<?> clazz = type.getRawType();

		// this conditions are sorted: most commonly used types go first

		if(String.class.equals(clazz)) {
			return this.stringTransformer;
		} else if(int.class.equals(clazz) || Integer.class.equals(clazz)) {
			return this.intTransformer;
		} else if(long.class.equals(clazz) || Long.class.equals(clazz)) {
			return this.longTransformer;
		} else if(float.class.equals(clazz) || Float.class.equals(clazz)) {
			return this.floatTransformer;
		} else if(double.class.equals(clazz) || Double.class.equals(clazz) || Number.class.equals(clazz)) {
			return this.doubleTransformer;
		} else if(boolean.class.equals(clazz) || Boolean.class.equals(clazz)) {
			return this.booleanTransformer;
		} else if(void.class.equals(clazz) || Void.class.equals(clazz)) {
			return this.voidTransformer;
		} else if(byte.class.equals(clazz) || Byte.class.equals(clazz)) {
			return this.byteTransformer;
		} else if(short.class.equals(clazz) || Short.class.equals(clazz)) {
			return this.shortTransformer;
		} else if(char.class.equals(clazz) || Character.class.equals(clazz)) {
			return this.charTransformer;
		} else if(BigInteger.class.equals(clazz)) {
			return this.bigIntegerTransformer;
		} else if(BigDecimal.class.equals(clazz)) {
			return this.bigDecimalTransformer;
		} else {
			return null;
		}

	}

}
