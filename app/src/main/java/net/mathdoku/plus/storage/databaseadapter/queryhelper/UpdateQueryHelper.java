package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.util.ParameterValidator;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.List;

public class UpdateQueryHelper extends QueryHelper {
	private final String tableName;
	private List<String> columnsToSet;
	private ConditionQueryHelper whereConditionQueryHelper;

	public UpdateQueryHelper(String tableName) {
		this.tableName = tableName;
		columnsToSet = new ArrayList<String>();
	}

	/**
	 * Set the column to the given value.
	 * 
	 * @param column
	 *            The column to be set.
	 * @param value
	 *            The value to be set. Null is allowed to clear the column.
	 */
	public void setColumnToValue(String column, String value) {
		columnsToSet.add(getFieldOperatorValue(column, EQUALS_OPERATOR, value));
	}

	/**
	 * Set the column to the value which is derived with the given SQL
	 * derivation statement (right hand side of equation only).
	 * 
	 * @param column
	 *            The column to be set.
	 * @param sqlStatement
	 *            The value to be set. Null is allowed to clear the column.
	 */
	public void setColumnToStatement(String column, String sqlStatement) {
		columnsToSet.add(getFieldOperatorEscapedString(column, EQUALS_OPERATOR,
				sqlStatement));
	}

	/**
	 * Convenience method to set the column to 'null'.
	 * 
	 * @param column
	 *            The column to be set.
	 */
	public void setColumnToNull(String column) {
		setColumnToValue(column, null);
	}

	public void setWhereCondition(ConditionQueryHelper conditionQueryHelper) {
		ParameterValidator.validateNotNull(conditionQueryHelper);
		this.whereConditionQueryHelper = conditionQueryHelper;
	}

	@Override
	public String toString() {
		if (Util.isListNullOrEmpty(columnsToSet)) {
			throw new IllegalStateException(
					"At least one column has to be set.");
		}
		query.append("UPDATE ");
		query.append(tableName);
		query.append(" SET ");
		query.append(joinStringsSeparatedWith(columnsToSet, COMMA));
		if (whereConditionQueryHelper != null) {
			query.append(" WHERE ");
			query.append(whereConditionQueryHelper.toString());
		}
		return super.toString();
	}
}
