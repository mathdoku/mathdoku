package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.ParameterValidator;

class FieldOperatorValue {
	private final String field;
	private final String operator;
	private final String value;

	public FieldOperatorValue(String field, String operator, String value) {
		ParameterValidator.validateNotNullOrEmpty(field);
		ParameterValidator.validateNotNullOrEmpty(operator);
		ParameterValidator.validateNotNullOrEmpty(value);
		this.field = field;
		this.operator = operator;
		this.value = value;
	}

	public FieldOperatorValue(String field, String operator, boolean value) {
		this(field, operator, DatabaseUtil.toQuotedSQLiteString(value));
	}

	public FieldOperatorValue(String field, String operator, int value) {
		this(field, operator, String.valueOf(value));
	}
}
