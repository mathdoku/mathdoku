package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;

public class TickerTapePainter extends BasePainter {

	private int mTextColor;
	private int mBackgroundColor;

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