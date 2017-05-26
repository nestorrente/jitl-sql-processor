package com.nestorrente.jitl.module.sql.yugioh;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.nestorrente.jitl.Jitl;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.util.TestUtils;

public abstract class YugiohDatabaseTest {

	private static Connection CONNECTION;
	protected static Jitl JITL;

	@BeforeClass
	public static void setUp() throws ClassNotFoundException, SQLException {

		CONNECTION = TestUtils.openAndInitializeYugiohDatabase();
		CONNECTION.setAutoCommit(false);

		JITL = Jitl.builder()
			.addModule(SQLModule.defaultInstance(CONNECTION))
			.build();

	}

	@AfterClass
	public static void tearDown() throws SQLException {
		try {
			TestUtils.dropYugiohDatabase();
		} finally {
			CONNECTION.close();
		}
	}

	@After
	public void rollback() throws ClassNotFoundException, SQLException {
		CONNECTION.rollback();
	}

}
