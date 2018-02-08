package com.nestorrente.jitl.processor.sql.types;

import com.google.common.base.CaseFormat;
import com.nestorrente.jitl.Jitl;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.util.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public abstract class TypesDatabaseTest {

	private static Connection CONNECTION;
	protected static Jitl JITL;

	@BeforeClass
	public static void setUp() throws ClassNotFoundException, SQLException {

		CONNECTION = TestUtils.openAndInitializeTypesDatabase();
		CONNECTION.setAutoCommit(false);

		Function<String, String> lowerUnderscoreToLowerCamelConverter = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL);

		JITL = Jitl.builder()
				.registerProcessor(
						SQLProcessor.builder(CONNECTION)
								.setColumnNameConverter(lowerUnderscoreToLowerCamelConverter)
								.build())
				.build();

	}

	@AfterClass
	public static void tearDown() throws SQLException {
		try {
			TestUtils.dropTypesDatabase();
		} finally {
			CONNECTION.close();
		}
	}

	@After
	public void rollback() throws ClassNotFoundException, SQLException {
		CONNECTION.rollback();
	}

}
