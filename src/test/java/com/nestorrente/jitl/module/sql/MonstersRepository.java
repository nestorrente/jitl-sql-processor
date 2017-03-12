package com.nestorrente.jitl.module.sql;

import com.nestorrente.jitl.annotation.Param;
import com.nestorrente.jitl.annotation.UseModule;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.annotation.AffectedRows;

@UseModule(SQLModule.class)
public interface MonstersRepository {

	Monster find(@Param("id") int id);

	@AffectedRows
	int deleteByLevel(@Param("level") int level);

}
