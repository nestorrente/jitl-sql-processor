package com.nestorrente.jitl.module.sql.yugioh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class MonstersRepositoryTest extends YugiohDatabaseTest {

	private static MonstersRepository REPOSITORY;

	@BeforeClass
	public static void setUpRepository() {
		REPOSITORY = JITL.getInstance(MonstersRepository.class);
	}

	@Test
	public void find5ReturnsSummonedSkull() {

		Monster monster = REPOSITORY.find(5);

		assertEquals(5, monster.getId());
		assertEquals("Summoned Skull", monster.getName());
		assertEquals(6, monster.getLevel());
		assertEquals(2500, monster.getAttack().intValue());
		assertEquals(1200, monster.getDefense().intValue());

	}

	@Test
	public void find9ReturnsTheWingedDragonOfRaWithNullAttackAndDefense() {

		Monster monster = REPOSITORY.find(9);

		assertEquals(9, monster.getId());
		assertEquals("The Winged Dragon of Ra", monster.getName());
		assertEquals(10, monster.getLevel());
		assertNull(monster.getAttack());
		assertNull(monster.getDefense());

	}

	@Test
	public void deleteByLevel7Returns3() {

		int affectedRows = REPOSITORY.deleteByLevel(7);

		assertEquals(3, affectedRows);

	}

	@Test
	public void addLordOfDragonsReturns10() {

		Monster monster = new Monster();
		monster.setName("Lord of Dragons");
		monster.setLevel(4);
		monster.setAttack(1200);
		monster.setDefense(1100);

		int id = REPOSITORY.add(monster);

		assertEquals(10, id);

	}

	@Test
	public void findAllReturns9Monsters() {

		Monster[] monsters = REPOSITORY.findAll();

		assertEquals(9, monsters.length);

	}

	@Test
	public void findAllWithAttackGreaterOrEqualsThanReceiving2500Returns4Monsters() {

		Iterable<Monster> monsters = REPOSITORY.findAllWithAttackGreaterOrEqualsThan(2500);

		String[] names = {
				"Blue-Eyes White Dragon",
				"Dark Magician",
				"Summoned Skull",
				"Obelisk the Tormentor"
		};

		int total = 0;
		for(Monster monster : monsters) {

			assertTrue(total < 4);

			String expectedName = names[total];

			assertEquals(expectedName, monster.getName());

			++total;

		}

	}

	@Test
	public void countMonstersByFirstLevel1Returns1() {

		int count = REPOSITORY.countMonstersByFirstLevel(1);

		assertEquals(1, count);

	}

}
