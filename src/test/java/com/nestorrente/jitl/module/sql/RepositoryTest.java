package com.nestorrente.jitl.module.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.nestorrente.jitl.Jitl;
import com.nestorrente.jitl.JitlBuilder;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RepositoryTest {

	private static Connection CONNECTION;
	private static Repository REPO;

	private static String GENERATED_NAME;

	@BeforeClass
	public static void openConnectionAndCreateRepository() throws ClassNotFoundException, SQLException {

		Class.forName("com.mysql.cj.jdbc.Driver");

		CONNECTION = DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "");
		CONNECTION.setAutoCommit(false);

		Jitl jitl = new JitlBuilder()
			.addModule(
				new SQLModuleBuilder(CONNECTION)
					.build())
			.build();

		REPO = jitl.getInstance(Repository.class);

		GENERATED_NAME = "test:" + System.currentTimeMillis();

	}

	@AfterClass
	public static void closeConnection() throws SQLException {

		if(CONNECTION == null) {
			return;
		}

		try {
			CONNECTION.rollback();
		} finally {
			CONNECTION.close();
		}

	}

	@Test
	public void _1_addReturns1() {

		int insertedId = REPO.add(GENERATED_NAME);

		assertTrue(insertedId > 3);

	}

	@Test
	public void _2_getNameReturnsGeneratedName() {

		String name = REPO.getName(1);

		assertEquals("uno", name);

	}

	@Test
	public void _3_updateReturns1() {

		int affectedRows = REPO.update(1, "uno");

		assertEquals(1, affectedRows);

	}

	@Test
	public void _4_deleteReturnsNothing() {
		REPO.delete();
	}

}
