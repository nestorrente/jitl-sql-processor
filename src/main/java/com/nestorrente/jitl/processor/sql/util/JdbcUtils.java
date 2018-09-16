package com.nestorrente.jitl.processor.sql.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

public class JdbcUtils {

	private static Calendar LOCAL_TIMEZONE_CALENDAR = Calendar.getInstance();
	private static Calendar UTC_CALENDAR = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

	public static void setObject(PreparedStatement statement, int index, Object value) throws SQLException {

		if(value == null) {
			try {
				statement.setObject(index, null);
			} catch(SQLException ex) {
				statement.setNull(index, Types.OTHER);
			}
		} else if(value instanceof Byte) {
			statement.setByte(index, (Byte) value);
		} else if(value instanceof Short) {
			statement.setShort(index, (Short) value);
		} else if(value instanceof Integer) {
			statement.setInt(index, (Integer) value);
		} else if(value instanceof Long) {
			statement.setLong(index, (Long) value);
		} else if(value instanceof Float) {
			statement.setFloat(index, (Float) value);
		} else if(value instanceof Double) {
			statement.setDouble(index, (Double) value);
		} else if(value instanceof String) {
			statement.setString(index, (String) value);
		} else if(value instanceof Date) {
			statement.setDate(index, (Date) value, LOCAL_TIMEZONE_CALENDAR);
		} else if(value instanceof Time) {
			statement.setTime(index, (Time) value, LOCAL_TIMEZONE_CALENDAR);
		} else if(value instanceof Timestamp) {
			statement.setTimestamp(index, (Timestamp) value, UTC_CALENDAR);
		} else if(value instanceof java.util.Date) {
			statement.setTimestamp(index, new Timestamp(((java.util.Date) value).getTime()), UTC_CALENDAR);
		} else if(value instanceof Calendar) {
			Calendar calendar = (Calendar) value;
			statement.setTimestamp(index, new Timestamp(calendar.getTimeInMillis()), calendar);
		} else if(value instanceof BigDecimal) {
			statement.setBigDecimal(index, (BigDecimal) value);
			// There isn't a clear way to set a BigInteger, so we let it to setObject
			// } else if(value instanceof BigInteger) {
			// 	statement.setBigDecimal(index, new BigDecimal((BigInteger) value));
		} else {
			statement.setObject(index, value);
		}

	}

	public static Object getObject(ResultSet resultSet, int columnIndex) throws SQLException {

		// TODO add all SQL types
		// Think about: binary types must use getBytes() or getBinaryStream()?
		switch(resultSet.getMetaData().getColumnType(columnIndex)) {

			case Types.NULL:
				return null;

			case Types.BIT:
			case Types.BOOLEAN:
				return getBoolean(resultSet, columnIndex);

			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER:
				return getInteger(resultSet, columnIndex);

			case Types.BIGINT:
				return getBigInteger(resultSet, columnIndex);

			case Types.FLOAT:
				return getFloat(resultSet, columnIndex);
			case Types.DOUBLE:
				return getDouble(resultSet, columnIndex);

			case Types.REAL:
			case Types.DECIMAL:
			case Types.NUMERIC:
				return resultSet.getBigDecimal(columnIndex);

			case Types.CHAR:
				return getCharacter(resultSet, columnIndex);
			case Types.NCHAR:
				return getNCharacter(resultSet, columnIndex);
			case Types.NVARCHAR:
				return resultSet.getNString(columnIndex);
			case Types.VARCHAR:
				return resultSet.getString(columnIndex);
			case Types.LONGVARCHAR:
				return resultSet.getAsciiStream(columnIndex);
			case Types.LONGNVARCHAR:
				return resultSet.getAsciiStream(columnIndex);
			case Types.CLOB:
				return resultSet.getClob(columnIndex);
			case Types.NCLOB:
				return resultSet.getNClob(columnIndex);

			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
				return resultSet.getBytes(columnIndex);
			case Types.BLOB:
				return resultSet.getBlob(columnIndex);
			case Types.DATE:
				return resultSet.getDate(columnIndex, LOCAL_TIMEZONE_CALENDAR);
			case Types.TIME:
			case Types.TIME_WITH_TIMEZONE: // FIXME timezone loose - is it a real problem?
				return resultSet.getTime(columnIndex, LOCAL_TIMEZONE_CALENDAR);
			case Types.TIMESTAMP:
			case Types.TIMESTAMP_WITH_TIMEZONE: // FIXME timezone loose - is it a real problem?
				return resultSet.getTimestamp(columnIndex, UTC_CALENDAR);

			case Types.ARRAY:
				return getArray(resultSet, columnIndex);

			case Types.REF:
				return resultSet.getRef(columnIndex);

			case Types.ROWID:
				return resultSet.getRowId(columnIndex);

			case Types.SQLXML:
				return resultSet.getSQLXML(columnIndex);

			case Types.OTHER:
			case Types.JAVA_OBJECT:
			case Types.DISTINCT:
			case Types.STRUCT:
			case Types.DATALINK:
			case Types.REF_CURSOR:
			default:
				return resultSet.getObject(columnIndex);

		}

	}

