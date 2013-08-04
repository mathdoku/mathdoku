package net.cactii.mathdoku.util;

public class SingletonInstanceNotInstantiated extends RuntimeException {
	private static final long serialVersionUID = -236646178154899930L;

	String mErrorMessage;

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
	 */
	public SingletonInstanceNotInstantiated(String errorMessage) {
		super(errorMessage);
		mErrorMessage = errorMessage;
	}

	public String getError() {
		return mErrorMessage;
	}
}
