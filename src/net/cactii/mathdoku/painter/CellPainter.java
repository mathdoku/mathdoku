package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class CellPainter extends BorderPainter {

	// Horizontal and vertical size of the cell
	private float mCellSize;

	// Borders between two cell which belong to same cage.
	private Paint mUnusedBorderPaint;

	// Border and background for a cell containing an invalid value.
	private Paint mInvalidBorderPaint;
	private Paint mInvalidBackgroundPaint;

	// Border and background for a cell from which the value is revealed.
	private Paint mCheatedBorderPaint;
	private Paint mCheatedBackgroundPaint;

	// Border and background for a cell containing a duplicate value
	private Paint mDuplicateBorderPaint;
	private Paint mDuplicateBackgroundPaint;

	// Border and background for the selected cell
	private Paint mSelectedBorderPaint;
	private Paint mSelectedBackgroundPaint;

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

		// Border and background for cells with an invalid user value. The
		// background color depends on the theme.
		mInvalidBorderPaint = new Paint();
		mInvalidBorderPaint.setAntiAlias(true);
		mInvalidBackgroundPaint = new Paint();

		// Border and background for cells having a duplicate value
		mDuplicateBorderPaint = new Paint();
		mDuplicateBackgroundPaint = new Paint();

		// Border and background for cells on which is cheated
		mCheatedBorderPaint = new Paint();
		mCheatedBackgroundPaint = new Paint();

		// Border and background for cells which are selected.
		mSelectedBorderPaint = new Paint();
		mSelectedBackgroundPaint = new Paint();
	}

	@Override
	public void setTheme(GridTheme theme) {
		// Set the unused border paint
		switch (theme) {
		case NEWSPAPER:
			mUnusedBorderPaint.setColor(0x80000000);
			break;
		case DARK:
			mUnusedBorderPaint.setColor(0xff7f7f7f);
			break;
		}

		// Set the invalid border paint
		switch (theme) {
		case NEWSPAPER:
			mInvalidBorderPaint.setColor(0xFFF8A86B);
			mInvalidBackgroundPaint.setColor(mInvalidBorderPaint.getColor());
			mInvalidBackgroundPaint.setStyle(Paint.Style.FILL);
			break;
		case DARK:
			mInvalidBorderPaint.setColor(0xFFBB0000);
			// mInvalidBackgroundPaint.setColor(mInvalidBorderPaint.getColor());
			// TODO: check if needed.
			mInvalidBackgroundPaint.setStyle(Paint.Style.STROKE);
			break;
		}

		// Set the duplicate value painter
		switch (theme) {
		case NEWSPAPER:
			mDuplicateBorderPaint.setColor(0xFFFFA091);
			mDuplicateBackgroundPaint.setColor(mDuplicateBorderPaint.getColor());
			mDuplicateBackgroundPaint.setStyle(Paint.Style.FILL);
			break;
		case DARK:
			mDuplicateBorderPaint.setColor(0x50FF0000);
			mDuplicateBackgroundPaint.setColor(mDuplicateBorderPaint.getColor());
			mDuplicateBackgroundPaint.setStyle(Paint.Style.STROKE);
			break;
		}

		// Set the cheated painter
		switch (theme) {
		case NEWSPAPER:
			mCheatedBorderPaint.setColor(0x90ffcea0);
			mCheatedBackgroundPaint.setColor(mCheatedBorderPaint.getColor());
			mCheatedBackgroundPaint.setStyle(Paint.Style.FILL);
			break;
		case DARK:
			mCheatedBorderPaint.setColor(0x90ffcea0);
			mCheatedBackgroundPaint.setColor(mCheatedBorderPaint.getColor());
			mCheatedBackgroundPaint.setStyle(Paint.Style.STROKE);
			break;
		}

		// Set the selected cell painter
		switch (theme) {
		case NEWSPAPER:
			mSelectedBorderPaint.setColor(0xFFE6E6E6);
			mSelectedBackgroundPaint.setColor(mSelectedBorderPaint.getColor());
			mSelectedBackgroundPaint.setStyle(Paint.Style.FILL);
			break;
		case DARK:
			mSelectedBorderPaint.setColor(0xD0F0D042);
			mSelectedBackgroundPaint.setColor(mSelectedBorderPaint.getColor());
			mSelectedBackgroundPaint.setStyle(Paint.Style.STROKE);
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
			mCheatedBorderPaint
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
			mCheatedBorderPaint
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
	public Paint getCheatedBorderPaint() {
		return mCheatedBorderPaint;
	}

	/**
	 * Get the paint for the background of a cell which has been revealed.
	 * 
	 * @return The paint for the background of a cell which has been revealed.
	 */
	public Paint getCheatedBackgroundPaint() {
		return mCheatedBackgroundPaint;
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