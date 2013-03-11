package net.cactii.mathdoku;

import java.util.ArrayList;
import java.util.Collections;

import net.cactii.mathdoku.Painter.CagePainter;
import net.cactii.mathdoku.Painter.CellPainter;
import net.cactii.mathdoku.Painter.Maybe1x9Painter;
import net.cactii.mathdoku.Painter.Maybe3x3Painter;
import net.cactii.mathdoku.Painter.UserValuePainter;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;

public class GridCell {
	private static final String TAG = "MathDoku.GridCell";

	// Identifiers of different versions of cell information which is stored in
	// saved game.
	private final String SAVE_GAME_CELL_VERSION_01 = "CELL";
	private final String SAVE_GAME_CELL_VERSION_02 = "CELL.v2";

	// Index of the cell (left to right, top to bottom, zero-indexed)
	private int mCellNumber;
	// X grid position, zero indexed
	private int mColumn;
	// Y grid position, zero indexed
	private int mRow;
	// Value of the digit in the cell
	private int mCorrectValue;
	// User's entered value
	private int mUserValue;
	// Id of the enclosing cage
	private int mCageId;
	// String of the cage
	private String mCageText;
	// User's candidate digits
	private ArrayList<Integer> mPossibles;

	// X pixel position
	public float mPosX;
	// Y pixel position
	public float mPosY;

	// View context
	public GridView mContext;
	// Whether to show warning background (duplicate value in row/col)
	public boolean mShowWarning;
	// Whether to show cell as selected
	public boolean mSelected;
	// Player cheated (revealed this cell)
	private boolean mCheated;
	// Highlight user input isn't correct value
	private boolean mInvalidHighlight;

	public static enum BorderType {
		NONE, CELL_WARNING, OUTER_OF_CAGE_NOT_SELECTED, OUTER_OF_CAGE_SELECTED
	}

	// Borders of the cell
	public BorderType borderTypeTop;
	public BorderType borderTypeBottom;
	public BorderType borderTypeLeft;
	public BorderType borderTypeRight;

	// References to the global painter objects.
	private CellPainter mCellPainter;
	private UserValuePainter mUserValuePainter;
	private Maybe3x3Painter mMaybe3x3Painter;
	private Maybe1x9Painter mMaybe1x9Painter;
	private CagePainter mCagePainter;

	public GridCell(GridView context, int cell) {
		int gridSize = context.mGridSize;
		this.mContext = context;
		this.mCellNumber = cell;
		this.mColumn = cell % gridSize;
		this.mRow = (int) (cell / gridSize);
		this.mCageText = "";
		this.mCageId = -1;
		this.mCorrectValue = 0;
		this.mUserValue = 0;
		this.mShowWarning = false;
		this.mCheated = false;
		this.mInvalidHighlight = false;
		this.mPossibles = new ArrayList<Integer>();
		this.mPosX = 0;
		this.mPosY = 0;

		// Retrieve all painters
		this.mCellPainter = Painter.getInstance().mCellPainter;
		this.mUserValuePainter = Painter.getInstance().mUserValuePainter;
		this.mMaybe3x3Painter = Painter.getInstance().mMaybe3x3Painter;
		this.mMaybe1x9Painter = Painter.getInstance().mMaybe1x9Painter;
		this.mCagePainter = Painter.getInstance().mCagePainter;

		borderTypeTop = BorderType.NONE;
		borderTypeRight = BorderType.NONE;
		borderTypeBottom = BorderType.NONE;
		borderTypeLeft = BorderType.NONE;
	}

	public String toString() {
		String str = "<cell:" + this.mCellNumber + " col:" + this.mColumn
				+ " row:" + this.mRow + " posX:" + this.mPosX + " posY:"
				+ this.mPosY + " val:" + this.mCorrectValue + ", userval: "
				+ this.mUserValue + ">";
		return str;
	}

