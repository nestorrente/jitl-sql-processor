package com.nestorrente.jitl.processor.sql.yugioh;

import com.nestorrente.jitl.annotation.UseProcessor;
import com.nestorrente.jitl.annotation.param.Param;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.annotation.AffectedRows;
import com.nestorrente.jitl.processor.sql.annotation.GeneratedKeys;

import java.util.List;
import java.util.Map;

@UseProcessor(SQLProcessor.class)
public interface MonstersRepository {

	Monster find(@Param("id") int id);

	Map<String, Object> findAsMap(@Param("id") int id);

	@AffectedRows
	int deleteByLevel(@Param("level") int level);

	List<Monster> findAllByLevel(@Param("level") int level);

	Iterable<Monster> findAllWithAttackGreaterOrEqualsThan(@Param("attack") int attack);

	Monster[] findAll();

	@GeneratedKeys
	int add(@Param("monster") Monster monster);

	int countMonstersByFirstLevel(@Param("levels") int... levels);

}
