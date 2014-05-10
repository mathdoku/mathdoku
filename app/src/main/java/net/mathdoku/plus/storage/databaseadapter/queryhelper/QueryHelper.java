package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import android.text.TextUtils;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.ParameterValidator;

import java.util.List;

public abstract class QueryHelper {
	protected static final String COMMA = ",";
	protected static final String SPACE = " ";
	protected static final String EQUALS_OPERATOR = "=";
	protected static final String NULL_VALUE = "null";

	protected StringBuilder query;

	public QueryHelper() {
		query = new StringBuilder();
	}

	protected static String getFieldOperatorValue(String field,
			String operator, String value) {
		return getFieldOperatorEscapedString(
				field,
				operator,
				value == null ? NULL_VALUE : DatabaseUtil
						.stringBetweenQuotes(value));
	}

	private static String getFieldOperatorEscapedString(String field,
			String operator, String value) {
		ParameterValidator.validateNotNullOrEmpty(field);
		ParameterValidator.validateNotNullOrEmpty(operator);
		ParameterValidator.validateNotNullOrEmpty(value);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(DatabaseUtil.stringBetweenBackTicks(field));
		stringBuilder.append(SPACE);
		stringBuilder.append(operator);
		stringBuilder.append(SPACE);
		stringBuilder.append(value);
		return stringBuilder.toString();
	}

	public static String getFieldEqualsValue(String column, String value) {
		return getFieldOperatorValue(column, EQUALS_OPERATOR, value);
	}

	public static String getFieldEqualsValue(String column, int value) {
		return getFieldOperatorEscapedString(column, EQUALS_OPERATOR,
				String.valueOf(value));
	}

	public String toString() {
		return query.toString();
	}

	protected static String joinStringsSeparatedWith(List<String> strings,
			String separator) {
		ParameterValidator.validateNotNullOrEmpty(strings);
		ParameterValidator.validateNotNullOrEmpty(separator);

		return TextUtils
				.join(surroundWithSpaceIfApplicable(separator), strings);
	}

	private static String surroundWithSpaceIfApplicable(String separator) {
		StringBuilder stringBuilder = new StringBuilder();

		if (!separator.equals(COMMA)) {
			stringBuilder.append(SPACE);
		}
		stringBuilder.append(separator);
		stringBuilder.append(SPACE);

		return stringBuilder.toString();
	}
}
