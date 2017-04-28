package com.nestorrente.jitl.module.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.jooq.lambda.Unchecked;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.nestorrente.jitl.Jitl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MonstersRepositoryTest {

	private static Connection CONNECTION;
	private static MonstersRepository REPO;

	@BeforeClass
	public static void openConnectionAndCreateRepository() throws ClassNotFoundException, SQLException {

		Class.forName("org.hsqldb.jdbc.JDBCDriver");

		CONNECTION = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/yugioh", "SA", "");
		CONNECTION.setAutoCommit(false);

		Jitl jitl = Jitl.builder()
			.addModule(SQLModule.defaultInstance(() -> CONNECTION, Unchecked.consumer(con -> con.rollback())))
			.build();

		REPO = jitl.getInstance(MonstersRepository.class);

	}

	@AfterClass
	public static void closeConnection() throws ClassNotFoundException, SQLException {
		CONNECTION.close();
	}

	@Test
	public void find5ReturnsSummonedSkull() {

		Monster monster = REPO.find(5);

		assertEquals(5, monster.getId());
		assertEquals("Summoned Skull", monster.getName());
		assertEquals(6, monster.getLevel());
		assertEquals(2500, monster.getAttack().intValue());
		assertEquals(1200, monster.getDefense().intValue());

	}

	@Test
	public void find9ReturnsTheWingedDragonOfRaWithNullAttackAndDefense() {

		Monster monster = REPO.find(9);

		assertEquals(9, monster.getId());
		assertEquals("The Winged Dragon of Ra", monster.getName());
		assertEquals(10, monster.getLevel());
		assertNull(monster.getAttack());
		assertNull(monster.getDefense());

	}

	@Test
	public void deleteByLevel7Returns3() {

		int affectedRows = REPO.deleteByLevel(7);

		assertEquals(3, affectedRows);

	}

	// NOTE: HsqldbServer must be restarted in order to re-run this test
	@Test
	public void addLordOfDragonsReturns10() {

		Monster monster = new Monster();
		monster.setName("Lord of Dragons");
		monster.setLevel(4);
		monster.setAttack(1200);
		monster.setDefense(1100);

		int id = REPO.add(monster);

		assertEquals(10, id);

	}

	@Test
	public void findAllReturns10Monsters() {

		Monster[] monsters = REPO.findAll();

		assertEquals(9, monsters.length);

	}

	@Test
	public void countMonstersByFirstLevel1Returns1() {

		int count = REPO.countMonstersByFirstLevel(1);

		assertEquals(1, count);

	}

	// TODO add tests using different types and accessors (array and index, array and property [must fail!], etc.)

}
