package net.mathdoku.plus.grid;

public class InvalidGridException extends ExceptionInInitializerError {

	private static final long serialVersionUID = -1502716956881113470L;

	private final String mErrorMessage;

	/**
	 * Creates a new instance of {@link InvalidGridException}.
	 */
	public InvalidGridException() {
		super();
		mErrorMessage = "unknown";
	}

	/**
	 * Creates a new instance of {@link InvalidGridException}.
	 * 
	 * @param errorMessage
	 *            The error message for the exception.
	 */
	public InvalidGridException(String errorMessage) {
		super(errorMessage); // call super class constructor
		this.mErrorMessage = errorMessage; // save message
	}

	public String getError() {
		return mErrorMessage;
	}

}
