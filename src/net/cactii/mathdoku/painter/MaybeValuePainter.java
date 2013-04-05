package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;

public class MaybeValuePainter extends DigitPainter {

	public enum MaybeGridType {
		GRID_3X3, GRID_2X5, GRID_1X9
	}

	// Size of grid of maybe values within the cell
	private MaybeGridType mMaybeGridType;

	// Size of maybe digits (not used for 1x9 painter)
	protected float mMaybeDigitWidth;
	protected float mMaybeDigitHeight;

	/**
	 * This method should not be used. Use
	 * {@link #MaybeValuePainter(Painter, MaybeGridType)} instead.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	private MaybeValuePainter(Painter painter) {
		super(painter);
	}

	/**
	 * Creates a new instance of {@link GridPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 * @param maybeGridType
	 *            The type of gird used to display the maybes.
	 */
	public MaybeValuePainter(Painter painter, MaybeGridType maybeGridType) {
		super(painter);
		mMaybeGridType = maybeGridType;

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

	@Override
	protected void setCellSize(float size) {
		switch (mMaybeGridType) {
		case GRID_1X9:
			// Text size is 25% of cell size
			int maybe1x9TextSize = (int) (size / 4);

			mTextPaintNormalInputMode.setTextSize(maybe1x9TextSize);
			mTextPaintMaybeInputMode.setTextSize(maybe1x9TextSize);

			// Align digits at left of cell and vertically centered. Note that
			// vertical offset of text will be used as bottom edge for text.
			mLeftOffset = 3;
			mTopOffset = (size + maybe1x9TextSize) / 2;
			break;
		case GRID_2X5:
			// TODO: Offsets all grids except 1x9 can be computed based on
			// number of rows and cols..

			// Text size is 25% of cell size
			int maybe2x5TextSize = (int) (size / 4);

			mTextPaintNormalInputMode.setTextSize(maybe2x5TextSize);
			mTextPaintMaybeInputMode.setTextSize(maybe2x5TextSize);

			// Compute the offsets at which the 2x5 grid of possible values will
			// be displayed within the cell. The grid of the maybe digits is
			// aligned to the bottom right corner of the cell.
			// Align digits at left of cell and vertically centered. Note that
			// vertical offset of text will be used as bottom edge for text.
			mLeftOffset = 3;
			mTopOffset = (int) (size / 1.7);

			// Compute height and width of region in which the digit will be
			// placed.
			mMaybeDigitHeight = (float) 0.25 * size;
			mMaybeDigitWidth = (float) 0.16 * size;
			break;
		case GRID_3X3:
			// TODO: Offsets all grids except 1x9 can be computed based on
			// number of rows and cols..

			// Text size is 25% of cell size
			int maybe3x3TextSize = (int) (size / 4.5);

			mTextPaintNormalInputMode.setTextSize(maybe3x3TextSize);
			mTextPaintMaybeInputMode.setTextSize(maybe3x3TextSize);

            mLeftOffset = (int) (size / 3);
			mTopOffset = (int) (size / 2) + 1;

			// Compute height and width of region in which the digit will be
			// placed.
			mMaybeDigitHeight = (float) 0.21 * size;
			mMaybeDigitWidth = (float) 0.21 * size;		}
	}

	public float getMaybeDigitWidth() {
		return mMaybeDigitWidth;
	}

	public float getMaybeDigitHeight() {
		return mMaybeDigitHeight;
	}
}