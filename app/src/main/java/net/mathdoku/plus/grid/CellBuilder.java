package net.mathdoku.plus.grid;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all data needed to build a new {@link Cell} instance.
 */
public class CellBuilder {
	private int mGridSize; // Required
	private int mId; // Required
	private int mCorrectValue; // Required
	private int mUserValue; // Optional
	private int mCageId; // Required
	private String mCageText; // Optional
	private List<Integer> mPossibles; // Optional
	private boolean mDuplicateValueHighlight; // Optional
	private boolean mSelected; // Optional
	private boolean mRevealed; // Optional
	private boolean mInvalidUserValueHighlight; // Optional
	private CellBuilderErrorChecking mCellBuilderErrorChecking; // Optional

	private enum CellBuilderErrorChecking {
		NORMAL, SKIP_CORRECT_VALUE_CHECK, SKIP_CAGE_CHECK
	}

	public CellBuilder() {
		mGridSize = -1; // Unknown
		mId = -1; // Unknown
		mCorrectValue = -1; // Unknown
		mUserValue = 0; // Not filled in
		mCageId = -1; // Unknown
		mCageText = "";
		mPossibles = new ArrayList<Integer>();
		mDuplicateValueHighlight = false;
		mSelected = false;
		mRevealed = false;
		mInvalidUserValueHighlight = false;
		mCellBuilderErrorChecking = CellBuilderErrorChecking.NORMAL;
	}

	public CellBuilder setGridSize(int gridSize) {
		mGridSize = gridSize;

		return this;
	}

	public CellBuilder setId(int id) {
		mId = id;

		return this;
	}

	public CellBuilder setCorrectValue(int correctValue) {
		mCorrectValue = correctValue;

		return this;
	}

	public CellBuilder setUserValue(int userValue) {
		mUserValue = userValue;

		return this;
	}

	public CellBuilder setCageId(int cageId) {
		mCageId = cageId;

		return this;
	}

	public CellBuilder setCageText(String cageText) {
		mCageText = cageText;

		return this;
	}

	public CellBuilder setPossibles(List<Integer> possibles) {
		mPossibles = possibles;

		return this;
	}

	public CellBuilder setDuplicateValueHighlight(
			boolean duplicateValueHighlight) {
		mDuplicateValueHighlight = duplicateValueHighlight;

		return this;
	}

	public CellBuilder setSelected(boolean selected) {
		mSelected = selected;

		return this;
	}

	public CellBuilder setRevealed(boolean revealed) {
		mRevealed = revealed;

		return this;
	}

	public CellBuilder setInvalidUserValueHighlight(
			boolean invalidUserValueHighlight) {
		mInvalidUserValueHighlight = invalidUserValueHighlight;

		return this;
	}

	/**
	 * Skips checking the correct value.
	 * 
	 * In case of loading a grid based on a grid definition this is not possible
	 * as those value are unknown until the grid is solved with the MathdokuDLX
	 * class. Cannot be used in conjunction with setDeferCageCheck.
	 */
	public CellBuilder setSkipCheckCorrectValueOnBuild() {
		mCellBuilderErrorChecking = CellBuilderErrorChecking.SKIP_CORRECT_VALUE_CHECK;

		return this;
	}

	/**
	 * Skips checking the cage reference.
	 * 
	 * In case of generating a grid the cells are created before the cages. The
	 * cage checks should therefore be deferred. Cannot be used in conjunction
	 * with setDeferCageCheck.
	 */
	public CellBuilder setSkipCheckCageReferenceOnBuild() {
		mCellBuilderErrorChecking = CellBuilderErrorChecking.SKIP_CAGE_CHECK;

		return this;
	}

	public Cell build() {
		return new Cell(this);
	}

	public int getGridSize() {
		return mGridSize;
	}

	public int getId() {
		return mId;
	}

	public int getCorrectValue() {
		return mCorrectValue;
	}

	public int getUserValue() {
		return mUserValue;
	}

	public int getCageId() {
		return mCageId;
	}

	public String getCageText() {
		return mCageText;
	}

	public List<Integer> getPossibles() {
		return mPossibles;
	}

	public boolean isDuplicateValueHighlighted() {
		return mDuplicateValueHighlight;
	}

	public boolean isSelected() {
		return mSelected;
	}

	public boolean isRevealed() {
		return mRevealed;
	}

	public boolean isInvalidUserValueHighlighted() {
		return mInvalidUserValueHighlight;
	}

	public boolean performCorrectValueCheck() {
		return mCellBuilderErrorChecking != CellBuilderErrorChecking.SKIP_CORRECT_VALUE_CHECK;
	}

	public boolean performCageReferenceCheck() {
		return mCellBuilderErrorChecking != CellBuilderErrorChecking.SKIP_CAGE_CHECK;
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder("CellBuilder{");
		stringBuilder.append("mGridSize=").append(mGridSize);
		stringBuilder.append(", mId=").append(mId);
		stringBuilder.append(", mCorrectValue=").append(mCorrectValue);
		stringBuilder.append(", mUserValue=").append(mUserValue);
		stringBuilder.append(", mCageId=").append(mCageId);
		stringBuilder.append(", mCageText='").append(mCageText).append('\'');
		stringBuilder.append(", mPossibles=").append(mPossibles);
		stringBuilder.append(", mDuplicateValueHighlight=").append(
				mDuplicateValueHighlight);
		stringBuilder.append(", mSelected=").append(mSelected);
		stringBuilder.append(", mRevealed=").append(mRevealed);
		stringBuilder.append(", mInvalidUserValueHighlight=").append(
				mInvalidUserValueHighlight);
		stringBuilder.append(", mCellBuilderErrorChecking=").append(
				mCellBuilderErrorChecking);
		stringBuilder.append('}');
		return stringBuilder.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof CellBuilder)) {
			return false;
		}

		CellBuilder that = (CellBuilder) o;

		if (mCageId != that.mCageId) {
			return false;
		}
		if (mCorrectValue != that.mCorrectValue) {
			return false;
		}
		if (mDuplicateValueHighlight != that.mDuplicateValueHighlight) {
			return false;
		}
		if (mGridSize != that.mGridSize) {
			return false;
		}
		if (mId != that.mId) {
			return false;
		}
		if (mInvalidUserValueHighlight != that.mInvalidUserValueHighlight) {
			return false;
		}
		if (mRevealed != that.mRevealed) {
			return false;
		}
		if (mSelected != that.mSelected) {
			return false;
		}
		if (mUserValue != that.mUserValue) {
			return false;
		}
		if (!mCageText.equals(that.mCageText)) {
			return false;
		}
		if (mCellBuilderErrorChecking != that.mCellBuilderErrorChecking) {
			return false;
		}
		if (!mPossibles.equals(that.mPossibles)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = mGridSize;
		result = 31 * result + mId;
		result = 31 * result + mCorrectValue;
		result = 31 * result + mUserValue;
		result = 31 * result + mCageId;
		result = 31 * result + mCageText.hashCode();
		result = 31 * result + mPossibles.hashCode();
		result = 31 * result + (mDuplicateValueHighlight ? 1 : 0);
		result = 31 * result + (mSelected ? 1 : 0);
		result = 31 * result + (mRevealed ? 1 : 0);
		result = 31 * result + (mInvalidUserValueHighlight ? 1 : 0);
		result = 31 * result + mCellBuilderErrorChecking.hashCode();
		return result;
	}
}
