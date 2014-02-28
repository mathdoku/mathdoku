package net.mathdoku.plus.puzzle.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import net.mathdoku.plus.painter.CagePainter;
import net.mathdoku.plus.painter.CellPainter;
import net.mathdoku.plus.painter.InputModeBorderPainter;
import net.mathdoku.plus.painter.MaybeValuePainter;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.painter.UserValuePainter;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.digitpositiongrid.DigitPositionGrid;
import net.mathdoku.plus.puzzle.grid.Grid;

import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.List;

public class CellDrawer {
	private final GridViewerView mGridViewerView;
	private Cell mCell;
	private Grid mGrid;
	private int mColumn;
	private int mRow;
	private float mPosX;
	private float mPosY;

	private static enum BorderType {
		NONE, CELL_IN_SELECTED_CAGE_WITH_BAD_MATH, CELL_IN_SELECTED_CAGE_WITH_GOOD_MATH, CELL_IN_UNSELECTED_CAGE_WITH_BAD_MATH, CELL_IN_UNSELECTED_CAGE_WITH_GOOD_MATH
	}

	// Which cells are adjacent to the Cell which is drawn by this
	// CellDrawer and is this adjacent cell in the same cage.
	private static enum AdjacentPosition {
		CELL_ABOVE(0), CELL_TO_RIGHT(1), CELL_BELOW(2), CELL_TO_LEFT(3);

		public final int mIndex;
		private AdjacentPosition mOppositeAdjacentPosition;

		static {
			CELL_ABOVE.mOppositeAdjacentPosition = CELL_BELOW;
			CELL_TO_RIGHT.mOppositeAdjacentPosition = CELL_TO_LEFT;
			CELL_BELOW.mOppositeAdjacentPosition = CELL_ABOVE;
			CELL_TO_LEFT.mOppositeAdjacentPosition = CELL_TO_RIGHT;
		}

		private AdjacentPosition(int index) {
			mIndex = index;
		}

		public AdjacentPosition getOppositeAdjacentPosition() {
			return mOppositeAdjacentPosition;
		}
	}

	static int mNumberOfAdjacentPositions = AdjacentPosition.values().length;

	private Cell[] mAdjacentCells;
	private CellDrawer[] mAdjacentCellDrawers;
	private boolean[] mIsInSameCageAsAdjacentCell;
	private BorderType[] mCommonBorderTypeWithAdjacentCell;

	private DigitPositionGrid mDigitPositionGrid;

	// References to the global painter objects.
	private CellPainter mCellPainter;
	private UserValuePainter mUserValuePainter;
	private MaybeValuePainter mMaybeGridPainter;
	private MaybeValuePainter mMaybeLinePainter;
	private CagePainter mCagePainter;
	private InputModeBorderPainter mInputModeBorderPainter;

	public CellDrawer(GridViewerView gridViewerView, Cell cell) {
		mGridViewerView = gridViewerView;
		mCell = cell;
		mGrid = cell.getGrid();
		mRow = cell.getRow();
		mColumn = cell.getColumn();

		mAdjacentCells = new Cell[mNumberOfAdjacentPositions];
		mAdjacentCellDrawers = new CellDrawer[mNumberOfAdjacentPositions];
		mIsInSameCageAsAdjacentCell = new boolean[mNumberOfAdjacentPositions];

		initializePainters();
		initializeCommonBorderTypeWithAdjacentCell();
	}

	private void initializePainters() {
		Painter painter = Painter.getInstance();
		mCellPainter = painter.getCellPainter();
		mUserValuePainter = painter.getUserValuePainter();
		mMaybeGridPainter = painter.getMaybeGridPainter();
		mMaybeLinePainter = painter.getMaybeLinePainter();
		mCagePainter = painter.getCagePainter();
		mInputModeBorderPainter = painter.getInputModeBorderPainter();
	}

	private void initializeCommonBorderTypeWithAdjacentCell() {
		mCommonBorderTypeWithAdjacentCell = new BorderType[mNumberOfAdjacentPositions];
		for (int i = 0; i < mIsInSameCageAsAdjacentCell.length; i++) {
			mCommonBorderTypeWithAdjacentCell[i] = BorderType.NONE;
		}
	}

