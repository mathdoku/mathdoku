package net.cactii.mathdoku.grid;

public class InvalidGridException extends ExceptionInInitializerError {

	private static final long serialVersionUID = -1502716956881113470L;

	String mErrorMessage;

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
	 */
	public InvalidGridException(String errorMessage) {
		super(errorMessage); // call super class constructor
		this.mErrorMessage = errorMessage; // save message
	}

	public String getError() {
		return mErrorMessage;
	}

}
