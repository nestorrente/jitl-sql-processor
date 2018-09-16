package com.nestorrente.jitl.processor.sql.util;

import java.util.StringJoiner;

public class StringUtils {

	public static String joinRepeating(String element, String delimiter, int times) {

		StringJoiner joiner = new StringJoiner(delimiter);

		while(times-- > 0) {
			joiner.add(element);
		}

		return joiner.toString();

	}

}
