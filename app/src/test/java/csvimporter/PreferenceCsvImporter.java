package csvimporter;

import android.content.SharedPreferences;

/**
 * This class imports records from a csv file into the app preferences. The csv
 * file must adhere to rules below:
 * 
 * 1. Rows starting with "//" are handled as comments and will be ignored. Also
 * empty rows will be ignored.
 * 
 * 2. Columns name, type and value are mandatory and must be defined in this
 * order as the first three columns.
 * 
 * 3. Additional columns at end are ignored.
 */
public class PreferenceCsvImporter extends CsvImporter {
	private static final String[] COLUMN_NAMES = new String[] { "name", "type",
			"value" };
	private final SharedPreferences.Editor editor;

	public PreferenceCsvImporter(String fileName,
			SharedPreferences sharedPreferences) {
		super(fileName, COLUMN_NAMES);
		if (sharedPreferences == null) {
			throw new CsvImporterException("Shared preferences cannot be null.");
		}
		this.editor = sharedPreferences.edit();
	}

	@Override
	protected void importLine(String line) {
		String[] values = getValuesFromLine(line);

		if (values.length < columnNames.length) {
			throw new CsvImporterException(String.format(
					"Line %d below contains to few values. Expected at least %d "
							+ "values while " + "%s are found.\nLine: %s",
					getLinesRead(), columnNames.length, values.length, line));
		}

		String name = values[0];
		String dataType = values[1];
		String value = values[2];
		try {
			if ("boolean".equalsIgnoreCase(dataType)) {
				editor.putBoolean(name, Boolean.valueOf(value.toLowerCase()));
			} else if ("int".equalsIgnoreCase(dataType)) {
				editor.putInt(name, Integer.parseInt(value));
			} else if ("string".equalsIgnoreCase(dataType)) {
				editor.putString(name, value);
			} else if ("long".equalsIgnoreCase(dataType)) {
				editor.putLong(name, Long.parseLong(value));
			} else {
				throw new CsvImporterException(String.format(
						"Data type '%s' on line %d is not a valid data type. ",
						dataType, getLinesRead()));
			}
		} catch (NumberFormatException e) {
			throw new CsvImporterException(
					String.format(
							"Value '%s' on line %d is not a valid value for data type '%s'.",
							value, getLinesRead(), dataType), e);
		}
	}

	@Override
	protected void importCompletedSuccessfull() {
		editor.commit();
	}
}
