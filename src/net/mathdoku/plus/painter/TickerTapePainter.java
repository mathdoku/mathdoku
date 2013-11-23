package net.mathdoku.plus.painter;

import net.mathdoku.plus.painter.Painter.GridTheme;

public class TickerTapePainter extends BasePainter {

	private final int mTextColor;
	private final int mBackgroundColor;

	public TickerTapePainter(Painter painter) {
		super(painter);

		mTextColor = 0xFFFFFFFF;
		mBackgroundColor = mPainter.getButtonBackgroundColor();
	}

	@Override
	public void setTheme(GridTheme theme) {
		// Not needed for this painter.
	}

	@Override
	protected void setCellSize(float size) {
		// Not needed for this painter.
	}

	/**
	 * Get the text color for the ticker tape.
	 * 
	 * @return The text color the ticker tape.
	 */
	public int getTextColor() {
		return mTextColor;
	}

	/**
	 * Get the background color for the ticker tape.
	 * 
	 * @return The background color the ticker tape.
	 */
	public int getBackgroundColor() {
		return mBackgroundColor;
	}
}