	private Paint getBorderPaint(BorderType borderType, boolean onlyBorders) { 
		switch (borderType) {
		case NONE:
			return null;
		case CELL_WARNING:
			return mCellPainter.mBorderWrongPaint;
		case OUTER_OF_CAGE_NOT_SELECTED:
			return mCagePainter.mBorderPaint;
		case OUTER_OF_CAGE_SELECTED:
			if (onlyBorders) {
				return mCagePainter.mBorderSelectedPaint;
			} else {
				return mCagePainter.mBorderPaint;
			}
		}
		return null;
	}

	public int countPossibles() {
		return this.mPossibles.size();
	}

	public void clearPossibles() {
		this.mPossibles.clear();
	}

	public int getFirstPossible() {
		return this.mPossibles.get(0);
	}

	public void togglePossible(int digit) {
		if (this.mPossibles.indexOf(new Integer(digit)) == -1)
			this.mPossibles.add(digit);
		else
			this.mPossibles.remove(new Integer(digit));
		Collections.sort(mPossibles);
	}

	public int getUserValue() {
		return mUserValue;
	}

	public boolean isUserValueSet() {
		return mUserValue != 0;
	}

	public void setUserValue(int digit) {
		this.mPossibles.clear();
		this.mUserValue = digit;
		mInvalidHighlight = false;
	}

	public void clearUserValue() {
		setUserValue(0);
	}

	public boolean isUserValueCorrect() {
		return mUserValue == mCorrectValue;
	}

	/* Returns whether the cell is a member of any cage */
	public boolean CellInAnyCage() {
		return mCageId != -1;
	}

	public void setInvalidHighlight(boolean value) {
		this.mInvalidHighlight = value;
	}

	public boolean getInvalidHighlight() {
		return this.mInvalidHighlight;
	}

