package net.mathdoku.plus.storage.databaseadapter.database;

import net.mathdoku.plus.util.ParameterValidator;

import java.util.HashMap;

public class DatabaseProjection extends HashMap<String, String> {

    private static final long serialVersionUID = 12382389483847823L;

    public enum Aggregation {
        MIN,
        MAX,
        AVG,
        SUM,
        COUNT,
        COUNTIF_TRUE;

        public String getAggregationColumnNameForColumn(String sourceColumn) {
            ParameterValidator.validateNotNullOrEmpty(sourceColumn);

            return toString() + "_" + sourceColumn;
        }
    }

    public DatabaseProjection() {
        super();
    }

    /**
     * Add a column to the projection. Preferred usage is {@link #put(String, String, String)} or {@link
     * #put(DatabaseProjection.Aggregation, String, String)} .
     * <p/>
     * Note: In contradiction to other put functions, the target and source columns are not back ticked. If appropriate
     * this should have been done in or before call to this method.
     *
     * @param targetColumn
     *         The name of the target column. Its name should be unique for this projection. This is not checked by this
     *         method.
     * @param sourceColumn
     *         Name of column on which the aggregation function will be applied to determine the value of the target
     *         column.
     * @return If constructed value ("[sourceColumn] AS [ `targetColumn`]") already exists, its value is returned. If
     * the value does not yet exists, null is returned.
     */
    @Override
    public String put(String targetColumn, String sourceColumn) {
        ParameterValidator.validateNotNullOrEmpty(targetColumn);
        ParameterValidator.validateNotNullOrEmpty(sourceColumn);
        return super.put(targetColumn, sourceColumn + " AS " + DatabaseUtil.stringBetweenBackTicks(targetColumn));
    }

    /**
     * Add a column to the projection.
     *
     * @param targetColumn
     *         The name of the target column. Its name should be unique for this projection. This is not checked by this
     *         method.
     * @param sourceTable
     *         Name of table in which the source column can be found.
     * @param sourceColumn
     *         Name of column on which the aggregation function will be applied to determine the value of the target
     *         column.
     * @return If constructed value ("[`sourceTable`.`sourceColumn`] AS [ `targetColumn`]") already exists, its value is
     * returned. If the value does not yet exists, null is returned.
     */
    @SuppressWarnings("UnusedReturnValue")
    public String put(String targetColumn, String sourceTable, String sourceColumn) {
        ParameterValidator.validateNotNullOrEmpty(targetColumn);
        ParameterValidator.validateNotNullOrEmpty(sourceTable);
        ParameterValidator.validateNotNullOrEmpty(sourceColumn);

        String target = DatabaseUtil.stringBetweenBackTicks(targetColumn);
        String source = DatabaseUtil.stringBetweenBackTicks(sourceTable) + "." + DatabaseUtil.stringBetweenBackTicks(
                sourceColumn);

        return super.put(targetColumn, source + " AS " + target);
    }

    /**
     * Add a column which is constructed using an aggregated SQL function to the projection.
     *
     * @param aggregation
     *         The aggregation to be used.
     * @param sourceTable
     *         Name of table in which the source column can be found.
     * @param sourceColumn
     *         Name of column on which the aggregation function will be applied to determine the value of the target
     *         column. Can be NULL for aggregation COUNT.
     * @return If constructed value already exists, its value is returned. If the value does not yet exists, null is
     * returned.
     */
    public String put(Aggregation aggregation, String sourceTable, String sourceColumn) {
        ParameterValidator.validateNotNull(aggregation);
        ParameterValidator.validateNotNullOrEmpty(sourceTable);
        ParameterValidator.validateNotNullOrEmpty(sourceColumn);

        String targetKey = aggregation.getAggregationColumnNameForColumn(sourceColumn);
        String target = DatabaseUtil.stringBetweenBackTicks(targetKey);
        String source = DatabaseUtil.stringBetweenBackTicks(sourceTable) + "." + DatabaseUtil.stringBetweenBackTicks(
                sourceColumn);

        String aggregatedSource;
        switch (aggregation) {
            case COUNT:
                aggregatedSource = "COUNT(1)";
                break;
            case COUNTIF_TRUE:
                aggregatedSource = "SUM(CASE WHEN " + source + " = 'true' THEN 1 ELSE 0 END)";
                break;
            default:
                aggregatedSource = aggregation.toString() + "(" + source + ")";
                break;
        }

        return super.put(targetKey, aggregatedSource + " AS " + target);
    }

    /**
     * Get a column names as defined in this projection.
     *
     * @return An array of strings containing all column names defined in this projection.
     */
    public String[] getAllColumnNames() {
        return keySet().toArray(new String[size()]);
    }
}
