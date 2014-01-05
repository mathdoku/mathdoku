package net.mathdoku.plus.storage.database;

public class DatabaseException extends RuntimeException {
	private final String mErrorMessage;

	/**
	 * Creates a new instance of
	 * {@link net.mathdoku.plus.storage.database.DatabaseException}.
	 */
	public DatabaseException() {
		super();
		mErrorMessage = "unknown";
	}

	/**
	 * Creates a new instance of
	 * {@link net.mathdoku.plus.storage.database.DatabaseException}.
	 * 
	 * @param errorMessage
	 *            The error message for the exception.
	 */
	public DatabaseException(String errorMessage) {
		super(errorMessage); // call super class constructor
		this.mErrorMessage = errorMessage; // save message
	}

	public String getError() {
		return mErrorMessage;
	}
}
