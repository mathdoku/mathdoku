package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class CellPainter extends BorderPainter {

	// Horizontal and vertical size of the cell
	private float mCellSize;

	// Borders between two cell which belong to same cage.
	private final Paint mUnusedBorderPaint;

	// Border and background for a cell containing an invalid value.
	private final Paint mInvalidBorderPaint;
	private final Paint mInvalidBackgroundPaint;

	// Border and background for a cell from which the value is revealed.
	private final Paint mRevealedBorderPaint;
	private final Paint mRevealedBackgroundPaint;

	// Border and background for a cell containing a duplicate value
	private final Paint mDuplicateBorderPaint;
	private final Paint mDuplicateBackgroundPaint;

	// Border and background for the selected cell
	private final Paint mSelectedBorderPaint;
	private final Paint mSelectedBackgroundPaint;

	/**
	 * Creates a new instance of {@link CellPainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public CellPainter(Painter painter) {
		super(painter);

		// Border between two cell which belong to same cage.
		mUnusedBorderPaint = new Paint();
		mUnusedBorderPaint.setStyle(Style.STROKE);
		mUnusedBorderPaint.setAntiAlias(true);
		mUnusedBorderPaint.setPathEffect(new DashPathEffect(
				new float[] { 2, 2 }, 0));

		// Border and background for cells with an invalid user value. The
		// background color depends on the theme.
		mInvalidBorderPaint = new Paint();
		mInvalidBorderPaint.setAntiAlias(true);
		mInvalidBorderPaint.setColor(0xFFF8A86B);
		mInvalidBackgroundPaint = new Paint();
		mInvalidBackgroundPaint.setColor(mInvalidBorderPaint.getColor());

		mInvalidBackgroundPaint.setStyle(Paint.Style.FILL);

		// Border and background for cells having a duplicate value
		mDuplicateBorderPaint = new Paint();
		mDuplicateBackgroundPaint = new Paint();
		mDuplicateBorderPaint.setColor(0xFFFFA091);
		mDuplicateBackgroundPaint.setStyle(Paint.Style.FILL);
		mDuplicateBackgroundPaint.setColor(mDuplicateBorderPaint.getColor());

		// Border and background for cells on which is revealed
		mRevealedBorderPaint = new Paint();
		mRevealedBackgroundPaint = new Paint();
		mRevealedBorderPaint.setColor(0x90ffcea0);
		mRevealedBackgroundPaint.setStyle(Paint.Style.FILL);
		mRevealedBackgroundPaint.setColor(mRevealedBorderPaint.getColor());

		// Border and background for cells which are selected.
		mSelectedBorderPaint = new Paint();
		mSelectedBackgroundPaint = new Paint();
		mSelectedBackgroundPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	public void setTheme(GridTheme theme) {
		// Set the unused border paint
		switch (theme) {
		case LIGHT:
			mUnusedBorderPaint.setColor(0x80000000);
			break;
		case DARK:
			mUnusedBorderPaint.setColor(0xff7f7f7f);
			break;
		}

		// Set the selected cell painter
		switch (theme) {
		case LIGHT:
			mSelectedBorderPaint.setColor(0xFFE6E6E6);
			mSelectedBackgroundPaint.setColor(mSelectedBorderPaint.getColor());
			break;
		case DARK:
			mSelectedBorderPaint.setColor(0xFF545353);
			mSelectedBackgroundPaint.setColor(mSelectedBorderPaint.getColor());
			break;
		}
	}

	@Override
	protected void setBorderSizes(boolean thin) {
		if (thin) {
			mUnusedBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_HAIR_LINE);
			mInvalidBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
			mDuplicateBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
			mRevealedBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
			mSelectedBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_NORMAL);
		} else {
			mUnusedBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_HAIR_LINE);
			mInvalidBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
			mDuplicateBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
			mRevealedBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
			mSelectedBorderPaint
					.setStrokeWidth(BorderPainter.BORDER_STROKE_WIDTH_THICK);
		}
	}

	@Override
	protected void setCellSize(float size) {
		mCellSize = size;
	}

	/**
	 * Get the size of the cell.
	 * 
	 * @return The size of the cell.
	 */
	public float getCellSize() {
		return mCellSize;
	}

	/**
	 * Get the paint for borders between two cells belonging to the same cage.
	 * 
	 * @return The paint for borders between two cells belonging to the same
	 *         cage.
	 */
	public Paint getUnusedBorderPaint() {
		return mUnusedBorderPaint;
	}

	/**
	 * Get the paint for a border of a cell containing an invalid value.
	 * 
	 * @return The paint for a border of a cell containing an invalid value.
	 */
	public Paint getInvalidBorderPaint() {
		return mInvalidBorderPaint;
	}

	/**
	 * Get the paint for the background of a cell containing an invalid value.
	 * 
	 * @return The paint for the background of a cell containing an invalid
	 *         value.
	 */
	public Paint getInvalidBackgroundPaint() {
		return mInvalidBackgroundPaint;
	}

	/**
	 * Get the paint for a border of a cell which has been revealed.
	 * 
	 * @return The paint for a border of a cell which has been revealed.
	 */
	public Paint getRevealedBorderPaint() {
		return mRevealedBorderPaint;
	}

	/**
	 * Get the paint for the background of a cell which has been revealed.
	 * 
	 * @return The paint for the background of a cell which has been revealed.
	 */
	public Paint getRevealedBackgroundPaint() {
		return mRevealedBackgroundPaint;
	}

	/**
	 * Get the paint for a border of a cell containing a duplicate value.
	 * 
	 * @return The paint for a border of a cell containing a duplicate value.
	 */
	public Paint getDuplicateBorderPaint() {
		return mDuplicateBorderPaint;
	}

	/**
	 * Get the paint for the background of a cell containing a duplicate value.
	 * 
	 * @return The paint for the background of a cell containing a duplicate
	 *         value.
	 */
	public Paint getWarningBackgroundPaint() {
		return mDuplicateBackgroundPaint;
	}

	/**
	 * Get the paint for a border of the selected cell.
	 * 
	 * @return The paint for a border of the selected cell.
	 */
	public Paint getSelectedBorderPaint() {
		return mSelectedBorderPaint;
	}

	/**
	 * Get the paint for the background of the selected cell.
	 * 
	 * @return The paint for the background of the selected cell.
	 */
	public Paint getSelectedBackgroundPaint() {
		return mSelectedBackgroundPaint;
	}
}