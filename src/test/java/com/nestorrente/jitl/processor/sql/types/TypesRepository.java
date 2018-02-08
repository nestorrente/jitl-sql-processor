package com.nestorrente.jitl.processor.sql.types;

import com.nestorrente.jitl.annotation.InlineTemplate;
import com.nestorrente.jitl.annotation.UseProcessor;
import com.nestorrente.jitl.annotation.param.Params;
import com.nestorrente.jitl.processor.sql.SQLProcessor;

import java.util.Date;
import java.util.Map;

@UseProcessor(SQLProcessor.class)
public interface TypesRepository {

	static final String SELECT_COLUMN_FROM_TABLE_WHERE_ID_QUERY = "SELECT \"$column\" FROM \"$table\" WHERE \"id\" = :id";

	@InlineTemplate("SELECT * FROM \"text_types_table\"")
	TextTypesPojo[] getTypes();

	@InlineTemplate(SELECT_COLUMN_FROM_TABLE_WHERE_ID_QUERY)
	@Params({ "column", "table", "id" })
	char getChar(String column, String table, int rowId);

	@InlineTemplate(SELECT_COLUMN_FROM_TABLE_WHERE_ID_QUERY)
	@Params({ "column", "table", "id" })
	Character getCharacter(String column, String table, int rowId);

	@InlineTemplate(SELECT_COLUMN_FROM_TABLE_WHERE_ID_QUERY)
	@Params({ "column", "table", "id" })
	Date getDate(String column, String table, int rowId);

	@InlineTemplate(SELECT_COLUMN_FROM_TABLE_WHERE_ID_QUERY)
	@Params({ "column", "table", "id" })
	java.sql.Date getSqlDate(String column, String table, int rowId);

	@InlineTemplate(SELECT_COLUMN_FROM_TABLE_WHERE_ID_QUERY)
	@Params({ "column", "table", "id" })
	java.sql.Timestamp getSqlTimestamp(String column, String table, int rowId);

	@InlineTemplate(SELECT_COLUMN_FROM_TABLE_WHERE_ID_QUERY)
	@Params({ "column", "table", "id" })
	java.sql.Time getSqlTime(String column, String table, int rowId);

	@InlineTemplate("SELECT * FROM \"$table\" WHERE \"id\" = :id")
	@Params({ "table", "id" })
	Map<String, Object> getFullRow(String table, int rowId);

}
