package net.cactii.mathdoku.storage;

public class InvalidStatisticsException extends Exception {
	private static final long serialVersionUID = 2131519517740911182L;

	String mErrorMessage;

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
	 */
	public InvalidStatisticsException(String errorMessage) {
		super(errorMessage); // call super class constructor
		this.mErrorMessage = errorMessage; // save message
	}

	public String getError() {
		return mErrorMessage;
	}
}
