package net.cactii.mathdoku.storage.database;

import java.util.HashMap;

public class Projection extends HashMap<String, String> {

	private static final long serialVersionUID = 12382389483847823L;

	public enum Aggregation {
		MIN, MAX, AVG, SUM, COUNT, COUNTIF_TRUE
	};

	public Projection() {
		super();
	}

	/**
	 * Add a column to the projection. Preferred usage is
	 * {@link #put(String, String, String)} or
	 * {@link #put(String, Aggregation, String, String)}.
	 * <p>
	 * Note: In contradiction to other put functions, the target and source
	 * columns are not back ticked. If appropriate this should have been done in
	 * or before call to this method.
	 * 
	 * @param targetColumn
	 *            The name of the target column. Its name should be unique for
	 *            this projection. This is not checked by this method.
	 * @param sourceColumn
	 *            Name of column on which the aggregation function will be
	 *            applied to determine the value of the target column.
	 */
	/**
	 * @return
	 */
	@Override
	public String put(String targetColumn, String sourceColumn) {
		return super.put(targetColumn, sourceColumn + " AS " + targetColumn);
	}

	/**
	 * Add a column to the projection.
	 * 
	 * @param targetColumn
	 *            The name of the target column. Its name should be unique for
	 *            this projection. This is not checked by this method.
	 * @param sourceTable
	 *            Name of table in which the source column can be found.
	 * @param sourceColumn
	 *            Name of column on which the aggregation function will be
	 *            applied to determine the value of the target column.
	 */
	public String put(String targetColumn, String sourceTable,
			String sourceColumn) {
		String target = DatabaseAdapter.stringBetweenBackTicks(targetColumn);
		String source = DatabaseAdapter.stringBetweenBackTicks(sourceTable)
				+ "." + DatabaseAdapter.stringBetweenBackTicks(sourceColumn);

		return super.put(target, source + " AS " + target);
	}

	/**
	 * Add a column which is constructed using an aggregated SQL function to the
	 * projection. The name of the target column name will be determined with
	 * method {@link #getAggregatedKey(Aggregation, String)} which can also be
	 * used to find the corresponding column in the cursor.
	 * 
	 * @param aggregation
	 *            The aggregation to be used.
	 * @param sourceTable
	 *            Name of table in which the source column can be found.
	 * @param sourceColumn
	 *            Name of column on which the aggregation function will be
	 *            applied to determine the value of the target column. Can be
	 *            NULL for aggregation COUNT.
	 */
	public void put(Aggregation aggregation, String sourceTable,
			String sourceColumn) {
		String target = DatabaseAdapter
				.stringBetweenBackTicks(getAggregatedKey(aggregation,
						sourceColumn));
		String source = DatabaseAdapter.stringBetweenBackTicks(sourceTable)
				+ "." + DatabaseAdapter.stringBetweenBackTicks(sourceColumn);

		String aggregatedSource;
		switch (aggregation) {
		case COUNT:
			aggregatedSource = "COUNT(1)";
			break;
		case COUNTIF_TRUE:
			aggregatedSource = "SUM(CASE WHEN " + source
					+ " = 'true' THEN 1 ELSE 0 END)";
			break;
		default:
			aggregatedSource = aggregation.toString() + "(" + source + ")";
		}

		super.put(target, aggregatedSource + " AS " + target);
	}

	/**
	 * Get a column names as defined in this projection.
	 * 
	 * @return An array of strings containing all column names defined in this
	 *         projection.
	 */
	public String[] getAllColumnNames() {
		return keySet().toArray(new String[size()]);
	}

	/**
	 * Gets the aggregated key for a given aggregation function and a source
	 * column.
	 * 
	 * @param aggregation
	 *            The aggregation to be used.
	 * @param sourceColumn
	 *            Name of column on which the aggregation function will be
	 *            applied.
	 * @return The aggregated key for a given aggregation function and a source
	 *         column.
	 */
	public String getAggregatedKey(Aggregation aggregation, String sourceColumn) {
		switch (aggregation) {
		case COUNT:
			return "count_rows";
		case COUNTIF_TRUE:
			return "count_" + sourceColumn;
		default:
			return aggregation.toString() + "_" + sourceColumn;
		}
	}
}