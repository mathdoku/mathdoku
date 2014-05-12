package databasehelper.CsvImporter;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import net.mathdoku.plus.storage.databaseadapter.DatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.storage.databaseadapter.database.DataType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import robolectric.TestRunnerHelper;

/**
 * This class imports records from a csv file into a database table. The csv
 * file must adhere to rules below:
 * 
 * 1. Rows starting with "//" are handled as comments and will be ignored. Also
 * empty rows will be ignored.
 * 
 * 2. Columns in the csv file must be in same order as fields in table.
 * 
 * 3. Data type of values must match with data type in table.
 * 
 * 4. Additional columns at end are ignored.
 */
public class CsvImporter {
	private static final String REGEXP_CSV_SEPARATOR = "\\|";
	private final String fileName;
	private final String tableName;
	private final String[] columnNames;
	private final DataType[] columnTypes;
	private final SQLiteDatabase sqliteDatabase;
	private int linesRead = 0;

	public CsvImporter(String fileName, DatabaseAdapter databaseAdapter) {
		if (fileName == null || fileName.isEmpty()) {
			throw new CsvImportException("Parameter filename should not be empty.");
		}
		if (databaseAdapter == null) {
			throw new CsvImportException("Parameter database adapter should not be null.");
		}
		this.fileName = fileName;
		sqliteDatabase = DatabaseHelper.getInstance().getWritableDatabase();
		tableName = databaseAdapter.getTableName();
		columnNames = databaseAdapter
				.getDatabaseTableDefinition()
				.getColumnNames();
		columnTypes = databaseAdapter
				.getDatabaseTableDefinition()
				.getColumnTypes();
	}

	public void importIntoDatabase() {
		InputStream inputStream = null;
		BufferedReader br = null;
		try {
			inputStream = TestRunnerHelper
					.getActivity()
					.getAssets()
					.open(fileName);
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			br = new BufferedReader(inputStreamReader);

			String line = getNextRowFromBufferedReader(br);
			validateHeader(line);

			line = getNextRowFromBufferedReader(br);
			while (line != null) {
				sqliteDatabase.insertOrThrow(tableName, null, getContentValuesFromLine(line));
				line = getNextRowFromBufferedReader(br);
			}
		} catch (IOException e) {
			throw new CsvImportException(String.format(
					"Cannot import file '%s' into database.", fileName), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Read next line from the buffered reader. If the line starts with "//" it
	 * is skipped as a comment line.
	 * 
	 * @param bufferedReader
	 *            The buffered reader to read from.
	 * @return The first line which is not a comment. Null in case of end of
	 *         stream.
	 * @throws IOException
	 */
	private String getNextRowFromBufferedReader(BufferedReader bufferedReader)
			throws IOException {
		String line = readLineFromBuffer(bufferedReader);

		while (line != null && (isCommentLine(line) || isEmptyLine(line))) {
			line = readLineFromBuffer(bufferedReader);
		}

		if (line != null && isMultiLine(line)) {
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(line);
			while (line != null && !containsRequiredNumberOfValues(stringBuilder.toString())) {
				line = readLineFromBuffer(bufferedReader);
				stringBuilder.append("\n");
				stringBuilder.append(line);
			}
			return stringBuilder.toString();
		}
		return line;
	}

	private String readLineFromBuffer(BufferedReader bufferedReader) throws IOException {
		String line = bufferedReader.readLine();
		linesRead++;
		return line;
	}

	private boolean isCommentLine(String line) {
		return line.startsWith("\"//") || line.startsWith("//");
	}

	private boolean isEmptyLine(String line) {
		return line.isEmpty() || line.matches(REGEXP_CSV_SEPARATOR + "*");
	}

	private boolean isMultiLine(String line) {
		String[] values = getValuesFromLine(line);

		if (values.length == 0) {
			return false;
		}
		String lastValueOnLine = values[values.length - 1];
		return (lastValueOnLine.startsWith("\"") && !lastValueOnLine.endsWith("\""));
	}

	private boolean containsRequiredNumberOfValues(String line) {
		String[] values = getValuesFromLine(line);
		return values.length >= columnNames.length;
	}

	private ContentValues getContentValuesFromLine(String line) {
		String[] values = getValuesFromLine(line);

		if (values.length < columnNames.length) {
			throw new CsvImportException(String.format(
					"Line %d below contains to few values. Expected at least %d " + "values while " +
							"%s are found.\nLine: %s",
					linesRead, columnNames.length, values.length, line));
		}

		ContentValues contentValues = new ContentValues();
		for (int i = 0; i < columnNames.length; i++) {
			try {
				switch (columnTypes[i]) {
				case INTEGER:
					contentValues.put(columnNames[i],
							Integer.valueOf(values[i]));
					break;
				case LONG:
					contentValues.put(columnNames[i], Long.valueOf(values[i]));
					break;
				default:
					contentValues.put(columnNames[i], values[i]);
					break;
				}
			} catch (NumberFormatException e) {
				throw new CsvImportException(
						String
								.format("Value '%s' on line %d is not a valid %s value for column " +
												"'%s'.",
										values[i], linesRead, columnTypes[i].toString(),
										columnNames[i]), e);
			}
			catch (Exception e) {
				throw new CsvImportException(
						String
								.format("Error while processing value '%s' on line %d for %s column " +
												"'%s'.",
										values[i], linesRead, columnTypes[i].toString(),
										columnNames[i]), e);
			}
		}

		return contentValues;
	}

	private String[] getValuesFromLine(String line) {
		String[] values = line.split(REGEXP_CSV_SEPARATOR);
		for (int i = 0; i < values.length; i++) {
			values[i] = removeEnclosingQuotationMarks(values[i]);
		}
		return values;
	}

	private String removeEnclosingQuotationMarks(String value) {
		if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	private void validateHeader(String line) {
		if (line == null) {
			throw new CsvImportException("File is empty.");
		}
		String[] values = getValuesFromLine(line);
		int index = 0;
		for (String columnName : columnNames) {
			if (index >= values.length || !columnName.equals(values[index])) {

				throw new CsvImportException(String.format(
						"Column '%s' is found while column '%s' was expected.\nGot"
								+ " columns: " + "%s\nExpected columns: %s",
						removeEnclosingQuotationMarks(values[index]),
						columnName, Arrays.asList(values).toString(), Arrays
								.asList(columnNames)
								.toString()));
			}
			index++;
		}
	}
}
