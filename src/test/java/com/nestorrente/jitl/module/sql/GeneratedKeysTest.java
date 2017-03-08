package com.nestorrente.jitl.module.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GeneratedKeysTest {

	public static void main(String[] args) throws Exception {

		Class.forName("com.mysql.cj.jdbc.Driver");

		try(Connection con = DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "")) {

			con.setAutoCommit(false);

			PreparedStatement stmt = con.prepareStatement("INSERT INTO pirolo(name) VALUES (?), (?);", PreparedStatement.RETURN_GENERATED_KEYS);
			stmt.setString(1, "Pirolítico");
			stmt.setString(2, "Pepitólogo");

			System.out.println("INSERTED " + stmt.executeUpdate() + " ROWS");

			ResultSet gks = stmt.getGeneratedKeys();

			while(gks.next()) {
				System.out.println("Generated ID: " + gks.getInt(1));
			}

			PreparedStatement stmt2 = con.prepareStatement("DELETE FROM pirolo WHERE name IN (?, ?);", PreparedStatement.RETURN_GENERATED_KEYS);
			stmt2.setString(1, "Pirolítico");
			stmt2.setString(2, "Pepitólogo");

			System.out.println("DELETED " + stmt2.executeUpdate() + " ROWS");

			con.commit();

		}

	}

}
