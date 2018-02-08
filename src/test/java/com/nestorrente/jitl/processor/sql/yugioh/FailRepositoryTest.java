package com.nestorrente.jitl.processor.sql.yugioh;

import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

public class FailRepositoryTest extends YugiohDatabaseTest {

	private static FailRepository REPOSITORY;

	@BeforeClass
	public static void setUpRepository() throws ClassNotFoundException, SQLException {
		REPOSITORY = JITL.getInstance(FailRepository.class);
	}

	@Test(expected = RuntimeException.class)
	public void arrayAccessByPropertyThrowsRuntimeException() {
		REPOSITORY.arrayAccessByProperty(1);
	}

	@Test(expected = RuntimeException.class)
	public void pojoAccessByIndexThrowsRuntimeException() {
		REPOSITORY.pojoAccessByIndex(new Monster());
	}

}
