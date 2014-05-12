package databasehelper.CsvImporter;

public class CsvImportException extends RuntimeException {
	public CsvImportException(String message) {
		super(message);
	}

	public CsvImportException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