	public static Character getCharacter(ResultSet resultSet, int columnIndex) throws SQLException {
		String value = resultSet.getString(columnIndex);
		return StringUtils.isEmpty(value) ? null : value.charAt(0);
	}

	public static Character getNCharacter(ResultSet resultSet, int columnIndex) throws SQLException {
		String value = resultSet.getNString(columnIndex);
		return StringUtils.isEmpty(value) ? null : value.charAt(0);
	}

	public static Boolean getBoolean(ResultSet resultSet, int columnIndex) throws SQLException {
		boolean value = resultSet.getBoolean(columnIndex);
		return resultSet.wasNull() ? null : value;
	}

	public static Byte getByte(ResultSet resultSet, int columnIndex) throws SQLException {
		byte value = resultSet.getByte(columnIndex);
		return resultSet.wasNull() ? null : value;
	}

	public static Short getShort(ResultSet resultSet, int columnIndex) throws SQLException {
		short value = resultSet.getShort(columnIndex);
		return resultSet.wasNull() ? null : value;
	}

	public static Integer getInteger(ResultSet resultSet, int columnIndex) throws SQLException {
		int value = resultSet.getInt(columnIndex);
		return resultSet.wasNull() ? null : value;
	}

	public static Long getLong(ResultSet resultSet, int columnIndex) throws SQLException {
		long value = resultSet.getLong(columnIndex);
		return resultSet.wasNull() ? null : value;
	}

	public static Float getFloat(ResultSet resultSet, int columnIndex) throws SQLException {
		float value = resultSet.getFloat(columnIndex);
		return resultSet.wasNull() ? null : value;
	}

	public static Double getDouble(ResultSet resultSet, int columnIndex) throws SQLException {
		double value = resultSet.getDouble(columnIndex);
		return resultSet.wasNull() ? null : value;
	}

	public static BigInteger getBigInteger(ResultSet resultSet, int columnIndex) throws SQLException {
		BigDecimal value = resultSet.getBigDecimal(columnIndex);
		return value == null ? null : value.toBigInteger();
	}

	public static Object[] getArray(ResultSet resultSet, int columnIndex) throws SQLException {

		Array sqlArray = resultSet.getArray(columnIndex);

		if(sqlArray == null) {
			return null;
		}

		Object array = sqlArray.getArray();

		if(array == null) {
			return null;
		}

		if(array instanceof char[]) {
			return ArrayUtils.toObject((char[]) array);
		} else if(array instanceof boolean[]) {
			return ArrayUtils.toObject((boolean[]) array);
		} else if(array instanceof byte[]) {
			return ArrayUtils.toObject((byte[]) array);
		} else if(array instanceof short[]) {
			return ArrayUtils.toObject((short[]) array);
		} else if(array instanceof int[]) {
			return ArrayUtils.toObject((int[]) array);
		} else if(array instanceof long[]) {
			return ArrayUtils.toObject((long[]) array);
		} else if(array instanceof float[]) {
			return ArrayUtils.toObject((float[]) array);
		} else if(array instanceof double[]) {
			return ArrayUtils.toObject((double[]) array);
		} else if(array instanceof Object[]) {
			return (Object[]) array;
		}

		throw new ClassCastException(String.format("Cannot cast %s to java.lang.Object[]", array.getClass().getName()));

	}

	public static PreparedStatement prepareStatement(Connection connection, String sql, Collection<Object> parameters, boolean returnGeneratedKeys) throws Exception {

		PreparedStatement statement = connection.prepareStatement(sql, returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);

		int index = 1;

		for(Object parameter : parameters) {
			JdbcUtils.setObject(statement, index++, parameter);
		}

		return statement;

	}

}
