package net.mathdoku.plus.puzzle.cell;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all data needed to build a new
 * {@link net.mathdoku.plus.puzzle.cell.Cell} instance.
 */
public class CellBuilder {
	private int mGridSize; // Required
	private int mId; // Required
	private int mCorrectValue; // Required
	private int mEnteredValue; // Optional
	private int mCageId; // Required
	private String mCageText; // Optional
	private List<Integer> mPossibles; // Optional
	private boolean mDuplicateValueHighlight; // Optional
	private boolean mSelected; // Optional
	private boolean mRevealed; // Optional
	private boolean mInvalidValueHighlight; // Optional
	private CellBuilderErrorChecking mCellBuilderErrorChecking; // Optional

	private enum CellBuilderErrorChecking {
		NORMAL, LENIENT_CORRECT_VALUE_CHECK, SKIP_CAGE_CHECK
	}

	public static final int GRID_SIZE_NOT_SET = -1;
	public static final int ID_NOT_SET = -1;
	public static final int CORRECT_VALUE_NOT_SET = 0;
	public static final int ENTERED_VALUE_NOT_SET = 0;
	public static final int CAGE_ID_NOT_SET = -1;

	public CellBuilder() {
		mGridSize = GRID_SIZE_NOT_SET;
		mId = ID_NOT_SET;
		mCorrectValue = CORRECT_VALUE_NOT_SET;
		mEnteredValue = ENTERED_VALUE_NOT_SET;
		mCageId = CAGE_ID_NOT_SET;
		mCageText = "";
		mPossibles = new ArrayList<Integer>();
		mDuplicateValueHighlight = false;
		mSelected = false;
		mRevealed = false;
		mInvalidValueHighlight = false;
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

	public CellBuilder setEnteredValue(int enteredValue) {
		mEnteredValue = enteredValue;

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

	public CellBuilder setInvalidValueHighlight(boolean invalidValueHighlight) {
		mInvalidValueHighlight = invalidValueHighlight;

		return this;
	}

	/**
	 * Perform a lenient check on the correct value when building the cell. This
	 * allows the correct value to be equal to zero. This is needed in case a
	 * grid is being build solely on a grid definition which does not contain
	 * the correct values. After the grid is created, the correct values are
	 * determined with the MathdokuDLX class. This method cannot be used in
	 * conjunction with setSkipCheckCageReferenceOnBuild.
	 */
	public CellBuilder setLenientCheckCorrectValueOnBuild() {
		mCellBuilderErrorChecking = CellBuilderErrorChecking.LENIENT_CORRECT_VALUE_CHECK;

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

	public int getEnteredValue() {
		return mEnteredValue;
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

	public boolean isInvalidValueHighlighted() {
		return mInvalidValueHighlight;
	}

	public boolean performLenientCorrectValueCheck() {
		return mCellBuilderErrorChecking == CellBuilderErrorChecking.LENIENT_CORRECT_VALUE_CHECK;
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
		stringBuilder.append(", mEnteredValue=").append(mEnteredValue);
		stringBuilder.append(", mCageId=").append(mCageId);
		stringBuilder.append(", mCageText='").append(mCageText).append('\'');
		stringBuilder.append(", mPossibles=").append(mPossibles);
		stringBuilder.append(", mDuplicateValueHighlight=").append(
				mDuplicateValueHighlight);
		stringBuilder.append(", mSelected=").append(mSelected);
		stringBuilder.append(", mRevealed=").append(mRevealed);
		stringBuilder.append(", mInvalidValueHighlight=").append(mInvalidValueHighlight);
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
		if (mInvalidValueHighlight != that.mInvalidValueHighlight) {
			return false;
		}
		if (mRevealed != that.mRevealed) {
			return false;
		}
		if (mSelected != that.mSelected) {
			return false;
		}
		if (mEnteredValue != that.mEnteredValue) {
			return false;
		}
		if (!mCageText.equals(that.mCageText)) {
			return false;
		}
		if (mCellBuilderErrorChecking != that.mCellBuilderErrorChecking) {
			return false;
		}
		if (mPossibles != null ? !mPossibles.equals(that.mPossibles)
				: that.mPossibles != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = mGridSize;
		result = 31 * result + mId;
		result = 31 * result + mCorrectValue;
		result = 31 * result + mEnteredValue;
		result = 31 * result + mCageId;
		result = 31 * result + mCageText.hashCode();
		result = 31 * result + (mPossibles != null ? mPossibles.hashCode() : 0);
		result = 31 * result + (mDuplicateValueHighlight ? 1 : 0);
		result = 31 * result + (mSelected ? 1 : 0);
		result = 31 * result + (mRevealed ? 1 : 0);
		result = 31 * result + (mInvalidValueHighlight ? 1 : 0);
		result = 31 * result + mCellBuilderErrorChecking.hashCode();
		return result;
	}
}
