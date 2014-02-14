package net.mathdoku.plus.grid;

import net.mathdoku.plus.enums.CageOperator;

import java.util.Arrays;

/**
 * This class holds all data needed to build a new {@link Cage} instance.
 */
public class CageBuilder {
	private int mId; // Required
	private CageOperator mCageOperator; // Required
	private int mResult; // Required
	private boolean mHideOperator; // Optional
	private int[] mCells; // Required

	public CageBuilder() {
		// Set default values for all optionals
		mHideOperator = false;
	}

	public CageBuilder setId(int id) {
		mId = id;

		return this;
	}

	public CageBuilder setCageOperator(CageOperator cageOperator) {
		mCageOperator = cageOperator;

		return this;
	}

	public CageBuilder setResult(int result) {
		mResult = result;

		return this;
	}

	public CageBuilder setHideOperator(boolean hideOperator) {
		mHideOperator = hideOperator;

		return this;
	}

	public CageBuilder setCells(int[] cells) {
		mCells = cells == null ? null : cells.clone();

		return this;
	}

	public Cage build() {
		return new Cage(this);
	}

	public int getId() {
		return mId;
	}

	public CageOperator getCageOperator() {
		return mCageOperator;
	}

	public int getResult() {
		return mResult;
	}

	public boolean getHideOperator() {
		return mHideOperator;
	}

	public int[] getCells() {
		return mCells;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("CageBuilder{\n");
		stringBuilder.append("\tmId=").append(mId).append("\n");
		stringBuilder
				.append("\tmCageOperator=")
				.append(mCageOperator)
				.append("\n");
		stringBuilder.append("\tmResult=").append(mResult).append("\n");
		stringBuilder
				.append("\tmHideOperator=")
				.append(mHideOperator)
				.append("\n");
		stringBuilder.append("\tmCells=[");
		for (int i = 0; i < mCells.length; i++) {
			stringBuilder.append(mCells[i]);
			if (i < mCells.length - 1) {
				stringBuilder.append(", ");
			}
		}
		stringBuilder.append("]\n");
		stringBuilder.append("}");
		return stringBuilder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CageBuilder)) {
			return false;
		}

		CageBuilder that = (CageBuilder) o;

		if (mHideOperator != that.mHideOperator) {
			return false;
		}
		if (mId != that.mId) {
			return false;
		}
		if (mResult != that.mResult) {
			return false;
		}
		if (mCageOperator != that.mCageOperator) {
			return false;
		}

		return Arrays.equals(mCells, that.mCells);
	}

	@Override
	public int hashCode() {
		int result = mId;
		result = 31 * result
				+ (mCageOperator != null ? mCageOperator.hashCode() : 0);
		result = 31 * result + mResult;
		result = 31 * result + (mHideOperator ? 1 : 0);
		result = 31 * result + (mCells != null ? Arrays.hashCode(mCells) : 0);
		return result;
	}
}
