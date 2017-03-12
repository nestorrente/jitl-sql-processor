package com.nestorrente.jitl.module.sql;

import com.nestorrente.jitl.annotation.Param;
import com.nestorrente.jitl.annotation.Params;
import com.nestorrente.jitl.annotation.UseModule;
import com.nestorrente.jitl.module.sql.SQLModule;
import com.nestorrente.jitl.module.sql.annotation.AffectedRows;
import com.nestorrente.jitl.module.sql.annotation.GeneratedKeys;

@UseModule(SQLModule.class)
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
