package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.List;

public class UpdateQueryHelper extends QueryHelper {
	private final String tableName;
	private List<String> columnsToSet;

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
	public void setColumnTo(String column, String value) {
		columnsToSet.add(getFieldOperatorValue(column, EQUALS_OPERATOR, value));
	}

	/**
	 * Convenience method to set the column to 'null'.
	 * 
	 * @param column
	 *            The column to be set.
	 */
	public void setColumnToNull(String column) {
		setColumnTo(column, null);
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
		return super.toString();
	}
}
