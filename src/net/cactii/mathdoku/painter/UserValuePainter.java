package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;
import android.graphics.Paint;

public class UserValuePainter extends DigitPainter {

	/**
	 * Creates a new instance of {@link UserValuePainter}.
	 * 
	 * @param painter
	 *            The global container for all painters.
	 */
	public UserValuePainter(Painter painter) {
		super(painter);
		mTextPaintNormalInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaintMaybeInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	public void setTheme(GridTheme theme) {
		mTextPaintNormalInputMode.setColor(mPainter
				.getHighlightedTextColorNormalInputMode());
		mTextPaintMaybeInputMode.setColor(mPainter.getDefaultTextColor());

		mTextPaintNormalInputMode.setTypeface(mPainter.getTypeface());

		mTextPaintMaybeInputMode.setTypeface(mPainter.getTypeface());

	}

	@Override
	protected void setCellSize(float size) {
		// Text size is 75% of cell size
		int userValueTextSize = (int) (size * 3 / 4);

		mTextPaintNormalInputMode.setTextSize(userValueTextSize);
		mTextPaintMaybeInputMode.setTextSize(userValueTextSize);

		// Compute the offsets at which the user value will be displayed within
		// the cell
		mLeftOffset = size / 2 - userValueTextSize / 4;

		// TODO: Why a different approach for top offset based on theme?? This
		// is not logical.
		// if (mTheme == GridTheme.NEWSPAPER) {
		mTopOffset = size / 2 + userValueTextSize * 2 / 5;
		// } else {
		// mTopOffset = size / 2
		// + userValueTextSize / 3;
		// }
	}

}
