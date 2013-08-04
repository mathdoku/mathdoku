package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.DigitPositionGrid;
import net.cactii.mathdoku.painter.Painter.DigitPainterMode;
import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;

public class MaybeValuePainter extends DigitPainter {

	// Size of grid of maybe values within the cell
	private DigitPositionGrid mDigitPositionGrid;

	// Size of regions to draw maybe digit
	protected float mMaybeDigitWidth;
	protected float mMaybeDigitHeight;

	/**
	 * Creates a new instance of {@link GridPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public MaybeValuePainter(Painter painter) {
		super(painter);

		mTextPaintNormalInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaintNormalInputMode.setTextSize(10);
		mTextPaintNormalInputMode.setTypeface(mPainter.getTypeface());
		mTextPaintNormalInputMode.setFakeBoldText(true);

		mTextPaintMaybeInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaintMaybeInputMode.setTextSize(10);
		mTextPaintMaybeInputMode.setTypeface(mPainter.getTypeface());
		mTextPaintMaybeInputMode.setFakeBoldText(true);
	}

	@Override
	public void setTheme(GridTheme theme) {
		mTextPaintNormalInputMode.setColor(mPainter.getDefaultTextColor());
		mTextPaintMaybeInputMode.setColor(mPainter
				.getHighlightedTextColorMaybeInputMode());
	}

	/**
	 * Do not use this method for this object. Use
	 * {@link #setCellSize(float, DigitPositionGrid)} instead.
	 */
	@Override
	protected void setCellSize(float size) {
		throw new RuntimeException("Method should not be called for object "
				+ this.getClass().getSimpleName()
				+ ". Call setCellSize(float, DigitPositionGrid) instead,");
	}

	protected void setCellSize(float size, DigitPositionGrid digitPositionGrid) {
		mDigitPositionGrid = digitPositionGrid;

		if (mDigitPositionGrid == null) {
			// All maybe values are printed on a single line

			// Text size is 25% of cell size
			int textSize = (int) (size / 4);
			mTextPaintNormalInputMode.setTextSize(textSize);
			mTextPaintMaybeInputMode.setTextSize(textSize);

			// Align digits at left of cell and vertically centered. Note that
			// vertical offset of text will be used as bottom edge for text.
			mLeftOffset = 3;
			mBottomOffset = (size + textSize) / 2;
		} else {
			// Determine number of rows and columns of maybe values to display.
			int rows = mDigitPositionGrid.getVisibleDigitRows();
			int cols = mDigitPositionGrid.getVisibleDigitColumns();

			// 1/3 of total height is used for cage result text. Also need to
			// reserve a margin below the maybes grid for the selected cage
			// border.
			int margin = 4;
			float maxHeightForMaybes = (size * 2 / 3) - margin;

			mMaybeDigitHeight = maxHeightForMaybes / rows;
			mMaybeDigitWidth = Math.min((size - 2 * margin) / cols,
					mMaybeDigitHeight);

			// Maximize the textsize within the available space for each row
			mTextPaintNormalInputMode.setTextSize(mMaybeDigitHeight);
			mTextPaintMaybeInputMode.setTextSize(mMaybeDigitHeight);

			// Compute the offsets at which the top left digit (1) will be
			// displayed within the cell if it is entered as a possible value.
			mLeftOffset = (size - margin) - (cols * mMaybeDigitWidth);
			mBottomOffset = (size - margin) - ((rows - 1) * mMaybeDigitHeight);
		}
	}

	/**
	 * Gets the width of a single position to display a maybe value.
	 * 
	 * @return The width of a single position to display a maybe value.
	 */
	public float getMaybeDigitWidth() {
		return mMaybeDigitWidth;
	}

	/**
	 * Gets the height of a single position to display a maybe value.
	 * 
	 * @return The height of a single position to display a maybe value.
	 */
	public float getMaybeDigitHeight() {
		return mMaybeDigitHeight;
	}

	/**
	 * Gets the digit position grid which has to be used to display maybe values
	 * inside a cell in case the maybes have to displayed as a grid. the cell.
	 * 
	 * @return The digit position grid object used to display maybe values in a
	 *         grid format inside a cell.
	 */
	public DigitPositionGrid getDigitPositionGrid() {
		return mDigitPositionGrid;
	}
	
	@Override
	public Paint getTextPaintMaybeInputMode() {
		return (mDigitPainterMode == DigitPainterMode.INPUT_MODE_BASED ? mTextPaintMaybeInputMode : mTextPaintNormalInputMode);
	}
}