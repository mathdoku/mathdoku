package net.mathdoku.plus.enums;

public enum PuzzleComplexity {
    // The puzzle complexity id's of the enum values should not be altered as
    // these values are persisted.
    RANDOM(0),
    VERY_EASY(1),
    EASY(2),
    NORMAL(3),
    DIFFICULT(4),
    VERY_DIFFICULT(5);

    @SuppressWarnings("unused")
    private static final String TAG = PuzzleComplexity.class.getName();

    private final int puzzleComplexityId;

    private PuzzleComplexity(int puzzleComplexityId) {
        this.puzzleComplexityId = puzzleComplexityId;
    }

    public static PuzzleComplexity fromId(String puzzleComplexityId) {
        try {
            return fromId(Integer.parseInt(puzzleComplexityId));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    getErrorStringCannotConvertIdToPuzzleComplexity(puzzleComplexityId), e);
        }
    }

    public static PuzzleComplexity fromId(int puzzleComplexityId) {
        for (PuzzleComplexity puzzleComplexity : values()) {
            if (puzzleComplexity.puzzleComplexityId == puzzleComplexityId) {
                return puzzleComplexity;
            }
        }

        throw new IllegalArgumentException(getErrorStringCannotConvertIdToPuzzleComplexity(
                String.valueOf(puzzleComplexityId)));
    }

    private static String getErrorStringCannotConvertIdToPuzzleComplexity(String puzzleComplexityId) {
        return String.format("Cannot convert id '%s' to a PuzzleComplexity", puzzleComplexityId);
    }

    public int getId() {
        return puzzleComplexityId;
    }
}
