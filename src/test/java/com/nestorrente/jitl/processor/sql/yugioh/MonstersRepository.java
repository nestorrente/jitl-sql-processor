package com.nestorrente.jitl.processor.sql.yugioh;

import com.nestorrente.jitl.annotation.UseProcessor;
import com.nestorrente.jitl.annotation.param.ParamName;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.annotation.AffectedRows;
import com.nestorrente.jitl.processor.sql.annotation.GeneratedKeys;

import java.util.List;
import java.util.Map;

@UseProcessor(SQLProcessor.class)
public interface MonstersRepository {

	Monster find(@ParamName("id") int id);

	Map<String, Object> findAsMap(@ParamName("id") int id);

	@AffectedRows
	int deleteByLevel(@ParamName("level") int level);

	List<Monster> findAllByLevel(@ParamName("level") int level);

	Iterable<Monster> findAllWithAttackGreaterOrEqualsThan(@ParamName("attack") int attack);

	Monster[] findAll();

	@GeneratedKeys
	int add(@ParamName("monster") Monster monster);

	int countMonstersByFirstLevel(@ParamName("levels") int... levels);

}
