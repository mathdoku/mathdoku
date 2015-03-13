package csvimporter;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.database.DataType;

/**
 * This class imports records from a csv file into a database table. The csv file must adhere to rules below:
 * <p/>
 * 1. Rows starting with "//" are handled as comments and will be ignored. Also empty rows will be ignored.
 * <p/>
 * 2. Columns in the csv file must be in same order as fields in table.
 * <p/>
 * 3. Data type of values must match with data type in table.
 * <p/>
 * 4. Additional columns at end are ignored.
 * <p/>
 * Instructions for manually exporting all MathDoku+ sqlite tables to a csv file which can be read by this class. After
 * opening the SQLite-database, enter the commands below:
 * <p/>
 * <pre>
 *  .headers on
 *  .mode csv
 *  .separator #
 *  .output grid.csv
 *  select * from grid;
 *  .output statistics.csv
 *  select * from statistics;
 *  .output leaderboard_rank.csv
 *  select * from leaderboard_rank;
 *  .output solving_attempt.csv
 *  select * from solving_attempt;
 *  .quit
 * </pre>
 */
public class DatabaseCsvImporter extends CsvImporter {
    private final String tableName;
    private final DataType[] columnTypes;
    private final SQLiteDatabase sqliteDatabase;

    public DatabaseCsvImporter(String fileName, DatabaseAdapter databaseAdapter) {
        super(fileName, getColumnNames(databaseAdapter));
        sqliteDatabase = DatabaseHelper.getInstance()
                .getWritableDatabase();
        tableName = databaseAdapter.getTableName();
        columnTypes = databaseAdapter.getDatabaseTableDefinition()
                .getColumnTypes();
    }

    private static String[] getColumnNames(DatabaseAdapter databaseAdapter) {
        if (databaseAdapter == null) {
            throw new CsvImporterException("Parameter database adapter should not be null.");
        }
        return databaseAdapter.getDatabaseTableDefinition()
                .getColumnNames();
    }

    protected void importLine(String line) {
        String[] values = getValuesFromLine(line);

        if (values.length < columnNames.length) {
            throw new CsvImporterException(String.format(
                    "Line %d below contains to few values. Expected at least %d " + "values while " + "%s are found" +
                            ".\nLine: %s",
                    getLinesRead(), columnNames.length, values.length, line));
        }

        ContentValues contentValues = new ContentValues();
        for (int i = 0; i < columnNames.length; i++) {
            try {
                switch (columnTypes[i]) {
                    case INTEGER:
                        contentValues.put(columnNames[i], Integer.valueOf(values[i]));
                        break;
                    case LONG:
                        contentValues.put(columnNames[i], Long.valueOf(values[i]));
                        break;
                    default:
                        contentValues.put(columnNames[i], values[i]);
                        break;
                }
            } catch (NumberFormatException e) {
                throw new CsvImporterException(
                        String.format("Value '%s' on line %d is not a valid %s value for column " + "" + "'%s'.",
                                      values[i], getLinesRead(), columnTypes[i].toString(), columnNames[i]), e);
            } catch (Exception e) {
                throw new CsvImporterException(
                        String.format("Error while processing value '%s' on line %d for %s " + "column " + "'%s'.",
                                      values[i], getLinesRead(), columnTypes[i].toString(), columnNames[i]), e);
            }
        }

        sqliteDatabase.insertOrThrow(tableName, null, contentValues);
    }
}
