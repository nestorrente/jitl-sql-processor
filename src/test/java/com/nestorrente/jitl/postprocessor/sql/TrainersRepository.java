package com.nestorrente.jitl.postprocessor.sql;

import com.nestorrente.jitl.annotation.Param;
import com.nestorrente.jitl.annotation.Params;
import com.nestorrente.jitl.annotation.PostProcessor;
import com.nestorrente.jitl.postprocessor.sql.annotation.AffectedRows;
import com.nestorrente.jitl.postprocessor.sql.annotation.GeneratedKeys;

@PostProcessor(SQLPostProcessor.class)
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
