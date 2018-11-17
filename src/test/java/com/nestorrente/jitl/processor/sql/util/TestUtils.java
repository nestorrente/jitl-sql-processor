package com.nestorrente.jitl.processor.sql.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TestUtils {

	private static final String YUGIOH_DATABASE_NAME = "yugioh";
	private static final String TYPES_DATABASE_NAME = "types";

	private static final String[] YUGIOH_DATABASE_INITIALIZATION_STATEMENTS = {
			"CREATE TABLE \"trainers\" (\"id\" INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, \"name\" VARCHAR(50))",
			"INSERT INTO \"trainers\"(\"name\") VALUES ('Yugi Muto')",
			"INSERT INTO \"trainers\"(\"name\") VALUES ('Seto Kaiba')",
			"INSERT INTO \"trainers\"(\"name\") VALUES ('Joey Wheeler')",
			"CREATE TABLE \"monsters\" (\"id\" INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, \"name\" VARCHAR(50), \"level\" INTEGER, \"attack\" INTEGER, \"defense\" INTEGER)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Blue-Eyes White Dragon', 8, 3000, 2500)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Dark Magician', 7, 2500, 2100)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Time Wizard', 2, 500, 400)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Red-Eyes Black Dragon', 7, 2400, 2000)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Summoned Skull', 6, 2500, 1200)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Gaia The Fierce Knight', 7, 2300, 2100)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Kuriboh', 1, 300, 200)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('Obelisk the Tormentor', 10, 4000, 4000)",
			"INSERT INTO \"monsters\"(\"name\", \"level\", \"attack\", \"defense\") VALUES ('The Winged Dragon of Ra', 10, null, null)",
	};

	private static final String[] TYPES_DATABASE_INITIALIZATION_STATEMENTS = {
			"CREATE TABLE \"text_types_table\" (\"id\" INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, \"char_column\" CHAR, \"long_varchar_column\" LONG VARCHAR)",
			"INSERT INTO \"text_types_table\"(\"char_column\", \"long_varchar_column\") VALUES ('c', 'word')",
			"INSERT INTO \"text_types_table\"(\"char_column\", \"long_varchar_column\") VALUES (null, null)",
			"CREATE TABLE \"datetime_types_table\" (\"id\" INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, \"bigint_column\" BIGINT, \"date_column\" DATE, \"time_column\" TIME, \"timestamp_column\" TIMESTAMP)",
			"INSERT INTO \"datetime_types_table\"(\"bigint_column\", \"date_column\", \"time_column\", \"timestamp_column\") VALUES (1494093600000, '2017-05-06', '20:00:00', '2017-05-06 18:00:00')",
			"INSERT INTO \"datetime_types_table\"(\"bigint_column\", \"date_column\", \"time_column\", \"timestamp_column\") VALUES (null, null, null, null)",
	};

	private static void initializeDatabase(Connection connection, String[] databaseInitializationStatements) throws ClassNotFoundException, SQLException {

		Statement stmt = connection.createStatement();

		for(String initializationStatement : databaseInitializationStatements) {
			stmt.addBatch(initializationStatement);
		}

		stmt.executeBatch();

	}

	private static Connection openAndInitializeDatabase(String databaseName, String[] databaseInitializationStatements) throws ClassNotFoundException, SQLException {

		Class.forName("org.apache.derby.jdbc.EmbeddedDriver");

		Connection connection = DerbyUtils.createAndOpenInMemory(databaseName);

		try {

			initializeDatabase(connection, databaseInitializationStatements);

			connection.setAutoCommit(false);

			return connection;

		} catch(Throwable th) {

			try {
				connection.close();
			} catch(Throwable ignore) {
			}

			throw th;

		}

	}

	public static Connection openAndInitializeYugiohDatabase() throws ClassNotFoundException, SQLException {
		return openAndInitializeDatabase(YUGIOH_DATABASE_NAME, YUGIOH_DATABASE_INITIALIZATION_STATEMENTS);
	}

	public static void dropYugiohDatabase() throws SQLException {
		DerbyUtils.dropInMemory(YUGIOH_DATABASE_NAME);
	}

	public static Connection openAndInitializeTypesDatabase() throws ClassNotFoundException, SQLException {
		return openAndInitializeDatabase(TYPES_DATABASE_NAME, TYPES_DATABASE_INITIALIZATION_STATEMENTS);
	}

	public static void dropTypesDatabase() throws SQLException {
		DerbyUtils.dropInMemory(TYPES_DATABASE_NAME);
	}

}
