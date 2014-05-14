package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.ParameterValidator;

import java.util.ArrayList;
import java.util.List;

public class CaseWhenHelper extends QueryHelper {
	private final List<WhenThenOperand> whenThenOperands;
	private String elseValue;

	private static class WhenThenOperand {
		public ConditionQueryHelper condition;
		public String value;
	}

	public CaseWhenHelper() {
		whenThenOperands = new ArrayList<WhenThenOperand>();
	}

	public CaseWhenHelper addOperand(ConditionQueryHelper conditionQueryHelper, String value) {
		ParameterValidator.validateNotNull(conditionQueryHelper);
		ParameterValidator.validateNotNullOrEmpty(value);
		WhenThenOperand whenThenOperand = new WhenThenOperand();
		whenThenOperand.condition = conditionQueryHelper;
		whenThenOperand.value = DatabaseUtil.stringBetweenQuotes(value);
		whenThenOperands.add(whenThenOperand);

		return this;
	}

	public CaseWhenHelper setElse(String value) {
		ParameterValidator.validateNotNullOrEmpty(value);
		elseValue = DatabaseUtil.stringBetweenQuotes(value);

		return this;
	}

	@Override
	public String toString() {
		if (whenThenOperands.isEmpty()) {
			throw new IllegalStateException("At least one operand expected.");
		}

		query.append(" CASE");
		for (WhenThenOperand whenThenOperand : whenThenOperands) {
			query.append(" WHEN ");
			query.append(whenThenOperand.condition.toString());
			query.append(" THEN ");
			query.append(whenThenOperand.value);
		}
		if (elseValue != null) {
			query.append(" ELSE ");
			query.append(elseValue);
		}
		query.append(" END");

		return super.toString();
	}
}
