package com.nestorrente.jitl.module.sql.yugioh;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

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
