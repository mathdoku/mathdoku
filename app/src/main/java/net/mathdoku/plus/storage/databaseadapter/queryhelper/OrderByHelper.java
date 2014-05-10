package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.List;

public class OrderByHelper extends QueryHelper {
	private static final String SORT_ASCENDING = "ASC";
	private static final String SORT_DESCENDING = "DESC";

	private List<String> orderByColumns;

	public OrderByHelper() {
		orderByColumns = new ArrayList<String>();
	}

	/**
	 * Add a column to the list of columns on which is sorted in ascending
	 * order.
	 * 
	 * @param column
	 *            The column to be added
	 */
	public OrderByHelper sortAscending(String column) {
		addOrderByColumn(column, SORT_ASCENDING);

		return this;
	}

	private void addOrderByColumn(String column, String sortOrder) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(DatabaseUtil.stringBetweenBackTicks(column));
		stringBuilder.append(SPACE);
		stringBuilder.append(sortOrder);
		orderByColumns.add(stringBuilder.toString());
	}

	/**
	 * Add a column to the list of columns on which is sorted in descending
	 * order.
	 * 
	 * @param column
	 *            The column to be added
	 */
	public OrderByHelper sortDescending(String column) {
		addOrderByColumn(column, SORT_DESCENDING);

		return this;
	}

	@Override
	public String toString() {
		if (Util.isListNullOrEmpty(orderByColumns)) {
			throw new IllegalStateException(
					"At least one column has to be set.");
		}
		query.append(joinStringsSeparatedWith(orderByColumns, COMMA));
		return super.toString();
	}
}
