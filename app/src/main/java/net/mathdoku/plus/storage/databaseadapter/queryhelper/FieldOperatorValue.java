package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.ParameterValidator;

/**
 * This class represents a field with an unquoted string value. Use class FieldOperatorStringValue in case the string
 * value on the right hand side has to be surrounded with quotes.
 */
public class FieldOperatorValue implements ConditionListElement {
    protected static final String SPACE = " ";
    protected static final String NULL_VALUE = "null";
    private static final String AND = "AND";

    private final String field;
    private final Operator operator;
    private final String value;

    public enum Operator {
        EQUALS("="),
        NOT_EQUALS("<>"),
        LESS_THAN("<"),
        BETWEEN("BETWEEN");

        private String sqliteOperator;

        Operator(String sqliteOperator) {
            this.sqliteOperator = sqliteOperator;
        }

        public String getSqliteOperator() {
            return sqliteOperator;
        }
    }

    /**
     * Constructor for all operators except BETWEEN.
     *
     * @param field
     *         The name of the field.
     * @param operator
     *         The comparison operator.
     * @param value
     *         The value with which the field is compared.
     */
    public FieldOperatorValue(String field, Operator operator, String value) {
        ParameterValidator.validateNotNullOrEmpty(field);
        ParameterValidator.validateNotNull(operator);
        ParameterValidator.validateNotNullOrEmpty(value);
        if (operator == Operator.BETWEEN) {
            throw new IllegalArgumentException("Operator 'BETWEEN' can not be used in this context.");
        }
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Constructor for operators BETWEEN.
     *
     * @param field
     *         The name of the field.
     * @param lowValue
     *         The lower value with which the field is compared.
     * @param highValue
     *         The higher value with which the field is compared.
     */
    protected FieldOperatorValue(String field, String lowValue, String highValue) {
        ParameterValidator.validateNotNullOrEmpty(field);
        ParameterValidator.validateNotNullOrEmpty(lowValue);
        ParameterValidator.validateNotNullOrEmpty(highValue);
        this.field = field;
        this.operator = Operator.BETWEEN;
        this.value = new StringBuilder().append(lowValue)
                .append(SPACE)
                .append(AND)
                .append(SPACE)
                .append(highValue)
                .toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(DatabaseUtil.stringBetweenBackTicks(field));
        stringBuilder.append(SPACE);
        stringBuilder.append(operator.getSqliteOperator());
        stringBuilder.append(SPACE);
        stringBuilder.append(value);
        return stringBuilder.toString();
    }
}
