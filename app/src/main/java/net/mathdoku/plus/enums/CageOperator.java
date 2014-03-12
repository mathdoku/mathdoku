package net.mathdoku.plus.enums;

public enum CageOperator {
	// The cage operators id's of the enum values should not be altered as these
	// values are persisted.
	NONE(0, ""), ADD(1, "+"), SUBTRACT(2, "-"), MULTIPLY(3, "x"), DIVIDE(4, "/");

	private int mCageOperatorId;
	private String mCageOperatorSign;

	private CageOperator(int cageOperatorId, String cageOperatorSign) {
		mCageOperatorId = cageOperatorId;
		mCageOperatorSign = cageOperatorSign;
	}

	public static CageOperator fromId(String cageOperatorId)
			throws IllegalArgumentException {
		int id = Integer.parseInt(cageOperatorId);
		try {
			switch (id) {
			case 0:
				return NONE;
			case 1:
				return ADD;
			case 2:
				return SUBTRACT;
			case 3:
				return MULTIPLY;
			case 4:
				return DIVIDE;
			default:
				throw new IllegalArgumentException(
						getErrorStringCannotConvertIdToCageOperator(cageOperatorId));
			}
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					getErrorStringCannotConvertIdToCageOperator(cageOperatorId),
					e);
		}
	}

	private static String getErrorStringCannotConvertIdToCageOperator(
			String cageOperatorId) {
		return String.format("Cannot convert id '%s' to a CageOperator",
				cageOperatorId);
	}

	public int getId() {
		return mCageOperatorId;
	}

	public String getSign() {
		return mCageOperatorSign;
	}
}
