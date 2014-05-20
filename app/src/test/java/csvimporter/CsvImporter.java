package csvimporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import robolectric.TestRunnerHelper;

/**
 * The CsvImporter is the base class to import csv files. This base class
 * handles the reading of lines of data form a csv file. The csv file must
 * adhere to rules below:
 * 
 * 1. Rows starting with "//" are handled as comments and will be ignored. Also
 * empty rows will be ignored.
 * 
 * 2. Columns in the csv file must be in specified order.
 * 
 * 3. Additional columns at the end of a row are allowed but ignored.
 */
public abstract class CsvImporter {
	private static final String REGEXP_CSV_SEPARATOR = "#";
	protected final String fileName;
	protected final String[] columnNames;
	private int linesRead = 0;

	public CsvImporter(String fileName, String[] columnNames) {
		if (fileName == null || fileName.isEmpty()) {
			throw new CsvImporterException(
					"Parameter filename should not be empty.");
		}
		if (columnNames == null || columnNames.length == 0) {
			throw new CsvImporterException(
					"At least one column name must be specified.");
		}
		for (String columnName : columnNames) {
			if (columnName == null || columnName.isEmpty()) {
				throw new CsvImporterException(String.format(
						"ColumnName '%s' is not valid.", columnName));
			}
		}
		this.fileName = fileName;
		this.columnNames = columnNames.clone();
	}

	/**
	 * Read next line from the buffered reader. If the line starts with "//" it
	 * is skipped as a comment line.
	 * 
	 * @param bufferedReader
	 *            The buffered reader to read from.
	 * @return The first line which is not a comment. Null in case of end of
	 *         stream.
	 * @throws java.io.IOException
	 */
	protected String getNextRowFromBufferedReader(BufferedReader bufferedReader)
			throws IOException {
		String line = readLineFromBuffer(bufferedReader);

		while (line != null
				&& (isCommentLine(line) || isEmptyLine(line) || isMultiLine(line))) {
			if (isMultiLine(line)) {
				StringBuilder stringBuilder = new StringBuilder();
				stringBuilder.append(line);
				while (line != null
						&& !containsRequiredNumberOfValues(stringBuilder
								.toString())) {
					line = readLineFromBuffer(bufferedReader);
					stringBuilder.append("\n");
					stringBuilder.append(line);
				}
				if (!isCommentLine(stringBuilder.toString())) {
					return stringBuilder.toString();
				}
			}

			line = readLineFromBuffer(bufferedReader);
		}

		return line;
	}

	private String readLineFromBuffer(BufferedReader bufferedReader)
			throws IOException {
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
		return (lastValueOnLine.startsWith("\"") && !lastValueOnLine
				.endsWith("\""));
	}

	private boolean containsRequiredNumberOfValues(String line) {
		String[] values = getValuesFromLine(line);
		return values.length >= columnNames.length;
	}

	public void importFile() {
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
				importLine(line);
				line = getNextRowFromBufferedReader(br);
			}
		} catch (IOException e) {
			throw new CsvImporterException(String.format(
					"Cannot import file '%s'.", fileName), e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		importCompletedSuccessfull();
	}

	protected abstract void importLine(String line);

	// Subclasses should override this method in case additional work has to be done when entire file is imported successfully.
	protected void importCompletedSuccessfull() {}

	private String removeEnclosingQuotationMarks(String value) {
		if (value != null && value.startsWith("\"") && value.endsWith("\"")) {
			return value.substring(1, value.length() - 1);
		}
		return value;
	}

	protected void validateHeader(String line) {
		if (line == null) {
			throw new CsvImporterException("File is empty.");
		}
		String[] values = getValuesFromLine(line);
		int index = 0;
		for (String columnName : columnNames) {
			if (index >= values.length || !columnName.equals(values[index])) {

				throw new CsvImporterException(String.format(
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

	protected int getLinesRead() {
		return linesRead;
	}

	protected String[] getValuesFromLine(String line) {
		String[] values = line.split(REGEXP_CSV_SEPARATOR);
		for (int i = 0; i < values.length; i++) {
			values[i] = removeEnclosingQuotationMarks(values[i]);
		}
		return values;
	}
}
