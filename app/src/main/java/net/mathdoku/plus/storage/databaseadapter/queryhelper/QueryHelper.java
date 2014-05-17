package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import android.text.TextUtils;

import net.mathdoku.plus.util.ParameterValidator;

import org.jetbrains.annotations.NotNull;

import java.util.List;

class QueryHelper {
	protected static final String SPACE = " ";
	protected static final String COMMA = ",";
	protected static final String PARENTHESES_LEFT = "(";
	protected static final String PARENTHESES_RIGHT = ")";

	private QueryHelper() {
		// Prevent instantiation of utility class.
	}

	static String join(String separator, @NotNull List<?> list) {
		ParameterValidator.validateNotNullOrEmpty(list);
		ParameterValidator.validateNotNullOrEmpty(separator);

		return TextUtils.join(surroundWithSpaceIfApplicable(separator), list);
	}

	static String joinStringsSeparatedWith(List<String> queryElements,
			String separator) {
		ParameterValidator.validateNotNullOrEmpty(queryElements);
		ParameterValidator.validateNotNullOrEmpty(separator);

		return TextUtils.join(surroundWithSpaceIfApplicable(separator), queryElements);
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

	public static String joinBetweenParentheses(
			String operatorBetweenConditions,
			List<ConditionListElement> conditionListElements) {
		return new StringBuilder()
				.append(SPACE)
				.append(PARENTHESES_LEFT)
				.append(join(operatorBetweenConditions, conditionListElements))
				.append(PARENTHESES_RIGHT)
				.toString();
	}
}
