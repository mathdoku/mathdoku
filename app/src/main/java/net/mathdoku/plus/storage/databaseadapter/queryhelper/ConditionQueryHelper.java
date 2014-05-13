package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.util.ParameterValidator;

import java.util.ArrayList;
import java.util.List;

public class ConditionQueryHelper extends QueryHelper {
	private static final String AND_OPERATOR = "AND";
	private static final String OR_OPERATOR = "OR";
	private static final String LESS_THAN_OPERATOR = "<";

	private List<String> operands;
	private String operator;

	public ConditionQueryHelper() {
		super();
		operands = new ArrayList<String>();
	}

	public static String getFieldLessThanValue(String column, String value) {
		return getFieldOperatorValue(column, LESS_THAN_OPERATOR, value);
	}

	public static String getFieldBetweenValues(String column, int minValue,
											   int maxValue) {
		ParameterValidator.validateNotNullOrEmpty(column);
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(column);
		stringBuilder.append(" BETWEEN ");
		stringBuilder.append(minValue);
		stringBuilder.append(SPACE);
		stringBuilder.append(AND_OPERATOR);
		stringBuilder.append(SPACE);
		stringBuilder.append(maxValue);
		return stringBuilder.toString();
	}

	public ConditionQueryHelper addOperand(String operand) {
		ParameterValidator.validateNotNullOrEmpty(operand);
		operands.add(operand);

		return this;
	}

	public ConditionQueryHelper setAndOperator() {
		validateNumberOfOperands();
		operator = AND_OPERATOR;

		return this;
	}

	private void validateNumberOfOperands() {
		if (operands.size() <= 1) {
			throw new IllegalStateException("At least two operands expected.");
		}
	}

	public ConditionQueryHelper setOrOperator() {
		validateNumberOfOperands();
		operator = OR_OPERATOR;

		return this;
	}

	@Override
	public String toString() {
		if (operands.size() < 1) {
			throw new IllegalStateException("At least one operand expected.");
		}
		if (operands.size() == 1) {
			query.append(operands.get(0));
		} else {
			query.append(" (");
			query.append(joinStringsSeparatedWith(operands, operator));
			query.append(")");
		}

		return query.toString();
	}
}
