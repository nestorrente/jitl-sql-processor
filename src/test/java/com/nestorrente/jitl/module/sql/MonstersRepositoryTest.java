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
import com.nestorrente.jitl.annotation.UseModule;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@UseModule(SQLModule.class)
public class MonstersRepositoryTest {

	private static Connection CONNECTION;
	private static MonstersRepository REPO;

	@BeforeClass
	public static void openConnectionAndCreateRepository() throws ClassNotFoundException, SQLException {

		Class.forName("org.hsqldb.jdbc.JDBCDriver");

		CONNECTION = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/yugioh", "SA", "");
		CONNECTION.setAutoCommit(false);

		Jitl jitl = Jitl.builder()
			.addModule(
				SQLModule.builder(CONNECTION)
					.build())
			.build();

		REPO = jitl.getInstance(MonstersRepository.class);

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
	public void find5ReturnsSummonedSkull() {

		Monster monster = REPO.find(5);

		assertEquals(5, monster.getId());
		assertEquals("Summoned Skull", monster.getName());
		assertEquals(6, monster.getLevel());
		assertEquals(2500, monster.getAttack());
		assertEquals(1200, monster.getDefense());

	}

	@Test
	public void deleteByLevel7Returns3() {

		int affectedRows = REPO.deleteByLevel(7);

		assertEquals(3, affectedRows);

	}

	@Test
	public void addLordOfDragonsReturns9() {

		Monster monster = new Monster();
		monster.setName("Lord of Dragons");
		monster.setLevel(4);
		monster.setAttack(1200);
		monster.setDefense(1100);

		int id = REPO.add(monster);

		assertEquals(9, id);

	}

	@Test
	public void findAllReturns9Monsters() {

		Monster[] monsters = REPO.findAll();

		assertEquals(6, monsters.length);

	}

	@Test
	public void countMonstersByFirstLevel1Returns1() {

		int count = REPO.countMonstersByFirstLevel(1);

		assertEquals(1, count);

	}

	// TODO add tests using different types and accessors (array and index, array and property [must fail!], etc.)

}
