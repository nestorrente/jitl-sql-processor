package com.nestorrente.jitl.module.sql.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.BeforeClass;
import org.junit.Test;

public class TypesTest extends TypesDatabaseTest {

	private static final String CHAR_COLUMN = "char_column";
	private static final String LONG_VARCHAR_COLUMN = "long_varchar_column";
	private static final String TEXT_TYPES_TABLE = "text_types_table";

	private static final String DATETIME_TYPES_TABLE = "datetime_types_table";
	private static final String BIGINT_COLUMN = "bigint_column";
	private static final String DATE_COLUMN = "date_column";
	private static final String TIME_COLUMN = "time_column";
	private static final String TIMESTAMP_COLUMN = "timestamp_column";
	private static final String[] DATETIME_TYPES_TABLE_COLUMNS = {
			BIGINT_COLUMN,
			DATE_COLUMN,
			TIME_COLUMN,
			TIMESTAMP_COLUMN
	};

	private static final long EXPECTED_TIMESTAMP = 1494093600000L;

	private static TypesRepository REPOSITORY;

	@BeforeClass
	public static void openConnectionAndCreateRepository() throws ClassNotFoundException, SQLException {

		REPOSITORY = JITL.getInstance(TypesRepository.class);

	}

	@Test
	public void getTypesReturnsTypesPojo() {

		TextTypesPojo[] types = REPOSITORY.getTypes();

		assertEquals(2, types.length);

		assertEquals(Character.valueOf('c'), types[0].getCharColumn());
		assertEquals("word", types[0].getLongVarcharColumn());

		assertNull(types[1].getCharColumn());
		assertNull(types[1].getLongVarcharColumn());

	}

	@Test
	public void getCharReceivingCharColumnTextTypesTableAnd1ReturnsA() {

		char ch = REPOSITORY.getChar(CHAR_COLUMN, TEXT_TYPES_TABLE, 1);

		assertEquals('c', ch);

	}

	@Test(expected = NullPointerException.class)
	public void getCharReceivingCharColumnAndTextTypesTable2ThrowsNullPointerException() {
		REPOSITORY.getChar(CHAR_COLUMN, TEXT_TYPES_TABLE, 2);
	}

	@Test
	public void getCharReceivingNvarcharColumnTextTypesTableAnd1ReturnsW() {

		char ch = REPOSITORY.getChar(LONG_VARCHAR_COLUMN, TEXT_TYPES_TABLE, 1);

		assertEquals('w', ch);

	}

	@Test
	public void getCharacterReceivingNvarcharColumnTextTypesTableAnd2ReturnsNull() {

		Character ch = REPOSITORY.getCharacter(LONG_VARCHAR_COLUMN, TEXT_TYPES_TABLE, 2);

		assertNull(ch);

	}

	@Test
	public void getDateReceivingAnyColumnDatetimeTypesTableAnd1ReturnsDate() throws ParseException {

		// TODO mejorar/limpiar este test

		Date expectedDatetime = new Date(EXPECTED_TIMESTAMP);

		DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
		Date expectedDate = dateFormatter.parse(dateFormatter.format(expectedDatetime));

		DateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");
		Date expectedTime = timeFormatter.parse(timeFormatter.format(expectedDatetime));

		for(String column : DATETIME_TYPES_TABLE_COLUMNS) {

			Date actual = REPOSITORY.getDate(column, DATETIME_TYPES_TABLE, 1);

			Date expected;

			switch(column) {
				case "date_column":
					expected = expectedDate;
					break;
				case "time_column":
					expected = expectedTime;
					break;
				default:
					expected = expectedDatetime;
					break;
			}

			assertEquals("While testing column \"" + column + "\":", expected, actual);

		}

	}

	@Test
	public void getDateReceivingAnyColumnDatetimeTypesTableAnd2ReturnsNull() {

		for(String column : DATETIME_TYPES_TABLE_COLUMNS) {

			Date date = REPOSITORY.getDate(column, DATETIME_TYPES_TABLE, 2);

			assertNull("While testing column " + column, date);

		}

	}

}
