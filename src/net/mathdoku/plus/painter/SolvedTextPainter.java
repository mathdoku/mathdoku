package net.mathdoku.plus.painter;

import net.mathdoku.plus.painter.Painter.GridTheme;

public class SolvedTextPainter extends BasePainter {

	private final int mTextColor;
	private final int mBackgroundColor;

	public SolvedTextPainter(Painter painter) {
		super(painter);

		mTextColor = 0xFF002F00;
		mBackgroundColor = 0xD0DECA1E;
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
	 * Get the text color for the pager tab strip.
	 * 
	 * @return The text color the pager tab strip.
	 */
	public int getTextColor() {
		return mTextColor;
	}

	/**
	 * Get the background color for the pager tab strip.
	 * 
	 * @return The background color the pager tab strip.
	 */
	public int getBackgroundColor() {
		return mBackgroundColor;
	}
}