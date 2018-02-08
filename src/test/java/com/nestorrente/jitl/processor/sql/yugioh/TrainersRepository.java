package com.nestorrente.jitl.processor.sql.yugioh;

import com.nestorrente.jitl.annotation.UseProcessor;
import com.nestorrente.jitl.annotation.param.Param;
import com.nestorrente.jitl.annotation.param.Params;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.annotation.AffectedRows;
import com.nestorrente.jitl.processor.sql.annotation.GeneratedKeys;

@UseProcessor(SQLProcessor.class)
public interface TrainersRepository {

	@GeneratedKeys
	int add(@Param("name") String name);

	String getName(@Param("id") int id);

	@AffectedRows
	@Params({ "id", "name" })
	int update(int id, String name);

	@AffectedRows
	void delete(@Param("id") int id);

}
