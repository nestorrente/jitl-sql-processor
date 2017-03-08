package com.nestorrente.jitl.module.sql;

import com.nestorrente.jitl.annotation.InlineTemplate;
import com.nestorrente.jitl.annotation.Module;
import com.nestorrente.jitl.annotation.Param;
import com.nestorrente.jitl.annotation.Params;
import com.nestorrente.jitl.module.sql.annotation.AffectedRows;
import com.nestorrente.jitl.module.sql.annotation.GeneratedKeys;

@Module(SQLModule.class)
public interface Repository {

	@InlineTemplate("INSERT INTO pirolo(name) VALUES (:name);")
	@GeneratedKeys
	int add(@Param("name") String name);

	@InlineTemplate("SELECT name FROM pirolo WHERE id = :id;")
	String getName(@Param("id") int id);

	@InlineTemplate("UPDATE pirolo SET name = :name WHERE id = :id;")
	@AffectedRows
	@Params({ "id", "name" })
	int update(int id, String name);

	@InlineTemplate("DELETE FROM pirolo WHERE id > 3;")
	@AffectedRows // no effect in statements that returns void
	void delete();

}
