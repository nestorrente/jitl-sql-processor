package com.nestorrente.jitl.module.sql.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DerbyUtils {

	private static final int SYSTEM_SHUTDOWN_ERROR_CODE = 50000;
	private static final String SYSTEM_SHUTDOWN_SQL_STATE = "XJ015";

	private static final int DATABASE_SHUTDOWN_ERROR_CODE = 45000;
	private static final String DATABASE_SHUTDOWN_SQL_STATE = "08006";

	public static Connection createAndOpen(String databaseName) throws SQLException {
		return DriverManager.getConnection("jdbc:derby:" + databaseName + ";create=true");
	}

	public static Connection createAndOpenInMemory(String databaseName) throws SQLException {
		return DriverManager.getConnection("jdbc:derby:memory:" + databaseName + ";create=true");
	}

	public static void shutdownAll() throws SQLException {
		internalShutdown("jdbc:derby:;shutdown=true", SYSTEM_SHUTDOWN_ERROR_CODE, SYSTEM_SHUTDOWN_SQL_STATE);
	}

	public static void shutdown(String databaseName) throws SQLException {
		internalShutdown("jdbc:derby:" + databaseName + ";shutdown=true", DATABASE_SHUTDOWN_ERROR_CODE, DATABASE_SHUTDOWN_SQL_STATE);
	}

	public static void shutdownInMemory(String databaseName) throws SQLException {
		internalShutdown("jdbc:derby:memory:" + databaseName + ";shutdown=true", DATABASE_SHUTDOWN_ERROR_CODE, DATABASE_SHUTDOWN_SQL_STATE);
	}

	public static void drop(String databaseName) throws SQLException {
		internalShutdown("jdbc:derby:" + databaseName + ";drop=true", DATABASE_SHUTDOWN_ERROR_CODE, DATABASE_SHUTDOWN_SQL_STATE);
	}

	public static void dropInMemory(String databaseName) throws SQLException {
		internalShutdown("jdbc:derby:memory:" + databaseName + ";drop=true", DATABASE_SHUTDOWN_ERROR_CODE, DATABASE_SHUTDOWN_SQL_STATE);
	}

	private static void internalShutdown(String connectionString, int expectedErrorCode, String expectedSQLState) throws SQLException {

		try {

			DriverManager.getConnection(connectionString);

		} catch(SQLException ex) {

			boolean successfullShutdown = expectedErrorCode == ex.getErrorCode() && expectedSQLState.equals(ex.getSQLState());

			if(!successfullShutdown) {
				throw ex;
			}

		}

	}

}
