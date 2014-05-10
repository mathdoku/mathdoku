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

	public void addOperand(String operand) {
		ParameterValidator.validateNotNullOrEmpty(operand);
		operands.add(operand);
	}

	public void setAndOperator() {
		validateNumberOfOperands();
		operator = AND_OPERATOR;
	}

	private void validateNumberOfOperands() {
		if (operands.size() <= 1) {
			throw new IllegalStateException("At least two operands expected.");
		}
	}

	public void setOrOperator() {
		validateNumberOfOperands();
		operator = OR_OPERATOR;
	}

	@Override
	public String toString() {
		validateNumberOfOperands();
		query.append(" (");
		query.append(joinStringsSeparatedWith(operands, operator));
		query.append(")");

		return query.toString();
	}
}
