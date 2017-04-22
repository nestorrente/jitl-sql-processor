package com.nestorrente.jitl.module.sql;

import static org.junit.Assert.assertEquals;

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
public class TrainersRepositoryTest {

	private static Connection CONNECTION;
	private static TrainersRepository REPO;

	@BeforeClass
	public static void openConnectionAndCreateRepository() throws ClassNotFoundException, SQLException {

		Class.forName("org.hsqldb.jdbc.JDBCDriver");

		CONNECTION = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/yugioh", "SA", "");
		CONNECTION.setAutoCommit(false);

		Jitl jitl = Jitl.builder()
			.addModule(SQLModule.defaultInstance(CONNECTION))
			.build();

		REPO = jitl.getInstance(TrainersRepository.class);

	}

	@AfterClass
	public static void closeConnection() throws ClassNotFoundException, SQLException {

		try {
			CONNECTION.rollback();
		} finally {
			CONNECTION.close();
		}

	}

	@Test
	public void test1addReturns1() {

		int insertedId = REPO.add("Maximillion Pegasus");

		assertEquals(4, insertedId);

	}

	@Test
	public void test2getName2ReturnsSetoKaiba() {

		String name = REPO.getName(2);

		assertEquals("Seto Kaiba", name);

	}

	@Test
	public void test3updateReturns1() {

		int affectedRows = REPO.update(1, "Yami Yugi");

		assertEquals(1, affectedRows);

	}

	@Test(expected = RuntimeException.class)
	public void test4deleteWithAffectedRowsReturningVoidThrowsException() {
		REPO.delete(4);
	}

}
