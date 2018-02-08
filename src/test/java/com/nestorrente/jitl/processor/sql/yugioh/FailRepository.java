package com.nestorrente.jitl.processor.sql.yugioh;

import com.nestorrente.jitl.annotation.InlineTemplate;
import com.nestorrente.jitl.annotation.UseProcessor;
import com.nestorrente.jitl.annotation.param.Param;
import com.nestorrente.jitl.processor.sql.SQLProcessor;

@UseProcessor(SQLProcessor.class)
public interface FailRepository {

	@InlineTemplate("SELECT COUNT(*) FROM \"monsters\" WHERE \"level\" = :levels.first")
	int arrayAccessByProperty(@Param("levels") int... levels);

	@InlineTemplate("SELECT COUNT(*) FROM \"monsters\" WHERE \"level\" = :monster[0]")
	int pojoAccessByIndex(@Param("monster") Monster monster);

}
