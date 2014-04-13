package net.mathdoku.plus.gridgenerating;

public class GridGeneratingException extends RuntimeException {
	@SuppressWarnings("unused")
	public GridGeneratingException() {
		super();
	}

	public GridGeneratingException(String errorMessage) {
		super(errorMessage);
	}

	@SuppressWarnings("unused")
	public GridGeneratingException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
