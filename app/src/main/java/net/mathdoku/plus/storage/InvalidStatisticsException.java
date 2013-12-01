package net.mathdoku.plus.storage;

public class InvalidStatisticsException extends Exception {
	private static final long serialVersionUID = 2131519517740911182L;

	private final String mErrorMessage;

	/**
	 * Creates a new instance of {@link InvalidStatisticsException}.
	 */
	public InvalidStatisticsException() {
		super();
		mErrorMessage = "unknown";
	}

	/**
	 * Creates a new instance of {@link InvalidStatisticsException}.
	 * 
	 * @param errorMessage
	 *            The error message for the exception.
	 */
	public InvalidStatisticsException(String errorMessage) {
		super(errorMessage); // call super class constructor
		this.mErrorMessage = errorMessage; // save message
	}

	public String getError() {
		return mErrorMessage;
	}
}
