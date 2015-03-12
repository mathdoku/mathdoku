package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.util.ParameterValidator;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.List;

public class UpdateQueryHelper {
    private final String tableName;
    private List<String> columnsToSet;
    private ConditionList whereConditionQueryHelper;

    public UpdateQueryHelper(String tableName) {
        this.tableName = tableName;
        columnsToSet = new ArrayList<String>();
    }

    /**
     * Set the column to the given value.
     *
     * @param column
     *         The column to be set.
     * @param value
     *         The value to be set. Null is allowed to clear the column.
     */
    public void setColumnToValue(String column, String value) {
        columnsToSet.add(new FieldOperatorStringValue(column, FieldOperatorValue.Operator.EQUALS,
                                                      value).toString());
    }

    /**
     * Set the column to the value which is derived with the given SQL derivation statement (right
     * hand side of equation only).
     *
     * @param column
     *         The column to be set.
     * @param sqlStatement
     *         The value to be set. Null is allowed to clear the column.
     */
    public void setColumnToStatement(String column, String sqlStatement) {
        columnsToSet.add(new FieldOperatorValue(column, FieldOperatorValue.Operator.EQUALS,
                                                sqlStatement).toString());
    }

    /**
     * Convenience method to set the column to 'null'.
     *
     * @param column
     *         The column to be set.
     */
    public void setColumnToNull(String column) {
        setColumnToValue(column, null);
    }

    public void setWhereCondition(ConditionList conditionList) {
        ParameterValidator.validateNotNull(conditionList);
        this.whereConditionQueryHelper = conditionList;
    }

    @Override
    public String toString() {
        if (Util.isListNullOrEmpty(columnsToSet)) {
            throw new IllegalStateException("At least one column has to be set.");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UPDATE ");
        stringBuilder.append(tableName);
        stringBuilder.append(" SET ");
        stringBuilder.append(QueryHelper.join(QueryHelper.COMMA, columnsToSet));
        if (whereConditionQueryHelper != null) {
            stringBuilder.append(" WHERE ");
            stringBuilder.append(whereConditionQueryHelper.toString());
        }
        return stringBuilder.toString();
    }
}
