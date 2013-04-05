package net.cactii.mathdoku;

public class InvalidFileFormatException extends Exception {
	private static final long serialVersionUID = 6377920643705765951L;

	String mErrorMessage;

	/**
	 * Creates a new instance of {@link InvalidFileFormatException}.
	 */
	public InvalidFileFormatException() {
		super();
		mErrorMessage = "unknown";
	}

	/**
	 * Creates a new instance of {@link InvalidFileFormatException}.
	 * 
	 * @param errorMessage
	 */
	public InvalidFileFormatException(String errorMessage) {
		super(errorMessage); // call super class constructor
		this.mErrorMessage = errorMessage; // save message
	}

	public String getError() {
		return mErrorMessage;
	}
}
