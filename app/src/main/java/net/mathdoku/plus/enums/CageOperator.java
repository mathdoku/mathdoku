package net.mathdoku.plus.enums;

import java.security.InvalidParameterException;

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

	public static CageOperator fromId(String cageOperatorId) {
		int id = Integer.parseInt(cageOperatorId);
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
			throw new InvalidParameterException("Cannot convert id '" + id
					+ "' to a CageOperator");
		}
	}

	public int getId() {
		return mCageOperatorId;
	}

	public String getSign() {
		return mCageOperatorSign;
	}
}
