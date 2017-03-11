package com.nestorrente.jitl.postprocessor.sql;

import org.hsqldb.server.Server;

public class HsqldbServer {

	public static void main(String[] args) {
		Server.main(new String[] { "--database.0", "res:/com/nestorrente/jitl/postprocessor/sql/database/yugioh", "--dbname.0", "yugioh" });
	}

}
