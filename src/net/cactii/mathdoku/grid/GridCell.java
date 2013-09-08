package net.cactii.mathdoku.grid;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;

import net.cactii.mathdoku.grid.ui.GridInputMode;
import net.cactii.mathdoku.grid.ui.SwipeMotion;
import net.cactii.mathdoku.painter.CagePainter;
import net.cactii.mathdoku.painter.CellPainter;
import net.cactii.mathdoku.painter.InputModeBorderPainter;
import net.cactii.mathdoku.painter.MaybeValuePainter;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.painter.UserValuePainter;
import net.cactii.mathdoku.statistics.GridStatistics;
import net.cactii.mathdoku.statistics.GridStatistics.StatisticsCounterType;
import net.cactii.mathdoku.storage.database.SolvingAttemptDatabaseAdapter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class GridCell {
	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.GridCell";

	// Each line in the GridFile which contains information about the cell
	// starts with an identifier. This identifier consists of a generic part and
	// the package revision number.
	private static final String SAVE_GAME_CELL_LINE = "CELL";

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
	private final ArrayList<Integer> mPossibles;

	// X pixel position
	public float mPosX;
	// Y pixel position
	public float mPosY;

	private Grid mGrid;

	// Highlight in case a duplicate value is found in row or column
	public boolean mDuplicateValueHighlight;
	// Whether to show cell as selected
	public boolean mSelected;
	// Player revealed this cell
	private boolean mRevealed;
	// Highlight user input isn't correct value
	private boolean mInvalidUserValueHighlight;

	public static enum BorderType {
		NONE, SELECTED__BAD_MATH, SELECTED__GOOD_MATH, NOT_SELECTED__BAD_MATH, NOT_SELECTED__GOOD_MATH
	}

	// Borders of the cell
	public BorderType mBorderTypeTop;
	public BorderType mBorderTypeBottom;
	public BorderType mBorderTypeLeft;
	public BorderType mBorderTypeRight;

	// References to the global painter objects.
	private final CellPainter mCellPainter;
	private final UserValuePainter mUserValuePainter;
	private final MaybeValuePainter mMaybeGridPainter;
	private final MaybeValuePainter mMaybeLinePainter;
	private final CagePainter mCagePainter;
	private final InputModeBorderPainter mInputModeBorderPainter;

	public GridCell(Grid grid, int cell) {
		int gridSize = grid.getGridSize();
		mGrid = grid;
		mCellNumber = cell;
		mColumn = cell % gridSize;
		mRow = cell / gridSize;
		mCageText = "";
		mCageId = -1;
		mCorrectValue = 0;
		mUserValue = 0;
		mDuplicateValueHighlight = false;
		mRevealed = false;
		mInvalidUserValueHighlight = false;
		mPossibles = new ArrayList<Integer>();
		mPosX = 0;
		mPosY = 0;

		// Retrieve all painters
		Painter painter = Painter.getInstance();
		mCellPainter = painter.getCellPainter();
		mUserValuePainter = painter.getUserValuePainter();
		mMaybeGridPainter = painter.getMaybeGridPainter();
		mMaybeLinePainter = painter.getMaybeLinePainter();
		mCagePainter = painter.getCagePainter();
		mInputModeBorderPainter = painter.getInputModeBorderPainter();

		mBorderTypeTop = BorderType.NONE;
		mBorderTypeRight = BorderType.NONE;
		mBorderTypeBottom = BorderType.NONE;
		mBorderTypeLeft = BorderType.NONE;
	}

	@Override
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
			return mCagePainter.getBorderPaint();
		case NOT_SELECTED__BAD_MATH:
			return mCagePainter.getBorderBadMathPaint();
		case SELECTED__GOOD_MATH:
			// In case the grid is deactivated (for example when an unfinished
			// puzzle is displayed in the archive, display the border as if the
			// cage was not selected
			return (mGrid != null && mGrid.isActive() ? mCagePainter
					.getBorderSelectedPaint() : mCagePainter.getBorderPaint());
		case SELECTED__BAD_MATH:
			return (mGrid != null && mGrid.isActive() ? mCagePainter
					.getBorderSelectedBadMathPaint() : mCagePainter
					.getBorderBadMathPaint());
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

	/**
	 * Adds the given digit to the possible values if not yet added before.
	 * 
	 * @param digit
	 *            The digit which has to be added.
	 * @return True in case the digit has been added. False otherwise or in case
	 *         the digit was added before.
	 */
	public boolean addPossible(int digit) {
		return addPossible(digit, true);
	}

	/**
	 * Adds the given digit to the possible values if not yet added before.
	 * 
	 * @param digit
	 *            The digit which has to be added.
	 * @param updateStatistics
	 *            True in case the statistics have to be updated when adding a
	 *            new maybe value. False otherwise.
	 * @return True in case the digit has been added. False otherwise or in case
	 *         the digit was added before.
	 */
	public boolean addPossible(int digit, boolean updateStatistics) {
		if (!hasPossible(digit)) {
			// Add possible value and sort the list of possible values.
			this.mPossibles.add(digit);
			Collections.sort(mPossibles);

			// Update statistics
			if (updateStatistics && mGrid != null) {
				GridStatistics gridStatistics = mGrid.getGridStatistics();
				if (gridStatistics != null) {
					gridStatistics
							.increaseCounter(StatisticsCounterType.POSSIBLES);
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes the given digit from the possible values if it was added before.
	 * 
	 * @param digit
	 *            The digit which has to be removed.
	 * @return True in case the digit has been removed. False otherwise or in
	 *         case the digit was not added before.
	 */
	public boolean removePossible(int digit) {
		if (hasPossible(digit)) {
			this.mPossibles.remove(Integer.valueOf(digit));
			return true;
		} else {
			return false;
		}
	}

	public int getUserValue() {
		return mUserValue;
	}

	public boolean isUserValueSet() {
		return mUserValue != 0;
	}

	/**
	 * Set the user value of the cell to a new value.
	 * 
	 * @param digit
	 *            The new value for the cell. Use 0 to clear the cell.
	 */
	public void setUserValue(int digit) {
		// Update statistics
		if (mGrid != null) {
			GridStatistics gridStatistics = mGrid.getGridStatistics();

			// Only count as replacement as both the original and the new value
			// are not 0 as this is used to indicate an empty cell.
			if (digit != 0 && mUserValue != 0 && digit != mUserValue) {
				gridStatistics
						.increaseCounter(StatisticsCounterType.USER_VALUE_REPLACED);
			}

			// Cell counters are only update if the solution of the cell has
			// not been revealed.
			if (mRevealed == false) {
				gridStatistics
						.decreaseCounter(mUserValue == 0 ? StatisticsCounterType.CELLS_EMPTY
								: StatisticsCounterType.CELLS_FILLED);
				gridStatistics
						.increaseCounter(digit == 0 ? StatisticsCounterType.CELLS_EMPTY
								: StatisticsCounterType.CELLS_FILLED);
			}
		}

		// Remove possibles
		mPossibles.clear();

		// Clear highlight except cheating
		mInvalidUserValueHighlight = false;
		mDuplicateValueHighlight = false;

		// Set new value
		mUserValue = digit;

		// Set borders for this cells and adjacents cells
		setBorders();

		// Check if grid is solved.
		if (mGrid != null) {
			mGrid.checkIfSolved();
		}
	}

	/**
	 * Clear the user value and/or possible values in a cell.
	 */
	public void clear() {
		// Note: setting the userValue to 0 clear the cell but also the possible
		// values!
		setUserValue(0);
	}

	/**
	 * Clear cheat and error flags.
	 */
	public void clearAllFlags() {
		mDuplicateValueHighlight = false;
		mRevealed = false;
		mInvalidUserValueHighlight = false;
	}

	public boolean isUserValueCorrect() {
		return mUserValue == mCorrectValue;
	}

	/* Returns whether the cell is a member of any cage */
	public boolean cellInAnyCage() {
		return mCageId != -1;
	}

	/**
	 * Mark the cell as a cell containing an invalid value.
	 */
	public void setInvalidHighlight() {
		mInvalidUserValueHighlight = true;
	}

	/**
	 * Checks whether the cell is higlighted as invalid. Note: a cell can
	 * contain an invalid value without being marked as invalid. A cell will
	 * only be marked as invalid after using the option "Check Progress".
	 * 
	 * @return True in case the cell has been marked as invalid. False
	 *         otherwise.
	 */
	public boolean hasInvalidUserValueHighlight() {
		return this.mInvalidUserValueHighlight;
	}

	// The next variable could also be declared as local variable in method
	// draw. But is created quite frequently. By reusing it the memory footprint
	// is reduced.
	Paint draw_textPaint = new Paint();

	/**
	 * Draw the cell inclusive borders, background and text.
	 */
	public void draw(Canvas canvas, float gridBorderWidth,
			GridInputMode inputMode, int swipeDigit) {
		// Get cell size
		int cellSize = (int) this.mCellPainter.getCellSize();

		// Calculate x and y for the cell origin (topleft). Use an offset to
		// prevent overlapping of cells and border for entire grid.
		this.mPosX = Math.round(gridBorderWidth + cellSize * this.mColumn);
		this.mPosY = Math.round(gridBorderWidth + cellSize * this.mRow);
		float top = this.mPosY;
		float bottom = this.mPosY + cellSize;
		float left = this.mPosX;
		float right = this.mPosX + cellSize;

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
		borderPaint = getBorderPaint(mBorderTypeTop);
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
		borderPaint = getBorderPaint(mBorderTypeRight);
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
		borderPaint = getBorderPaint(mBorderTypeBottom);
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
		borderPaint = getBorderPaint(mBorderTypeLeft);
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
		// Next the inner borders are drawn for invalid, revealed, duplicate and
		// selected cells.
		// Theoretically multiple borders can be drawn. The less import signals
		// will be drawn first so the most important signal is in the middle of
		// the cell and adjacent to the corresponding background.
		// Order of signals in increasing importance: duplicate, revealed,
		// invalid, selected.
		// ---------------------------------------------------------------------

		for (int i = 1; i <= 4; i++) {
			switch (i) {
			case 1:
				borderPaint = ((mDuplicateValueHighlight && mGrid
						.hasPrefShowDupeDigits()) ? mCellPainter
						.getDuplicateBorderPaint() : null);
				break;
			case 2:
				borderPaint = (mRevealed ? mCellPainter
						.getRevealedBorderPaint() : null);
				break;
			case 3:
				borderPaint = (mInvalidUserValueHighlight ? mCellPainter
						.getInvalidBorderPaint() : null);
				break;
			case 4:
				borderPaint = (mSelected && mGrid != null && mGrid.isActive() ? mCellPainter
						.getSelectedBorderPaint() : null);
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
				top += borderWidth - 1;
				right -= borderWidth - 1;
				bottom -= borderWidth - 1;
				left += borderWidth - 1;
			}
		}

		// ---------------------------------------------------------------------
		// Next the cell background is drawn. Of course only 1 background will
		// be drawn. In case the cell is selected that will be the most
		// important background. In the cell is not selected but we already have
		// drawn a signal border, we will draw the background for the most
		// import signal.
		// Order of signals in increasing importance: selected, invalid,
		// revealed, duplicate.
		// ---------------------------------------------------------------------

		Paint background = null;
		if (mSelected && mGrid != null && mGrid.isActive()) {
			background = mCellPainter.getSelectedBackgroundPaint();
		} else if (mInvalidUserValueHighlight) {
			background = mCellPainter.getInvalidBackgroundPaint();
		} else if (mRevealed) {
			background = mCellPainter.getRevealedBackgroundPaint();
		} else if (mDuplicateValueHighlight && mGrid.hasPrefShowDupeDigits()) {
			background = mCellPainter.getWarningBackgroundPaint();
		}
		if (background != null) {
			canvas.drawRect(left, top, right, bottom, background);
		}

		// Draw cell value.
		// Note: only for the selected cell the swipe digit can have a value
		// other than 0 while a swipe motion is started but not yet finished. In
		// this case the original user value may not be drawn as it will be
		// replace with another definitive value or with a maybe value.
		if ((this.isUserValueSet() && swipeDigit == 0)
				|| (inputMode == GridInputMode.NORMAL && swipeDigit != 0)) {
			// Get the value which will be shown as user value in case the swipe
			// motion will be released at this moment.
			String userValue = Integer
					.toString(inputMode == GridInputMode.NORMAL
							&& swipeDigit != 0 ? swipeDigit : mUserValue);

			Paint paint = (inputMode == GridInputMode.NORMAL ? mUserValuePainter
					.getTextPaintNormalInputMode() : mUserValuePainter
					.getTextPaintMaybeInputMode());

			// Calculate left offset to get the user value centered
			// horizontally.
			int centerOffset = (int) ((cellSize - paint.measureText(userValue)) / 2);

			canvas.drawText(userValue, mPosX + centerOffset, mPosY
					+ mUserValuePainter.getBottomOffset(), paint);
		}
		// Cage text
		if (!this.mCageText.equals("")) {
			// Clone the text painter and decrease text size until the cage text
			// fits within the cell.
			draw_textPaint.set(mCagePainter.getTextPaint());
			float scaleFactor = (cellSize - 4)
					/ draw_textPaint.measureText(mCageText);
			if (scaleFactor < 1) {
				draw_textPaint.setTextSize(draw_textPaint.getTextSize()
						* scaleFactor);
			}

			canvas.drawText(mCageText,
					this.mPosX + mCagePainter.getTextLeftOffset(), this.mPosY
							+ mCagePainter.getTextBottomOffset(),
					draw_textPaint);
		}

		// Draw penciled in digits.
		// Note: only for the selected cell the swipe digit can have a value
		// other than 0 while a swipe motion is started but not yet finished. In
		// this case the swipe digit has to be added (if not yet present) to or
		// removed (if already present) from the possible values of this cell.
		// Note that the original possible values may not be shown in case the
		// content of the cell have to replaced with a new user value.
		if ((mPossibles.size() > 0 && !(inputMode == GridInputMode.NORMAL && swipeDigit != 0))
				|| (inputMode == GridInputMode.MAYBE && swipeDigit != 0)) {
			// Temporary alter the possible values in case a swipe digit is
			// selected.
			if (swipeDigit != 0) {
				if (hasPossible(swipeDigit)) {
					removePossible(swipeDigit);
				} else {
					addPossible(swipeDigit, false);
				}
			}

			// Draw the possible values
			if (mGrid.hasPrefShowMaybesAs3x3Grid()) {
				// Get the digit positioner to be used
				DigitPositionGrid digitPositionGrid = mMaybeGridPainter
						.getDigitPositionGrid();

				// Determine which painter to use
				Paint paint = (inputMode == GridInputMode.NORMAL ? mMaybeGridPainter
						.getTextPaintNormalInputMode() : mMaybeGridPainter
						.getTextPaintMaybeInputMode());

				// Draw all possible which are currently set for this cell.
				for (int i = 0; i < mPossibles.size(); i++) {
					// Get the possible and the specific position in the digit
					// position grid
					int possible = mPossibles.get(i);
					int row = digitPositionGrid.getRow(possible);
					int col = digitPositionGrid.getCol(possible);

					float xPos = mPosX + mMaybeGridPainter.getLeftOffset()
							+ col * mMaybeGridPainter.getMaybeDigitWidth();
					float yPos = mPosY + mMaybeGridPainter.getBottomOffset()
							+ row * mMaybeGridPainter.getMaybeDigitHeight();
					canvas.drawText(Integer.toString(possible), xPos, yPos,
							paint);
				}

			} else {
				// Build string of possible values
				String possiblesText = "";
				for (int i = 0; i < mPossibles.size(); i++) {
					possiblesText += Integer.toString(mPossibles.get(i));
				}

				// Clone the text painter and decrease text size until the
				// possible values string fit within the cell.
				draw_textPaint
						.set(inputMode == GridInputMode.NORMAL ? mMaybeLinePainter
								.getTextPaintNormalInputMode()
								: mMaybeLinePainter
										.getTextPaintMaybeInputMode());
				float scaleFactor = (cellSize - 2 * mMaybeLinePainter
						.getLeftOffset())
						/ draw_textPaint.measureText(possiblesText);
				if (scaleFactor < 1) {
					draw_textPaint.setTextSize(draw_textPaint.getTextSize()
							* scaleFactor);
				}

				// Calculate addition left offset to get the maybe values
				// centered horizontally.
				int centerOffset = (int) ((cellSize - draw_textPaint
						.measureText(possiblesText)) / 2);

				canvas.drawText(possiblesText, mPosX + centerOffset, mPosY
						+ mMaybeLinePainter.getBottomOffset(), draw_textPaint);
			}

			// Undo the temporary change to the possible values
			if (swipeDigit != 0) {
				if (hasPossible(swipeDigit)) {
					removePossible(swipeDigit);
				} else {
					addPossible(swipeDigit, false);
				}
			}
		}
	}

	// The next variable could also be declared as local variable in method
	// drawSwipeOverlay. But is created quite frequently. By reusing it the
	// memory footprint is reduced.
	static Rect drawSwipeOverlay_bounds = new Rect();

	/**
	 * Draw the swipe overlay for the selected cell.
	 */
	public void drawSwipeOverlay(Canvas canvas, float gridBorderWidth,
			GridInputMode inputMode, float mXPosSwipe, float mYPosSwipe,
			int swipeDigit, boolean outerSwipeCircleVisible) {
		assert (inputMode == GridInputMode.NORMAL || inputMode == GridInputMode.MAYBE);
		if (mGrid.getSelectedCell() != this) {
			// This cell is not the selected cell.
			return;
		}

		// Get cell size
		int cellSize = (int) this.mCellPainter.getCellSize();

		// Calculate x and y for the cell origin (top left). Use an offset to
		// prevent overlapping of cells and border for entire grid.
		this.mPosX = Math.round(gridBorderWidth + cellSize * this.mColumn);
		this.mPosY = Math.round(gridBorderWidth + cellSize * this.mRow);
		float top = this.mPosY;
		float left = this.mPosX;

		// Get the painters for the overlay border
		// Determine which painter to use
		Paint borderPaint = (inputMode == GridInputMode.NORMAL ? mInputModeBorderPainter
				.getNormalInputModeBorderPaint() : mInputModeBorderPainter
				.getMaybeInputModeBorderPaint());
		float borderOverlayWidth = borderPaint.getStrokeWidth();
		Paint segmentSeparatorPaint = mInputModeBorderPainter
				.getSegmentDivider();
		Paint textNormalPaint = mInputModeBorderPainter.getNormalTextPaint();
		Paint textHighlightedPaint = mInputModeBorderPainter
				.getHighlightedTextPaint();

		// Get the size of the grid as all digits up to grid size have to be
		// drawn in the swipe circle.
		int gridSize = mGrid.getGridSize();

		// Draw the swipe border background
		int centerX = (int) (left + cellSize / 2);
		int centerY = (int) (top + (cellSize / 2));

		// Define helper variables outside loop
		double radiusOffset;
		int angle;
		float offsetX;
		float offsetY;

		// Draw the swipe circles
		for (int circle = 1; circle <= (outerSwipeCircleVisible ? 2 : 1); circle++) {
			float radius = cellSize * circle;
			if (borderPaint != null) {
				canvas.drawCircle(centerX, centerY, radius
						- (borderOverlayWidth / 2) - 2, borderPaint);
			}

			// Plot all applicable digits clockwise in the swipe circle.
			for (int i = 1; i <= gridSize; i++) {
				// Determine the minimal space needed to draw the digit.
				textNormalPaint.getTextBounds(Integer.toString(i), 0, 1,
						drawSwipeOverlay_bounds);

				// Determine the offset for which the radius has to be correct
				// to get to the center of the space needed to draw the digit.
				radiusOffset = Math
						.sqrt((drawSwipeOverlay_bounds.height() * drawSwipeOverlay_bounds
								.height())
								+ (drawSwipeOverlay_bounds.width() * drawSwipeOverlay_bounds
										.width())) / 2;

				// Determine the point at which the center of the digit has to
				// placed.
				angle = SwipeMotion.getAngleCenterSwipeSegment(i);
				offsetX = (int) (Math.cos(Math.toRadians(angle)) * (radius - radiusOffset));
				offsetY = (int) (Math.sin(Math.toRadians(angle)) * (radius - radiusOffset));

				// Find the lower left corner of the space in which the digit
				// has to
				// be drawn.
				offsetX += centerX - (drawSwipeOverlay_bounds.width() / 2);
				offsetY += centerY + (drawSwipeOverlay_bounds.height() / 2);

				// Draw the text at the lower left corner
				canvas.drawText(Integer.toString(i), offsetX, offsetY,
						(i == swipeDigit ? textHighlightedPaint
								: textNormalPaint));
			}

			// Draw separator lines between the segments of the swipe circle
			for (int i = 0; i <= gridSize; i++) {
				angle = SwipeMotion.getAngleToNextSwipeSegment(i);
				canvas.drawLine(
						centerX
								+ (int) (Math.cos(Math.toRadians(angle)) * radius),
						centerY
								+ (int) (Math.sin(Math.toRadians(angle)) * radius),
						centerX
								+ (int) (Math.cos(Math.toRadians(angle)) * (radius - borderOverlayWidth)),
						centerY
								+ (int) (Math.sin(Math.toRadians(angle)) * (radius - borderOverlayWidth)),
						segmentSeparatorPaint);
			}
		}

		// Redraw the cell including the content which results as the swipe
		// motion is released at the current position.
		draw(canvas, gridBorderWidth, inputMode, (swipeDigit >= 1
				&& swipeDigit <= gridSize ? swipeDigit : 0));

		// Draw a line from the middle of the selected cell to the current swipe
		// position to indicate which digit will be selected on release.
		canvas.drawLine(left + (cellSize / 2), top + (cellSize / 2),
				mXPosSwipe, mYPosSwipe,
				mInputModeBorderPainter.getSwipeLinePaint());
	}

	/**
	 * Draw the copy overlay for the selected cell.
	 * 
	 * @param canvas
	 * @param gridBorderWidth
	 * @param inputMode
	 *            The input mode of the entire grid.
	 * @param inputModeSelectedCell
	 *            The input mode to be used for drawing the content of the
	 *            selected cell.
	 * @param mXPosSwipe
	 * @param mYPosSwipe
	 */
	public void drawCopyOverlay(Canvas canvas, float gridBorderWidth,
			GridInputMode inputMode, GridInputMode inputModeSelectedCell,
			float mXPosSwipe, float mYPosSwipe) {
		assert (inputMode == GridInputMode.COPY);
		if (mGrid.getSelectedCell() != this) {
			// This cell is not the selected cell.
			return;
		}

		// Get cell size
		int cellSize = (int) this.mCellPainter.getCellSize();

		// Calculate x and y for the cell origin (top left). Use an offset to
		// prevent overlapping of cells and border for entire grid.
		this.mPosX = Math.round(gridBorderWidth + cellSize * this.mColumn);
		this.mPosY = Math.round(gridBorderWidth + cellSize * this.mRow);
		float top = this.mPosY;
		float left = this.mPosX;

		// Get the painters for the overlay border
		// Determine which painter to use
		Paint borderPaint = mInputModeBorderPainter
				.getCopyBackgroundBorderPaint();
		float borderOverlayWidth = borderPaint.getStrokeWidth();
		Paint textNormalPaint = mInputModeBorderPainter.getNormalTextPaint();

		// Get the size of the grid as all digits up to grid size have to be
		// drawn in the swipe circle.
		int gridSize = mGrid.getGridSize();

		// Draw the swipe border background
		int centerX = (int) (left + cellSize / 2);
		int centerY = (int) (top + (cellSize / 2));

		// Draw the border
		float radius = cellSize;
		if (borderPaint != null) {
			canvas.drawCircle(centerX, centerY, radius
					- (borderOverlayWidth / 2) - 2, borderPaint);
		}

		// TODO Plot help text clockwise in the circle

		// Redraw the cell including the content which results as the cell is
		// tapped in copy mode.
		draw(canvas, gridBorderWidth, inputModeSelectedCell, 0);
	}

	/**
	 * Create a string representation of the Grid Cell which can be used to
	 * store a grid cell in a saved game.
	 * 
	 * @return A string representation of the grid cell.
	 */
	public String toStorageString() {
		String storageString = SAVE_GAME_CELL_LINE
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mCellNumber
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + mRow
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mColumn
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mCageText
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mCorrectValue
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ mUserValue
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1;
		for (int possible : mPossibles) {
			storageString += possible
					+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2;
		}
		storageString += SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mInvalidUserValueHighlight)
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mRevealed)
				+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
				+ Boolean.toString(mSelected);

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
	public boolean fromStorageString(String line, int savedWithRevisionNumber) {
		String[] cellParts = line
				.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1);

		// Only process the storage string if it starts with the correct
		// identifier.
		if (cellParts[0].equals(SAVE_GAME_CELL_LINE) == false) {
			return false;
		}

		// When upgrading to MathDoku v2 the history is not converted. As of
		// revision 369 all logic for handling games stored with older versions
		// is removed.
		if (savedWithRevisionNumber <= 368) {
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

		// Get possible values
		if (!cellParts[index].equals("")) {
			for (String possible : cellParts[index]
					.split(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
				addPossible(Integer.parseInt(possible), false);
			}
		}
		index++;

		mInvalidUserValueHighlight = Boolean.parseBoolean(cellParts[index++]);
		mRevealed = Boolean.parseBoolean(cellParts[index++]);
		mSelected = Boolean.parseBoolean(cellParts[index++]);

		return true;
	}

	public int getCellNumber() {
		return mCellNumber;
	}

	/**
	 * The column number (zero based) of the cell.
	 * 
	 * @return
	 */
	public int getColumn() {
		return mColumn;
	}

	/**
	 * The row number (zero based) of the cell.
	 * 
	 * @return
	 */
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
			this.mGrid.addMove(move);
		} else {
			originalCellChange.addRelatedMove(move);
		}
		return move;
	}

	public void undo(int previousUserValue,
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
		return (this.mPossibles.indexOf(Integer.valueOf(digit)) >= 0);
	}

	/**
	 * Confirm that the user has revealed the content of the cell.
	 */
	public void setRevealed() {
		// Correct grid statistics
		if (mRevealed == false && mGrid != null) {
			GridStatistics gridStatistics = mGrid.getGridStatistics();
			gridStatistics
					.decreaseCounter(isUserValueSet() ? StatisticsCounterType.CELLS_FILLED
							: StatisticsCounterType.CELLS_EMPTY);
			gridStatistics
					.increaseCounter(StatisticsCounterType.CELLS_REVEALED);
		}

		mRevealed = true;
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

	// The next variable could also be declared as local variable in method
	// drawDashedLine. But is created quite frequently. By reusing it the
	// memory footprint is reduced.
	Path drawDashedLine_path = new Path();

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
		drawDashedLine_path.reset();
		drawDashedLine_path.moveTo(left, top);
		drawDashedLine_path.lineTo(right, bottom);
		canvas.drawPath(drawDashedLine_path,
				mCellPainter.getUnusedBorderPaint());
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
		mBorderTypeTop = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.mBorderTypeBottom = mBorderTypeTop;
		}

		// Set right border for this cell and the left border for the cell on
		// the right
		otherCell = getCellOnRight();
		mBorderTypeRight = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.mBorderTypeLeft = mBorderTypeRight;
		}

		// Set bottom border for this cell and the top border for the cell below
		otherCell = getCellBelow();
		mBorderTypeBottom = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.mBorderTypeTop = mBorderTypeBottom;
		}

		// Set left border for this cell and the right border for the cell on
		// the left
		otherCell = getCellOnLeft();
		mBorderTypeLeft = getCommonBorderType(this, otherCell);
		if (otherCell != null) {
			otherCell.mBorderTypeRight = mBorderTypeLeft;
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

	/**
	 * Checks if this cell is emtpy, i.e. it does not contain a user value nor
	 * possible values.
	 * 
	 * @return True in case the cell is empty. False otherwise.
	 */
	public boolean isEmpty() {
		return (mUserValue == 0 && mPossibles.size() == 0);
	}

	/**
	 * Get the coordinates of the center of the cell.
	 * 
	 * @param gridBorderWidth
	 *            The width of the border for which has to be corrected.
	 * @return The (x,y) coordinated of the center of the cell.
	 */
	public float[] getCellCentreCoordinates(float gridBorderWidth) {
		// Get cell size
		int cellSize = (int) this.mCellPainter.getCellSize();

		float top = Math.round(gridBorderWidth + cellSize * this.mRow);
		// float bottom = this.mPosY + cellSize;
		float left = Math.round(gridBorderWidth + cellSize * this.mColumn);
		// float right = this.mPosX + cellSize;

		return new float[] { left + (cellSize / 2), top + (cellSize / 2) };
	}

	/**
	 * Set the duplicate highlight of the cell.
	 * 
	 * @param higlighted
	 *            True in case the duplicate highlight is visble. False
	 *            otherwise.
	 */
	public void setDuplicateHighlight(boolean highlight) {
		mDuplicateValueHighlight = highlight;
	}

	/**
	 * Checks is the user value of the cell was revealed.
	 * 
	 * @return True in case the cell has been revealed. False otherwise.
	 */
	public boolean isRevealed() {
		return mRevealed;
	}
}