	/* Draw the cell. Border and text is drawn. */
	public void onDraw(Canvas canvas, boolean onlyBorders) {

		// Calculate x and y for the cell origin (topleft)
		this.mPosX = this.mCellPainter.mCellSize * this.mColumn;
		this.mPosY = this.mCellPainter.mCellSize * this.mRow;

		float top = this.mPosY;
		float bottom = this.mPosY + this.mCellPainter.mCellSize;
		float left = this.mPosX + this.mCellPainter.mCellSize;
		float right = this.mPosX;
		GridCell cellAbove = this.mContext.getCellAt(this.mRow - 1,
				this.mColumn);
		GridCell cellLeft = this.mContext
				.getCellAt(this.mRow, this.mColumn - 1);
		GridCell cellRight = this.mContext.getCellAt(this.mRow,
				this.mColumn + 1);
		GridCell cellBelow = this.mContext.getCellAt(this.mRow + 1,
				this.mColumn);

		// Top
		Paint borderPaint = getBorderPaint(borderTypeTop, onlyBorders);
		if (borderPaint != null) {
			canvas.drawLine(right, top, left, top, borderPaint);
		}

		// Right
		borderPaint = getBorderPaint(borderTypeRight, onlyBorders);
		if (borderPaint != null) {
			canvas.drawLine(left, top, left, bottom, borderPaint);
		}

		// Bottom
		borderPaint = getBorderPaint(borderTypeBottom, onlyBorders);
		if (borderPaint != null) {
			canvas.drawLine(right, bottom, left, bottom, borderPaint);
		}

		// Left
		borderPaint = getBorderPaint(borderTypeLeft, onlyBorders);
		if (borderPaint != null) {
			canvas.drawLine(right, top, right, bottom, borderPaint);
		}

		if (!onlyBorders) {
			if ((mShowWarning && mContext.mDupedigits)
					|| mInvalidHighlight) {
				canvas.drawRect(right + 1, top + 1, left - 1, bottom - 1,
						mCellPainter.mBackgroundWarningPaint);
			}
			if (this.mSelected) {
				canvas.drawRect(right + 3, top + 3, left - 3, bottom - 3,
						mCellPainter.mBackgroundSelectedPaint);
			}
			if (this.mCheated) {
				canvas.drawRect(right + 1, top + 1, left - 1, bottom - 1,
						mCellPainter.mBackgroundCheatedPaint);
			}
		} else {
			if (this.borderTypeTop == BorderType.OUTER_OF_CAGE_SELECTED) {
				if (cellAbove == null) {
					top += 2;
				} else {
					top += 1;
				}
			}
			if (this.borderTypeRight == BorderType.OUTER_OF_CAGE_SELECTED) {
				if (cellRight == null) {
					left -= 3;
				} else {
					left -= 2;
				}
			}
			if (this.borderTypeBottom == BorderType.OUTER_OF_CAGE_SELECTED) {
				if (cellBelow == null) {
					bottom -= 3;
				} else {
					bottom -= 2;
				}
			}
			if (this.borderTypeLeft == BorderType.OUTER_OF_CAGE_SELECTED) {
				if (cellLeft == null) {
					right += 2;
				} else {
					right += 1;
				}
			}
		}

		if (onlyBorders)
			return;

		// Cell value
		if (this.isUserValueSet()) {
			canvas.drawText("" + this.mUserValue, this.mPosX
					+ this.mUserValuePainter.mLeftOffset, this.mPosY
					+ this.mUserValuePainter.mTopOffset,
					this.mUserValuePainter.mPaint);
		}
		// Cage text
		if (!this.mCageText.equals("")) {
			canvas.drawText(this.mCageText, this.mPosX + 2, this.mPosY
					+ mCagePainter.mTextPaint.getTextSize(),
					mCagePainter.mTextPaint);
		}

		// Draw pencilled in digits.
		if (mPossibles.size() > 0) {
			Activity activity = mContext.mContext;
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(activity);
			if (prefs.getBoolean("maybe3x3", true)) {
				for (int i = 0; i < mPossibles.size(); i++) {
					int possible = mPossibles.get(i);
					float xPos = mPosX + mMaybe3x3Painter.mLeftOffset
							+ ((possible - 1) % 3) * mMaybe3x3Painter.mScale;
					float yPos = mPosY + mMaybe3x3Painter.mTopOffset
							+ ((int) (possible - 1) / 3)
							* mMaybe3x3Painter.mScale;
					canvas.drawText(Integer.toString(possible), xPos, yPos,
							this.mMaybe3x3Painter.mTextPaint);
				}
			} else {
				String possibles = "";
				for (int i = 0; i < mPossibles.size(); i++) {
					possibles += Integer.toString(mPossibles.get(i));
				}
				canvas.drawText(possibles,
						mPosX + mMaybe1x9Painter.mLeftOffset, mPosY
								+ mMaybe1x9Painter.mTopOffset,
						mMaybe1x9Painter.mTextPaint);
			}
		}
	}

