package com.nestorrente.jitl.module.sql.yugioh;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

public class TrainersRepositoryTest extends YugiohDatabaseTest {

	private static TrainersRepository REPOSITORY;

	@BeforeClass
	public static void setUpRepository() throws ClassNotFoundException, SQLException {
		REPOSITORY = JITL.getInstance(TrainersRepository.class);
	}

	@Test
	public void addReturns4() {

		int insertedId = REPOSITORY.add("Maximillion Pegasus");

		assertEquals(4, insertedId);

	}

	@Test
	public void getName2ReturnsSetoKaiba() {

		String name = REPOSITORY.getName(2);

		assertEquals("Seto Kaiba", name);

	}

	@Test
	public void updateReturns1() {

		int affectedRows = REPOSITORY.update(1, "Yami Yugi");

		assertEquals(1, affectedRows);

	}

	@Test(expected = RuntimeException.class)
	public void deleteWithAffectedRowsReturningVoidThrowsException() {
		REPOSITORY.delete(3);
	}

}
