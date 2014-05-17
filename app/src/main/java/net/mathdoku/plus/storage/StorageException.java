package net.mathdoku.plus.storage;

public class StorageException extends RuntimeException {
	public StorageException() {
		super();
	}

	public StorageException(String errorMessage) {
		super(errorMessage);
	}

	public StorageException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
