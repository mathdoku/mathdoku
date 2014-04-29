package net.mathdoku.plus.util;

public class ParameterValidator {
	private ParameterValidator() {
		// Prevent instantiation of utility class.
	}

	public static void validateNotNullOrEmpty(String string) {
		validateNotNull(string);
		if (string.isEmpty()) {
			throwEmptyException();
		}
	}

	public static void validateNotNull(Object object) {
		if (object == null) {
			throw new IllegalArgumentException("Parameter cannot be null.");
		}
	}

	private static void throwEmptyException() {
		throw new IllegalArgumentException("Parameter cannot be empty.");
	}
}
