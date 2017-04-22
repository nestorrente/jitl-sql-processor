package com.nestorrente.jitl.module.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.nestorrente.jitl.Jitl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FailRepositoryTest {

	private static Connection CONNECTION;
	private static FailRepository REPO;

	@BeforeClass
	public static void openConnectionAndCreateRepository() throws ClassNotFoundException, SQLException {

		Class.forName("org.hsqldb.jdbc.JDBCDriver");

		CONNECTION = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/yugioh", "SA", "");
		CONNECTION.setAutoCommit(false);

		Jitl jitl = Jitl.builder()
			.addModule(SQLModule.defaultInstance(CONNECTION))
			.build();

		REPO = jitl.getInstance(FailRepository.class);

	}

	@AfterClass
	public static void closeConnection() throws ClassNotFoundException, SQLException {

		try {
			CONNECTION.rollback();
		} finally {
			CONNECTION.close();
		}

	}

	@Test(expected = RuntimeException.class)
	public void arrayAccessByPropertyThrowsRuntimeException() {
		REPO.arrayAccessByProperty(1);
	}

	@Test(expected = RuntimeException.class)
	public void pojoAccessByIndexThrowsRuntimeException() {
		REPO.pojoAccessByIndex(new Monster());
	}

}
