package com.nestorrente.jitl.processor.sql.util;

import java.util.StringJoiner;

public class StringUtils {

	public static String joinRepeating(String element, String delimiter, int times) {

		StringJoiner joiner = new StringJoiner(delimiter);

		for(int i = 0; i < times; ++i) {
			joiner.add(element);
		}

		return joiner.toString();

	}

}
