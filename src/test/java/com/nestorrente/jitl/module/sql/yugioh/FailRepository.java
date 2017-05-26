package com.nestorrente.jitl.module.sql.yugioh;

import com.nestorrente.jitl.annotation.InlineTemplate;
import com.nestorrente.jitl.annotation.UseModule;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.annotation.Param;

@UseModule(SQLModule.class)
public interface FailRepository {

	@InlineTemplate("SELECT COUNT(*) FROM \"monsters\" WHERE \"level\" = :levels.first")
	int arrayAccessByProperty(@Param("levels") int... levels);

	@InlineTemplate("SELECT COUNT(*) FROM \"monsters\" WHERE \"level\" = :monster[0]")
	int pojoAccessByIndex(@Param("monster") Monster monster);

}
