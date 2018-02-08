package com.nestorrente.jitl.processor.sql.yugioh;

import com.nestorrente.jitl.Jitl;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.util.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class YugiohDatabaseTest {

	private static Connection CONNECTION;
	protected static Jitl JITL;

	@BeforeClass
	public static void setUp() throws ClassNotFoundException, SQLException {

		CONNECTION = TestUtils.openAndInitializeYugiohDatabase();
		CONNECTION.setAutoCommit(false);

		JITL = Jitl.builder()
				.registerProcessor(SQLProcessor.defaultInstance(CONNECTION))
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
