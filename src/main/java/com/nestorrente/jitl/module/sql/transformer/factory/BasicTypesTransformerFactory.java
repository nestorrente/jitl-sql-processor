package com.nestorrente.jitl.module.sql.transformer.factory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;

import com.google.common.reflect.TypeToken;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.transformer.CellTransformer;
import com.nestorrente.jitl.module.sql.transformer.ResultSetTransformer;
import com.nestorrente.jitl.module.sql.util.SqlUtils;

/**
 *
 */
public class BasicTypesTransformerFactory implements ResultSetTransformerFactory {

	private final CellTransformer<Void> voidTransformer = (rs, i) -> null;
	private final CellTransformer<String> stringTransformer = (rs, i) -> rs.getString(i);
	private final CellTransformer<Character> charTransformer = SqlUtils::getCharacter;
	private final CellTransformer<Boolean> booleanTransformer = SqlUtils::getBoolean;
	private final CellTransformer<Byte> byteTransformer = SqlUtils::getByte;
	private final CellTransformer<Short> shortTransformer = SqlUtils::getShort;
	private final CellTransformer<Integer> intTransformer = SqlUtils::getInteger;
	private final CellTransformer<Long> longTransformer = SqlUtils::getLong;
	private final CellTransformer<Float> floatTransformer = SqlUtils::getFloat;
	private final CellTransformer<Double> doubleTransformer = SqlUtils::getDouble;

	// TODO crear otra factory para BigDecimal, BigInteger y Number?
	private final CellTransformer<BigDecimal> bigDecimalTransformer = ResultSet::getBigDecimal;
	private final CellTransformer<BigInteger> bigIntegerTransformer = SqlUtils::getBigInteger;

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
		} else if(BigDecimal.class.equals(clazz) || Number.class.equals(clazz)) {
			return this.bigDecimalTransformer;
		} else {
			return null;
		}

	}

}
