package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.ParameterValidator;

import java.util.ArrayList;
import java.util.List;

public class CaseWhenHelper {
    private final List<WhenThenOperand> whenThenOperands;
    private String elseValue;

    private static class WhenThenOperand {
        FieldOperatorValue condition;
        String value;
    }

    public CaseWhenHelper() {
        whenThenOperands = new ArrayList<WhenThenOperand>();
    }

    public CaseWhenHelper addOperand(FieldOperatorValue condition, String value) {
        ParameterValidator.validateNotNull(condition);
        ParameterValidator.validateNotNullOrEmpty(value);
        WhenThenOperand whenThenOperand = new WhenThenOperand();
        whenThenOperand.condition = condition;
        whenThenOperand.value = DatabaseUtil.stringBetweenQuotes(value);
        whenThenOperands.add(whenThenOperand);

        return this;
    }

    public CaseWhenHelper setElseStringValue(String value) {
        ParameterValidator.validateNotNullOrEmpty(value);
        elseValue = DatabaseUtil.stringBetweenQuotes(value);

        return this;
    }

    @Override
    public String toString() {
        if (whenThenOperands.isEmpty()) {
            throw new IllegalStateException("At least one operand expected.");
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(" CASE");
        for (WhenThenOperand whenThenOperand : whenThenOperands) {
            stringBuilder.append(" WHEN ");
            stringBuilder.append(whenThenOperand.condition.toString());
            stringBuilder.append(" THEN ");
            stringBuilder.append(whenThenOperand.value);
        }
        if (elseValue != null) {
            stringBuilder.append(" ELSE ");
            stringBuilder.append(elseValue);
        }
        stringBuilder.append(" END");

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CaseWhenHelper)) {
            return false;
        }

        CaseWhenHelper that = (CaseWhenHelper) o;

        if (elseValue != null ? !elseValue.equals(that.elseValue) : that.elseValue != null) {
            return false;
        }
        if (!whenThenOperands.equals(that.whenThenOperands)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = whenThenOperands.hashCode();
        result = 31 * result + (elseValue != null ? elseValue.hashCode() : 0);
        return result;
    }
}
