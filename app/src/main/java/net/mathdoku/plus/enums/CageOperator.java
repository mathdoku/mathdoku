package net.mathdoku.plus.enums;

public enum CageOperator {
    // The cage operators id's of the enum values should not be altered as these
    // values are persisted.
    NONE(0, ""),
    ADD(1, "+"),
    SUBTRACT(2, "-"),
    MULTIPLY(3, "x"),
    DIVIDE(4, "/");

    private int cageOperatorId;
    private String cageOperatorSign;

    private CageOperator(int cageOperatorId, String cageOperatorSign) {
        this.cageOperatorId = cageOperatorId;
        this.cageOperatorSign = cageOperatorSign;
    }

    public static CageOperator fromId(String cageOperatorId) {
        try {
            return fromId(Integer.parseInt(cageOperatorId));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(getErrorStringCannotConvertIdToCageOperator(cageOperatorId), e);
        }
    }

    private static CageOperator fromId(int cageOperatorId) {
        for (CageOperator cageOperator : values()) {
            if (cageOperator.cageOperatorId == cageOperatorId) {
                return cageOperator;
            }
        }

        throw new IllegalArgumentException(getErrorStringCannotConvertIdToCageOperator(String.valueOf(cageOperatorId)));
    }

    private static String getErrorStringCannotConvertIdToCageOperator(String cageOperatorId) {
        return String.format("Cannot convert id '%s' to a CageOperator", cageOperatorId);
    }

    public int getId() {
        return cageOperatorId;
    }

    public String getSign() {
        return cageOperatorSign;
    }
}