	/**
	 * Create a string representation of the Grid Cell which can be used to
	 * store a grid cell in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString() {
		String storageString = SAVE_GAME_CELL_VERSION_02
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mCellNumber
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mRow
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mColumn
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mCageText
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mCorrectValue
				+ GameFile.FIELD_DELIMITER_LEVEL1 + mUserValue
				+ GameFile.FIELD_DELIMITER_LEVEL1;
		for (int possible : mPossibles) {
			storageString += possible + GameFile.FIELD_DELIMITER_LEVEL2;
		}
		storageString += GameFile.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mInvalidHighlight)
				+ GameFile.FIELD_DELIMITER_LEVEL1 + Boolean.toString(mCheated)
				+ GameFile.FIELD_DELIMITER_LEVEL1 + Boolean.toString(mSelected);

		return storageString;
	}

	/**
	 * Read cell information from or a storage string which was created with @
	 * GridCell#toStorageString()} before.
	 * 
	 * @param line
	 *            The line containing the cell information.
	 * @return True in case the given line contains cell information and is
	 *         processed correctly. False otherwise.
	 */
	public boolean fromStorageString(String line) {
		String[] cellParts = line.split(GameFile.FIELD_DELIMITER_LEVEL1);

		int cellInformationVersion = 0;
		if (cellParts[0].equals(SAVE_GAME_CELL_VERSION_01)) {
			cellInformationVersion = 1;
		} else if (cellParts[0].equals(SAVE_GAME_CELL_VERSION_02)) {
			cellInformationVersion = 2;
		} else {
			return false;
		}

		// Process all parts
		int index = 1;
		mCellNumber = Integer.parseInt(cellParts[index++]);
		mRow = Integer.parseInt(cellParts[index++]);
		mColumn = Integer.parseInt(cellParts[index++]);
		mCageText = cellParts[index++];
		mCorrectValue = Integer.parseInt(cellParts[index++]);
		mUserValue = Integer.parseInt(cellParts[index++]);
		if ((cellInformationVersion == 1 && index < cellParts.length)
				|| cellInformationVersion > 1) {
			if (!cellParts[index].equals("")) {
				for (String possible : cellParts[index]
						.split(GameFile.FIELD_DELIMITER_LEVEL2)) {
					togglePossible(Integer.parseInt(possible));
				}
			}
			index++;
		}
		if (cellInformationVersion > 1) {
			mInvalidHighlight = Boolean.parseBoolean(cellParts[index++]);
			mCheated = Boolean.parseBoolean(cellParts[index++]);
			mSelected = Boolean.parseBoolean(cellParts[index++]);
		}
		return true;
	}

	public int getCellNumber() {
		return mCellNumber;
	}

	public int getColumn() {
		return mColumn;
	}

	public int getRow() {
		return mRow;
	}

	public int getCorrectValue() {
		return mCorrectValue;
	}

	public void setCorrectValue(int newValue) {
		mCorrectValue = newValue;
	}

	public int getCageId() {
		return mCageId;
	}

	public void setCageId(int newCageId) {
		mCageId = newCageId;
	}

	public void clearCage() {
		mCageId = -1;
		mCageText = "";
	}

	public String getCageText() {
		return mCageText;
	}

	public void setCageText(String newCageText) {
		mCageText = newCageText;
	}

	/**
	 * Saves all information needed to undo a user move on this cell.
	 * 
	 * @param originalCellChange
	 *            Use null in case this cell change is a result of a
	 *            modification made by the user itself. In case the cell is
	 *            changed indirectly as a result of changing another cell, use
	 *            the original cell change.
	 * @return The cell change which is created. This value can be used
	 *         optionally to related other cell changes to this cell change.
	 */
	public CellChange saveUndoInformation(CellChange originalCellChange) {
		// Store old values of this cell
		CellChange move = new CellChange(this, this.mUserValue, this.mPossibles);
		if (originalCellChange == null) {
			// This move is not a result of another move.
			this.mContext.AddMove(move);
		} else {
			originalCellChange.addRelatedMove(move);
		}
		return move;
	}

	public void Undo(int previousUserValue,
			ArrayList<Integer> previousPossibleValues) {
		mUserValue = previousUserValue;
		mPossibles.clear();
		if (previousPossibleValues != null) {
			for (int previousPossibleValue : previousPossibleValues) {
				mPossibles.add(previousPossibleValue);
			}
			Collections.sort(mPossibles);
		}
	}

	public void Select() {
		mContext.SetSelectedCell(this);
	}

	/**
	 * Check if the given digit is registered as a posibble value for this cell.
	 * 
	 * @param digit
	 *            The digit which is to be checked.
	 * @return True in case the digit is registered as a possible value for this
	 *         cell. False otherwise.
	 */
	public boolean hasPossible(int digit) {
		return (this.mPossibles.indexOf(new Integer(digit)) >= 0);
	}

	/**
	 * Confirm that the user has cheated to reveal the content of cell.
	 */
	public void setCheated() {
		this.mCheated = true;
	}
}