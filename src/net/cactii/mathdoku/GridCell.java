package net.cactii.mathdoku;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;

import net.cactii.mathdoku.MainActivity.InputMode;
import net.cactii.mathdoku.Painter.CagePainter;
import net.cactii.mathdoku.Painter.CellPainter;
import net.cactii.mathdoku.Painter.Maybe1x9Painter;
import net.cactii.mathdoku.Painter.Maybe3x3Painter;
import net.cactii.mathdoku.Painter.UserValuePainter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class GridCell {
	@SuppressWarnings("unused")
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

	private Grid mGrid;

	// Whether to show warning background (duplicate value in row/col)
	public boolean mShowWarning;
	// Whether to show cell as selected
	public boolean mSelected;
	// Player cheated (revealed this cell)
	private boolean mCheated;
	// Highlight user input isn't correct value
	private boolean mInvalidHighlight;

	public static enum BorderType {
		NONE, SELECTED__BAD_MATH, SELECTED__GOOD_MATH, NOT_SELECTED__BAD_MATH, NOT_SELECTED__GOOD_MATH
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

	public GridCell(Grid grid, int cell) {
		int gridSize = grid.getGridSize();
		this.mGrid = grid;
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

	private Paint getBorderPaint(BorderType borderType) {
		switch (borderType) {
		case NONE:
			return null;
		case NOT_SELECTED__GOOD_MATH:
			return mCagePainter.mBorderPaint;
		case NOT_SELECTED__BAD_MATH:
			return mCagePainter.mBorderBadMathPaint;
		case SELECTED__GOOD_MATH:
			return mCagePainter.mBorderSelectedPaint;
		case SELECTED__BAD_MATH:
			return mCagePainter.mBorderSelectedBadMathPaint;
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

		// Check cage maths
		mGrid.mCages.get(mCageId).checkCageMathsCorrect(false);

		// Set borders for this cells and adjacents cells
		setBorders();

		// Check if grid is solved.
		if (mGrid != null) {
			mGrid.checkIfSolved();
		}
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

	/**
	 * Draw the cell inclusive borders, background and text.
	 */
	public void draw(Canvas canvas, float gridBorderWidth,
			MainActivity.InputMode inputMode) {
		// Calculate x and y for the cell origin (topleft). Use an offset to
		// prevent overlapping of cells and border for entire grid.
		this.mPosX = Math.round(gridBorderWidth + this.mCellPainter.mCellSize
				* this.mColumn);
		this.mPosY = Math.round(gridBorderWidth + this.mCellPainter.mCellSize
				* this.mRow);
		float top = this.mPosY;
		float bottom = this.mPosY + this.mCellPainter.mCellSize;
		float left = this.mPosX;
		float right = this.mPosX + this.mCellPainter.mCellSize;

		// ---------------------------------------------------------------------
		// Draw cage borders first. In case a cell border is part of the cage
		// border it might be necessary to extend the border into an adjacent
		// cell to get a straight corner. Per border it has to be checked if the
		// cell border overlaps with the cage border.
		// IMPORTANT: Transparent cage borders are not correctly supported as
		// overlapping borders will lead to a slightly darker color.
		// ---------------------------------------------------------------------
		boolean cellOnLeftIsInSameCage = isInSameCageAsCell(this.mRow,
				this.mColumn - 1);
		boolean cellOnRightIsInSameCage = isInSameCageAsCell(this.mRow,
				this.mColumn + 1);
		boolean cellAboveIsInSameCage = isInSameCageAsCell(this.mRow - 1,
				this.mColumn);
		boolean cellBelowIsInSameCage = isInSameCageAsCell(this.mRow + 1,
				this.mColumn);

		Paint borderPaint;

		// Top border of cell (will only be drawn for first row
		float topOffset = 0;
		borderPaint = getBorderPaint(borderTypeTop);
		if (borderPaint != null) {
			// Calculate offset and draw top border
			float offset = (mRow == 0 ? (float) Math
					.floor((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas.drawLine(left - (cellOnLeftIsInSameCage ? offset : 0), top
					+ offset, right + (cellOnRightIsInSameCage ? offset : 0),
					top + offset, borderPaint);

			// Calculate offset for inner space after drawing top border
			topOffset = (float) Math
					.floor((float) ((mRow == 0 ? 1 : 0.5) * borderPaint
							.getStrokeWidth()));
		}

		// Right border of cell
		borderPaint = getBorderPaint(borderTypeRight);
		float rightOffset = 0;
		if (borderPaint != null) {
			// Calculate offset and draw right border
			float offset = (mColumn == mGrid.getGridSize() - 1 ? (float) Math
					.ceil((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas.drawLine(right - offset, top
					- (cellAboveIsInSameCage ? offset : 0), right - offset,
					bottom + (cellBelowIsInSameCage ? offset : 0), borderPaint);

			// Calculate offset for inner space after drawing right border
			rightOffset = (float) Math.floor((float) ((mColumn == mGrid
					.getGridSize() - 1 ? 1 : 0.5) * borderPaint
					.getStrokeWidth()));
		} else {
			// Due to a bug
			// (https://code.google.com/p/android/issues/detail?id=29944), a
			// dashed line can not be drawn with drawLine at API-level 11 or
			// above.
			drawDashedLine(canvas, right, top, right, bottom);
		}

		// Bottom border of cell
		borderPaint = getBorderPaint(borderTypeBottom);
		float bottomOffset = 0;
		if (borderPaint != null) {
			// Calculate offset and draw bottom border
			float offset = (mRow == mGrid.getGridSize() - 1 ? (float) Math
					.ceil((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas.drawLine(left - (cellOnLeftIsInSameCage ? offset : 0),
					bottom - offset, right
							+ (cellOnRightIsInSameCage ? offset : 0), bottom
							- offset, borderPaint);

			// Calculate offset for inner space after drawing bottom border
			bottomOffset = (float) Math.floor((float) ((mRow == mGrid
					.getGridSize() - 1 ? 1 : 0.5) * borderPaint
					.getStrokeWidth()));
		} else {
			// Due to a bug
			// (https://code.google.com/p/android/issues/detail?id=29944), a
			// dashed line can not be drawn with drawLine at API-level 11 or
			// above.
			drawDashedLine(canvas, left, bottom, right, bottom);
		}

		// Left border of cell (will only be draw for first column
		float leftOffset = 0;
		borderPaint = getBorderPaint(borderTypeLeft);
		if (borderPaint != null) {
			// Calculate offset and draw left border
			float offset = (mColumn == 0 ? (float) Math
					.floor((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas.drawLine(left + offset, top
					- (cellAboveIsInSameCage ? offset : 0), left + offset,
					bottom + (cellBelowIsInSameCage ? offset : 0), borderPaint);

			// Calculate offset for inner space after drawing left border
			leftOffset = (float) Math
					.floor((float) ((mColumn == 0 ? 1 : 0.5) * borderPaint
							.getStrokeWidth()));
		}

		// Calculate new offsets with respect to space used by cell border.
		top += topOffset;
		right -= rightOffset;
		bottom -= bottomOffset;
		left += leftOffset;

		// ---------------------------------------------------------------------
		// Next the inner borders are drawn for invalid, cheated and warnings.
		// Theoretically multiple borders can be drawn. The less import signals
		// will be drawn first so the most important signal is in the middle of
		// the cell and adjacent to the corresponding background.
		// Order of signals in increasing importance: warning, cheated, invalid,
		// selected.
		// ---------------------------------------------------------------------

		for (int i = 1; i <= 4; i++) {
			switch (i) {
			case 1:
				borderPaint = ((mShowWarning && mGrid.hasPrefShowDupeDigits()) ? mCellPainter.mWarning.mBorderPaint
						: null);
				break;
			case 2:
				borderPaint = (mCheated ? mCellPainter.mCheated.mBorderPaint
						: null);
				break;
			case 3:
				borderPaint = (mInvalidHighlight ? mCellPainter.mInvalid.mBorderPaint
						: null);
				break;
			case 4:
				borderPaint = (mSelected ? mCellPainter.mSelected.mBorderPaint
						: null);
				break;
			}
			if (borderPaint != null) {
				// Draw this border
				float borderWidth = borderPaint.getStrokeWidth();
				float borderOffset = (float) Math
						.ceil((float) (0.5 * borderWidth));

				// For support of transparent borders it has to be avoided that
				// lines do overlap.
				canvas.drawLine(left, top + borderOffset, right - borderWidth,
						top + borderOffset, borderPaint);
				canvas.drawLine(right - borderOffset, top,
						right - borderOffset, bottom - borderWidth, borderPaint);
				canvas.drawLine(left + borderWidth, bottom - borderOffset,
						right, bottom - borderOffset, borderPaint);
				canvas.drawLine(left + borderOffset, top + borderWidth, left
						+ borderOffset, bottom, borderPaint);
				top += borderWidth;
				right -= borderWidth;
				bottom -= borderWidth;
				left += borderWidth;
			}
		}

		// ---------------------------------------------------------------------
		// Next the cell background is drawn. Of course only 1 background will
		// be drawn. In case the cell is selected that will be the most
		// important background. In the cell is not selected but we already have
		// drawn a signal border, we will draw the background for the most
		// import signal.
		// Order of signals in increasing importance: warning, cheated, invalid.
		// ---------------------------------------------------------------------

		Paint background = null;
		if (mSelected) {
			background = mCellPainter.mSelected.mBackgroundPaint;
		} else if (mInvalidHighlight) {
			background = mCellPainter.mInvalid.mBackgroundPaint;
		} else if (mCheated) {
			background = mCellPainter.mCheated.mBackgroundPaint;
		} else if (mShowWarning && mGrid.hasPrefShowDupeDigits()) {
			background = mCellPainter.mWarning.mBackgroundPaint;
		}
		if (background != null) {
			canvas.drawRect(left, top, right, bottom, background);
		}

		// Cell value
		if (this.isUserValueSet()) {
			Paint paint = (inputMode == inputMode.NORMAL ? mUserValuePainter.mTextPaintNormalInputMode
					: mUserValuePainter.mTextPaintMaybeInputMode);
			canvas.drawText("" + mUserValue, mPosX
					+ mUserValuePainter.mLeftOffset, mPosY
					+ mUserValuePainter.mTopOffset, paint);
		}
		// Cage text
		if (!this.mCageText.equals("")) {
			// Clone the text painter and decrease text size until the cage text fits within the cell.
			Paint textPaint = new Paint(mCagePainter.mTextPaint);
			float scaleFactor = (mCellPainter.mCellSize - 4) / textPaint.measureText(mCageText);
			if (scaleFactor < 1) {
				textPaint.setTextSize(mCagePainter.mTextPaint.getTextSize() * scaleFactor);
			}
			
			canvas.drawText(mCageText, this.mPosX + 2, this.mPosY
					+ mCagePainter.mTextPaint.getTextSize(),
					textPaint);
		}

		// Draw pencilled in digits.
		if (mPossibles.size() > 0) {
			if (mGrid.hasPrefShowMaybesAs3x3Grid()) {
				Paint paint = (inputMode == InputMode.NORMAL ? mMaybe3x3Painter.mTextPaintNormalInputMode
						: mMaybe3x3Painter.mTextPaintMaybeInputMode);
				for (int i = 0; i < mPossibles.size(); i++) {
					int possible = mPossibles.get(i);
					float xPos = mPosX + mMaybe3x3Painter.mLeftOffset
							+ ((possible - 1) % 3) * mMaybe3x3Painter.mScale;
					float yPos = mPosY + mMaybe3x3Painter.mTopOffset
							+ ((int) (possible - 1) / 3)
							* mMaybe3x3Painter.mScale;
					canvas.drawText(Integer.toString(possible), xPos, yPos,
							paint);
				}
			} else {
				Paint paint = (inputMode == InputMode.NORMAL ? mMaybe1x9Painter.mTextPaintNormalInputMode
						: mMaybe1x9Painter.mTextPaintMaybeInputMode);
				String possibles = "";
				for (int i = 0; i < mPossibles.size(); i++) {
					possibles += Integer.toString(mPossibles.get(i));
				}
				canvas.drawText(possibles,
						mPosX + mMaybe1x9Painter.mLeftOffset, mPosY
								+ mMaybe1x9Painter.mTopOffset, paint);
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

	public GridCage getCage() {
		return (mGrid == null ? null : mGrid.mCages.get(mCageId));
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
			this.mGrid.AddMove(move);
		} else {
			originalCellChange.addRelatedMove(move);
		}
		return move;
	}

	public void Undo(int previousUserValue,
			ArrayList<Integer> previousPossibleValues) {
		setUserValue(previousUserValue);
		if (previousPossibleValues != null) {
			for (int previousPossibleValue : previousPossibleValues) {
				mPossibles.add(previousPossibleValue);
			}
			Collections.sort(mPossibles);
		}
	}

	public void select() {
		mGrid.setSelectedCell(this);
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

	/**
	 * Sets the reference to the grid to which this cell belongs.
	 * 
	 * @param grid
	 *            The grid the cell should refer to.
	 */
	public void setGridReference(Grid grid) {
		mGrid = grid;
	}

	public void checkWithOtherValuesInRowAndColumn() {
		if (isUserValueSet()) {
			if (mGrid.getNumValueInCol(this) > 1
					|| mGrid.getNumValueInRow(this) > 1) {
				// Value has been used in another cell in the same row or
				// column.
				mShowWarning = true;
				return;
			}
		}
		mShowWarning = false;
	}

	/**
	 * Checks whether this cell is part of the currently selected cage.
	 * 
	 * @return True in case this cell is part of the currently selected cage.
	 *         False otherwise.
	 */
	public boolean isCellInSelectedCage() {
		if (mGrid.getSelectedCell() == null) {
			// When no cell is selected, a cage isn't selected as well.
			return false;
		}

		return (mGrid.getCageForSelectedCell().mId == mCageId);
	}

	/**
	 * Checks whether this cell is part of the same cage as the cell at the
	 * given coordinates.
	 * 
	 * @param row
	 *            Row number (zero based) of cell to compare with.
	 * @param column
	 *            Column number (zero based) of cell to compare with.
	 * @return True in case cells are part of same cage. False otherwise.
	 */
	public boolean isInSameCageAsCell(int row, int column) {
		GridCell cell = this.mGrid.getCellAt(row, column);
		if (cell != null && cell.getCageId() == this.mCageId) {
			return true;
		}
		return false;
	}

	/**
	 * Draws a dashed line.
	 * 
	 * Due to a bug (https://code.google.com/p/android/issues/detail?id=29944),
	 * a dashed line can not be drawn with drawLine at API-level 11 or above.
	 * 
	 * @param canvas
	 *            The canvas on which will be drawed.
	 * @param left
	 *            Starting X position.
	 * @param top
	 *            Starting Y position.
	 * @param right
	 *            Ending X position.
	 * @param bottom
	 *            Ending Y position.
	 */
	private void drawDashedLine(Canvas canvas, float left, float top,
			float right, float bottom) {
		Path path = new Path();
		path.moveTo(left, top);
		path.lineTo(right, bottom);
		canvas.drawPath(path, mCellPainter.mUnusedBorderPaint);
	}

	public GridCell getCellAbove() {
		return mGrid.getCellAt(mRow - 1, mColumn);
	}

	public GridCell getCellOnRight() {
		return mGrid.getCellAt(mRow, mColumn + 1);
	}

	public GridCell getCellBelow() {
		return mGrid.getCellAt(mRow + 1, mColumn);
	}

	public GridCell getCellOnLeft() {
		return mGrid.getCellAt(mRow, mColumn - 1);
	}

	/**
	 * Sets the borders for this cell and cells adjacent to this cell.
	 */
	public void setBorders() {
		// Set top border for this cell and the bottom border for the cell above
		GridCell otherCell = getCellAbove();
		borderTypeTop = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.borderTypeBottom = borderTypeTop;
		}

		// Set right border for this cell and the left border for the cell on
		// the right
		otherCell = getCellOnRight();
		borderTypeRight = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.borderTypeLeft = borderTypeRight;
		}

		// Set bottom border for this cell and the top border for the cell below
		otherCell = getCellBelow();
		borderTypeBottom = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.borderTypeTop = borderTypeBottom;
		}

		// Set left border for this cell and the right border for the cell on
		// the left
		otherCell = getCellOnLeft();
		borderTypeLeft = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.borderTypeRight = borderTypeLeft;
		}
	}

	/**
	 * Determines for two (adjacent) cells what border type to use for the
	 * border between those cells. It is not checked whether two cell are really
	 * adjacent.
	 * 
	 * @param cell1
	 *            First cell.
	 * @param cell2
	 *            Second cell. This cell may be null in which case only the
	 *            first cell will be used to determine the correct border.
	 * @return The border type to be used between the given cell.
	 */
	private BorderType getCommonBorderType(GridCell cell1, GridCell cell2) {
		if (cell1 == null) {
			throw new InvalidParameterException(
					"Method getMostImportantBorderType can not be called with "
							+ "parameter cell1 equals null.");
		}

		// If both cells are part of the same cage there will be no border.
		if (cell2 != null && cell1.getCage().equals(cell2.getCage())) {
			return BorderType.NONE;
		}

		// If cell1 is part of the selected cage, it status is more important
		// than status of cell 2.
		if (cell1.isCellInSelectedCage()) {
			if (!cell1.getCage().mUserMathCorrect
					&& mGrid.hasPrefShowBadCageMaths()) {
				return BorderType.SELECTED__BAD_MATH;
			} else {
				return BorderType.SELECTED__GOOD_MATH;
			}
		}

		// If cell1 is not part of the selected cage, than status of cell2 will
		// prevail in case it is part of the selected cage.
		if (cell2 != null && cell2.isCellInSelectedCage()) {
			if (!cell2.getCage().mUserMathCorrect
					&& mGrid.hasPrefShowBadCageMaths()) {
				return BorderType.SELECTED__BAD_MATH;
			} else {
				return BorderType.SELECTED__GOOD_MATH;
			}
		}

		// Both cells are in a cage which is not selected.
		if ((!cell1.getCage().mUserMathCorrect || (cell2 != null && !cell2
				.getCage().mUserMathCorrect))
				&& mGrid.hasPrefShowBadCageMaths()) {
			return BorderType.NOT_SELECTED__BAD_MATH;
		} else {
			return BorderType.NOT_SELECTED__GOOD_MATH;
		}
	}
}