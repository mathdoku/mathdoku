package csvimporter;

public class CsvImporterException extends RuntimeException {
	public CsvImporterException(String message) {
		super(message);
	}

	public CsvImporterException(String message, Throwable throwable) {
		super(message, throwable);
	}
}