	public void setReferencesToCellAbove(Cell cellAbove,
			CellDrawer cellDrawerAbove) {
		setReferencesToCell(AdjacentPosition.CELL_ABOVE, cellAbove,
				cellDrawerAbove);
	}

	public void setReferencesToCellToRight(Cell cellToRight,
			CellDrawer cellDrawerToRight) {
		setReferencesToCell(AdjacentPosition.CELL_TO_RIGHT, cellToRight,
				cellDrawerToRight);
	}

	public void setReferencesToCellBelow(Cell cellBelow,
			CellDrawer cellDrawerBelow) {
		setReferencesToCell(AdjacentPosition.CELL_BELOW, cellBelow,
				cellDrawerBelow);
	}

	public void setReferencesToCellToLeft(Cell cellToLeft,
			CellDrawer cellDrawerToLeft) {
		setReferencesToCell(AdjacentPosition.CELL_TO_LEFT, cellToLeft,
				cellDrawerToLeft);
	}

	private void setReferencesToCell(AdjacentPosition adjacentPosition,
			Cell cellReference, CellDrawer cellDrawerReference) {
		mAdjacentCells[adjacentPosition.mIndex] = cellReference;
		mAdjacentCellDrawers[adjacentPosition.mIndex] = cellDrawerReference;
		mIsInSameCageAsAdjacentCell[adjacentPosition.mIndex] = (cellReference != null && mCell
				.getCageId() == cellReference.getCageId());
	}

	public void setDigitPositionGrid(DigitPositionGrid digitPositionGrid) {
		mDigitPositionGrid = digitPositionGrid;
	}

	// The next variable could also be declared as local variable in method
	// draw. But is created quite frequently. By reusing it the memory footprint
	// is reduced.
	private final Paint draw_textPaint = new Paint();

