package net.mathdoku.plus.grid;

public class UnexpectedMethodInvocationException extends RuntimeException {

	private final String mErrorMessage;

	public UnexpectedMethodInvocationException() {
		super();
		mErrorMessage = "unknown";
	}

	public UnexpectedMethodInvocationException(String errorMessage) {
		super(errorMessage);
		mErrorMessage = errorMessage; // save message
	}

	public String getError() {
		return mErrorMessage;
	}

}
