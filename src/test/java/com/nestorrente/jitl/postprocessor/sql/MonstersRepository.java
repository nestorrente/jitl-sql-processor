package com.nestorrente.jitl.postprocessor.sql;

import com.nestorrente.jitl.annotation.Param;
import com.nestorrente.jitl.annotation.PostProcessor;
import com.nestorrente.jitl.postprocessor.sql.annotation.AffectedRows;

@PostProcessor(SQLPostProcessor.class)
public interface MonstersRepository {

	Monster find(@Param("id") int id);

	@AffectedRows
	int deleteByLevel(@Param("level") int level);

}
