package net.cactii.mathdoku;

public class InvalidFileFormatException extends Exception {
	String errorMessage;

	/**
	 * Creates a new instance of {@link InvalidFileFormatException} in 
	 */
	public InvalidFileFormatException() {
		super();
		errorMessage = "unknown";
	}

	/**
	 * Creates a new instance of {@link InvalidFileFormatException}.

	 * @param errorMessage
	 */
	public InvalidFileFormatException(String errorMessage) {
		super(errorMessage); // call super class constructor
		this.errorMessage = errorMessage; // save message
	}

	public String getError() {
		return errorMessage;
	}
}
