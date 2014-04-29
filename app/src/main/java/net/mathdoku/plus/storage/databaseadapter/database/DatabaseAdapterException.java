package net.mathdoku.plus.storage.databaseadapter.database;

public class DatabaseAdapterException extends RuntimeException {
	public DatabaseAdapterException() {
		super();
	}

	public DatabaseAdapterException(String errorMessage) {
		super(errorMessage);
	}

	public DatabaseAdapterException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
