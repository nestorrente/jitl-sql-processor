package com.nestorrente.jitl.module.sql;

import java.util.List;
import java.util.Map;

import com.nestorrente.jitl.annotation.UseModule;
import com.nestorrente.jitl.annotation.Param;
import com.nestorrente.jitl.module.sql.annotation.AffectedRows;
import com.nestorrente.jitl.module.sql.annotation.GeneratedKeys;

@UseModule(SQLModule.class)
public interface MonstersRepository {

	Monster find(@Param("id") int id);

	Map<String, Object> findAsMap(@Param("id") int id);

	@AffectedRows
	int deleteByLevel(@Param("level") int level);

	List<Monster> findAllByLevel(@Param("level") int level);

	Monster[] findAll();

	@GeneratedKeys
	int add(@Param("monster") Monster monster);

	int countMonstersByFirstLevel(@Param("levels") int... levels);

}
