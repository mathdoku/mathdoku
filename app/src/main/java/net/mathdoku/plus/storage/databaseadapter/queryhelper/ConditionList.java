package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.util.ParameterValidator;

import java.util.ArrayList;
import java.util.List;

public class ConditionList implements ConditionListElement {
	private static final String AND_OPERATOR = "AND";
	private static final String OR_OPERATOR = "OR";

	private List<ConditionListElement> conditionListElements;
	private String operatorBetweenConditions;

	public ConditionList() {
		super();
		conditionListElements = new ArrayList<ConditionListElement>();
	}

	public static String getFieldLessThanValue(String column, String value) {
		return new FieldOperatorStringValue(column,
				FieldOperatorValue.Operator.LESS_THAN, value).toString();
	}

	public ConditionList addOperand(ConditionListElement conditionListElement) {
		ParameterValidator.validateNotNull(conditionListElement);
		conditionListElements.add(conditionListElement);

		return this;
	}

	public ConditionList setAndOperator() {
		validateNumberOfOperands(2);
		operatorBetweenConditions = AND_OPERATOR;

		return this;
	}

	private void validateNumberOfOperands(int minimumOperandsRequired) {
		if (conditionListElements.size() < minimumOperandsRequired) {
			throw new IllegalStateException(
					String.format("At least %d operand(s) expected.",
							minimumOperandsRequired));
		}
	}

	public ConditionList setOrOperator() {
		validateNumberOfOperands(2);
		operatorBetweenConditions = OR_OPERATOR;

		return this;
	}

	@Override
	public String toString() {
		validateNumberOfOperands(1);
		if (conditionListElements.size() == 1) {
			return conditionListElements.get(0).toString();
		} else {
			return QueryHelper.joinBetweenParentheses(
					operatorBetweenConditions, conditionListElements);
		}
	}
}
