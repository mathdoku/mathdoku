package net.mathdoku.plus.util;

import java.util.List;

public class ParameterValidator {
    private ParameterValidator() {
        // Prevent instantiation of utility class.
    }

    public static void validateNotNullOrEmpty(String string) {
        validateNotNull(string);
        if (string.trim()
                .isEmpty()) {
            throwEmptyException();
        }
    }

    public static <T> void validateNotNullOrEmpty(List<T> list) {
        if (list == null || list.size() == 0) {
            throwEmptyException();
        }
    }

    public static void validateNotNullOrEmpty(String[] strings) {
        validateNotNull(strings);
        if (strings.length == 0) {
            throwEmptyException();
        }
        for (String string : strings) {
            validateNotNullOrEmpty(string);
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
