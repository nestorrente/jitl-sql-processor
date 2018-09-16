package com.nestorrente.jitl.processor.sql.yugioh;

import com.nestorrente.jitl.annotation.UseProcessor;
import com.nestorrente.jitl.annotation.param.ParamName;
import com.nestorrente.jitl.annotation.param.ParamNames;
import com.nestorrente.jitl.processor.sql.SQLProcessor;
import com.nestorrente.jitl.processor.sql.annotation.AffectedRows;
import com.nestorrente.jitl.processor.sql.annotation.GeneratedKeys;

@UseProcessor(SQLProcessor.class)
public interface TrainersRepository {

	@GeneratedKeys
	int add(@ParamName("name") String name);

	String getName(@ParamName("id") int id);

	@AffectedRows
	@ParamNames({ "id", "name" })
	int update(int id, String name);

	@AffectedRows
	void delete(@ParamName("id") int id);

}
