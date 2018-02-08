package com.nestorrente.jitl.processor.sql.yugioh;

public class Monster {

	private int id;
	private String name;
	private int level;
	private Integer attack;
	private Integer defense;

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public int getLevel() {
		return this.level;
	}

	public Integer getAttack() {
		return this.attack;
	}

	public Integer getDefense() {
		return this.defense;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public void setAttack(int attack) {
		this.attack = attack;
	}

	public void setDefense(int defense) {
		this.defense = defense;
	}

	@Override
	public String toString() {
		return "Monster [id=" + this.id + ", name=" + this.name + ", level=" + this.level + ", attack=" + this.attack + ", defense=" + this.defense + "]";
	}

}