	/**
	 * Draw the cell inclusive borders, background and text.
	 */
	public void draw(Canvas canvas, float gridBorderWidth,
			GridInputMode inputMode, int swipeDigit) {
		// Get current cell size
		int cellSize = (int) mCellPainter.getCellSize();

		// Calculate x and y for the cell origin (top left). Use an offset to
		// prevent overlapping of cells and border for entire grid.
		mPosX = Math.round(gridBorderWidth + cellSize * mColumn);
		mPosY = Math.round(gridBorderWidth + cellSize * mRow);
		float top = mPosY;
		float bottom = mPosY + cellSize;
		float left = mPosX;
		float right = mPosX + cellSize;

		if (mCell.isBordersInvalidated()) {
			setBorders();
		}

		// ---------------------------------------------------------------------
		// Draw cage borders first. In case a cell border is part of the cage
		// border it might be necessary to extend the border into an adjacent
		// cell to get a straight corner. Per border it has to be checked if the
		// cell border overlaps with the cage border.
		// IMPORTANT: Transparent cage borders are not correctly supported as
		// overlapping borders will lead to a slightly darker color.
		// ---------------------------------------------------------------------
		Paint borderPaint;

		// Top border of cell (will only be drawn for first row
		float topOffset = 0;
		borderPaint = getBorderPaint(mCommonBorderTypeWithAdjacentCell[AdjacentPosition.CELL_ABOVE.mIndex]);
		if (borderPaint != null) {
			// Calculate offset and draw top border
			float offset = (mRow == 0 ? (float) Math
					.floor((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas
					.drawLine(
							left
									- (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_TO_LEFT.mIndex] ? offset
											: 0),
							top + offset,
							right
									+ (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_TO_RIGHT.mIndex] ? offset
											: 0), top + offset, borderPaint);

			// Calculate offset for inner space after drawing top border
			topOffset = (float) Math
					.floor((float) ((mRow == 0 ? 1 : 0.5) * borderPaint
							.getStrokeWidth()));
		}

		// Right border of cell
		borderPaint = getBorderPaint(mCommonBorderTypeWithAdjacentCell[AdjacentPosition.CELL_TO_RIGHT.mIndex]);
		float rightOffset = 0;
		if (borderPaint != null) {
			// Calculate offset and draw right border
			float offset = (mColumn == mGrid.getGridSize() - 1 ? (float) Math
					.ceil((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas
					.drawLine(
							right - offset,
							top
									- (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_ABOVE.mIndex] ? offset
											: 0),
							right - offset,
							bottom
									+ (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_BELOW.mIndex] ? offset
											: 0), borderPaint);

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
		borderPaint = getBorderPaint(mCommonBorderTypeWithAdjacentCell[AdjacentPosition.CELL_BELOW.mIndex]);
		float bottomOffset = 0;
		if (borderPaint != null) {
			// Calculate offset and draw bottom border
			float offset = (mRow == mGrid.getGridSize() - 1 ? (float) Math
					.ceil((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas
					.drawLine(
							left
									- (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_TO_LEFT.mIndex] ? offset
											: 0),
							bottom - offset,
							right
									+ (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_TO_RIGHT.mIndex] ? offset
											: 0), bottom - offset, borderPaint);

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
		borderPaint = getBorderPaint(mCommonBorderTypeWithAdjacentCell[AdjacentPosition.CELL_TO_LEFT.mIndex]);
		if (borderPaint != null) {
			// Calculate offset and draw left border
			float offset = (mColumn == 0 ? (float) Math
					.floor((float) (0.5 * borderPaint.getStrokeWidth())) : 0);
			canvas
					.drawLine(
							left + offset,
							top
									- (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_ABOVE.mIndex] ? offset
											: 0),
							left + offset,
							bottom
									+ (mIsInSameCageAsAdjacentCell[AdjacentPosition.CELL_BELOW.mIndex] ? offset
											: 0), borderPaint);

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
				borderPaint = ((mCell.isDuplicateValueHighlighted() && mGridViewerView
						.hasPrefShowDupeDigits()) ? mCellPainter
						.getDuplicateBorderPaint() : null);
				break;
			case 2:
				borderPaint = (mCell.isRevealed() ? mCellPainter
						.getRevealedBorderPaint() : null);
				break;
			case 3:
				borderPaint = (mCell.hasInvalidUserValueHighlight() ? mCellPainter
						.getInvalidBorderPaint() : null);
				break;
			case 4:
				borderPaint = (mCell.isSelected() && mGrid != null
						&& mGrid.isActive() ? mCellPainter
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
				canvas
						.drawLine(right - borderOffset, top, right
								- borderOffset, bottom - borderWidth,
								borderPaint);
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
		if (mCell.isSelected() && mGrid != null && mGrid.isActive()) {
			background = mCellPainter.getSelectedBackgroundPaint();
		} else if (mCell.hasInvalidUserValueHighlight()) {
			background = mCellPainter.getInvalidBackgroundPaint();
		} else if (mCell.isRevealed()) {
			background = mCellPainter.getRevealedBackgroundPaint();
		} else if (mCell.isDuplicateValueHighlighted()
				&& mGridViewerView.hasPrefShowDupeDigits()) {
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
		if ((mCell.isUserValueSet() && swipeDigit == 0)
				|| (inputMode == GridInputMode.NORMAL && swipeDigit != 0)) {
			// Get the value which will be shown as user value in case the swipe
			// motion will be released at this moment.
			String userValue = Integer
					.toString(inputMode == GridInputMode.NORMAL
							&& swipeDigit != 0 ? swipeDigit : mCell
							.getUserValue());

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
		String cageText = mCell.getCageText();
		if (!cageText.equals("")) {
			// Clone the text painter and decrease text size until the cage text
			// fits within the cell.
			draw_textPaint.set(mCagePainter.getTextPaint());
			float scaleFactor = (cellSize - 4)
					/ draw_textPaint.measureText(cageText);
			if (scaleFactor < 1) {
				draw_textPaint.setTextSize(draw_textPaint.getTextSize()
						* scaleFactor);
			}

			canvas.drawText(cageText, mPosX + mCagePainter.getTextLeftOffset(),
					mPosY + mCagePainter.getTextBottomOffset(), draw_textPaint);
		}

		// Draw penciled in digits.
		// Note: only for the selected cell the swipe digit can have a value
		// other than 0 while a swipe motion is started but not yet finished. In
		// this case the swipe digit has to be added (if not yet present) to or
		// removed (if already present) from the possible values of this cell.
		// Note that the original possible values may not be shown in case the
		// content of the cell have to replaced with a new user value.
		List<Integer> possibles = mCell.getPossibles();
		if ((possibles.size() > 0 && !(inputMode == GridInputMode.NORMAL && swipeDigit != 0))
				|| (inputMode == GridInputMode.MAYBE && swipeDigit != 0)) {
			// Temporary alter the possible values in case a swipe digit is
			// selected.
			if (swipeDigit != 0) {
				if (possibles.indexOf(Integer.valueOf(swipeDigit)) >= 0) {
					possibles.remove(Integer.valueOf(swipeDigit));
				} else {
					possibles.add(swipeDigit);
					Collections.sort(possibles);
				}
			}

			// Draw the possible values
			if (mGridViewerView.hasPrefShowMaybesAs3x3Grid()
					&& mDigitPositionGrid != null) {
				// Determine which painter to use
				Paint paint = (inputMode == GridInputMode.NORMAL ? mMaybeGridPainter
						.getTextPaintNormalInputMode() : mMaybeGridPainter
						.getTextPaintMaybeInputMode());

				// Draw all possible which are currently set for this cell.
				for (Integer possible : possibles) {
					// Get the possible and the specific position in the digit
					// position grid
					int row = mDigitPositionGrid.getRow(possible);
					int col = mDigitPositionGrid.getCol(possible);

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
				for (Integer possible : possibles) {
					possiblesText += Integer.toString(possible);
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
		}
	}

	// The next variable could also be declared as local variable in method
	// drawSwipeOverlay. But is created quite frequently. By reusing it the
	// memory footprint is reduced.
	private static final Rect drawSwipeOverlay_bounds = new Rect();

	/**
	 * Draw the swipe overlay for the selected cell.
	 */
	public void drawSwipeOverlay(Canvas canvas, float gridBorderWidth,
			GridInputMode inputMode, float mXPosSwipe, float mYPosSwipe,
			int swipeDigit, boolean outerSwipeCircleVisible) {
		assert (inputMode == GridInputMode.NORMAL || inputMode == GridInputMode.MAYBE);
		if (mGrid.getSelectedCell() != mCell) {
			// This cell is not the selected cell.
			return;
		}

		// Get cell size
		int cellSize = (int) mCellPainter.getCellSize();

		// Calculate x and y for the cell origin (top left). Use an offset to
		// prevent overlapping of cells and border for entire grid.
		mPosX = Math.round(gridBorderWidth + cellSize * mColumn);
		mPosY = Math.round(gridBorderWidth + cellSize * mRow);
		float top = mPosY;
		float left = mPosX;

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
				canvas
						.drawLine(
								centerX
										+ (int) (Math
												.cos(Math.toRadians(angle)) * radius),
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

	private Paint getBorderPaint(BorderType borderType) {
		switch (borderType) {
		case NONE:
			return null;
		case CELL_IN_UNSELECTED_CAGE_WITH_GOOD_MATH:
			return mCagePainter.getBorderPaint();
		case CELL_IN_UNSELECTED_CAGE_WITH_BAD_MATH:
			return mCagePainter.getBorderBadMathPaint();
		case CELL_IN_SELECTED_CAGE_WITH_GOOD_MATH:
			// In case the grid is deactivated (for example when an unfinished
			// puzzle is displayed in the archive, display the border as if the
			// cage was not selected
			return (mGrid != null && mGrid.isActive() ? mCagePainter
					.getBorderSelectedPaint() : mCagePainter.getBorderPaint());
		case CELL_IN_SELECTED_CAGE_WITH_BAD_MATH:
			return (mGrid != null && mGrid.isActive() ? mCagePainter
					.getBorderSelectedBadMathPaint() : mCagePainter
					.getBorderBadMathPaint());
		}
		return null;
	}

	// The next variable could also be declared as local variable in method
	// drawDashedLine. But is created quite frequently. By reusing it the
	// memory footprint is reduced.
	private final Path drawDashedLine_path = new Path();

	/**
	 * Draws a dashed line.
	 * <p/>
	 * Due to a bug (https://code.google.com/p/android/issues/detail?id=29944),
	 * a dashed line can not be drawn with drawLine at API-level 11 or above.
	 * 
	 * @param canvas
	 *            The canvas on which will be drawn.
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

	/**
	 * Sets the borders for this cell and cells adjacent to this cell.
	 */
	public void setBorders() {
		for (int i = 0; i < mCommonBorderTypeWithAdjacentCell.length; i++) {
			AdjacentPosition adjacentPosition = AdjacentPosition.values()[i];
			AdjacentPosition oppositeAdjacentPosition = adjacentPosition
					.getOppositeAdjacentPosition();

			mCommonBorderTypeWithAdjacentCell[adjacentPosition.mIndex] = getCommonBorderType(
					mCell, mAdjacentCells[adjacentPosition.mIndex]);
			if (mAdjacentCellDrawers[adjacentPosition.mIndex] != null) {
				mAdjacentCellDrawers[adjacentPosition.mIndex]
						.setCommonBorderTypeWithCell(
								oppositeAdjacentPosition,
								mCommonBorderTypeWithAdjacentCell[adjacentPosition.mIndex]);
			}
		}
	}

	private void setCommonBorderTypeWithCell(AdjacentPosition adjacentPosition,
			BorderType borderType) {
		mCommonBorderTypeWithAdjacentCell[adjacentPosition.mIndex] = borderType;
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
	private BorderType getCommonBorderType(Cell cell1, Cell cell2) {
		if (cell1 == null) {
			throw new InvalidParameterException(
					"Method getMostImportantBorderType can not be called with "
							+ "parameter cell1 equals null.");
		}

		// If both cells are part of the same cage there will be no border.
		if (cell2 != null && cell1.getCageId() == cell2.getCageId()) {
			return BorderType.NONE;
		}

		// If cell1 is part of the selected cage, it status is more important
		// than status of cell 2.
		if (cell1.isCellInSelectedCage()) {
			if (!mGrid.getCage(cell1).isUserMathCorrect()
					&& mGridViewerView.hasPrefShowBadCageMaths()) {
				return BorderType.CELL_IN_SELECTED_CAGE_WITH_BAD_MATH;
			} else {
				return BorderType.CELL_IN_SELECTED_CAGE_WITH_GOOD_MATH;
			}
		}

		// If cell1 is not part of the selected cage, than status of cell2 will
		// prevail in case it is part of the selected cage.
		if (cell2 != null && cell2.isCellInSelectedCage()) {
			if (!mGrid.getCage(cell2).isUserMathCorrect()
					&& mGridViewerView.hasPrefShowBadCageMaths()) {
				return BorderType.CELL_IN_SELECTED_CAGE_WITH_BAD_MATH;
			} else {
				return BorderType.CELL_IN_SELECTED_CAGE_WITH_GOOD_MATH;
			}
		}

		// Both cells are in a cage which is not selected.
		if ((!mGrid.getCage(cell1).isUserMathCorrect() || (cell2 != null && !mGrid
				.getCage(cell2)
				.isUserMathCorrect()))
				&& mGridViewerView.hasPrefShowBadCageMaths()) {
			return BorderType.CELL_IN_UNSELECTED_CAGE_WITH_BAD_MATH;
		} else {
			return BorderType.CELL_IN_UNSELECTED_CAGE_WITH_GOOD_MATH;
		}
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
		int cellSize = (int) mCellPainter.getCellSize();

		float top = Math.round(gridBorderWidth + cellSize * mRow);
		// float bottom = mPosY + cellSize;
		float left = Math.round(gridBorderWidth + cellSize * mColumn);
		// float right = mPosX + cellSize;

		return new float[] { left + (cellSize / 2), top + (cellSize / 2) };
	}
}
