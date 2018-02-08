package com.nestorrente.jitl.processor.sql;

import java.util.Collection;

class ParsedQueryData {

	private final String sqlCode;
	private final Collection<Object> parametersValues;

	public ParsedQueryData(String sqlCode, Collection<Object> parametersValues) {
		this.sqlCode = sqlCode;
		this.parametersValues = parametersValues;
	}

	public String getSqlCode() {
		return this.sqlCode;
	}

	public Collection<Object> getParametersValues() {
		return this.parametersValues;
	}

}
