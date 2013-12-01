package net.mathdoku.plus.util;

public class SingletonInstanceNotInstantiated extends RuntimeException {
	private static final long serialVersionUID = -236646178154899930L;

	private String mErrorMessage;

	/**
	 * Creates a new instance of {@link SingletonInstanceNotInstantiated}.
	 */
	public SingletonInstanceNotInstantiated() {
		super(
				"Can not get singleton instance as long a the class has not been instantiated before.");
	}

	/**
	 * Creates a new instance of {@link SingletonInstanceNotInstantiated}.
	 * 
	 * @param errorMessage
	 *            The message to be displayed in case this error is thrown.
	 */
	public SingletonInstanceNotInstantiated(String errorMessage) {
		super(errorMessage);
		mErrorMessage = errorMessage;
	}

	public String getError() {
		return mErrorMessage;
	}
}
