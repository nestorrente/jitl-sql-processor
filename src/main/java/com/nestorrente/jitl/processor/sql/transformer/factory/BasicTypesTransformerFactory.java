package com.nestorrente.jitl.processor.sql.transformer.factory;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.transformer.CellTransformer;
import com.nestorrente.jitl.processor.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.processor.sql.util.JdbcUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;

/**
 *
 */
public class BasicTypesTransformerFactory implements ResultSetTransformerFactory {

	private final CellTransformer<Void> voidTransformer = (rs, i) -> null;
	private final CellTransformer<String> stringTransformer = (rs, i) -> rs.getString(i);
	private final CellTransformer<Character> charTransformer = JdbcUtils::getCharacter;
	private final CellTransformer<Boolean> booleanTransformer = JdbcUtils::getBoolean;
	private final CellTransformer<Byte> byteTransformer = JdbcUtils::getByte;
	private final CellTransformer<Short> shortTransformer = JdbcUtils::getShort;
	private final CellTransformer<Integer> intTransformer = JdbcUtils::getInteger;
	private final CellTransformer<Long> longTransformer = JdbcUtils::getLong;
	private final CellTransformer<Float> floatTransformer = JdbcUtils::getFloat;
	private final CellTransformer<Double> doubleTransformer = JdbcUtils::getDouble;

	// TODO crear otra factory para BigDecimal, BigInteger y Number?
	private final CellTransformer<BigDecimal> bigDecimalTransformer = ResultSet::getBigDecimal;
	private final CellTransformer<BigInteger> bigIntegerTransformer = JdbcUtils::getBigInteger;

	@Override
	public ResultSetTransformer<?> get(SQLProcessor processor, TypeToken<?> type) {

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
		} else if(BigDecimal.class.equals(clazz) || Number.class.equals(clazz)) {
			return this.bigDecimalTransformer;
		} else {
			return null;
		}

	}

}
