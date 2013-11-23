package net.mathdoku.plus.painter;

import net.mathdoku.plus.painter.Painter.GridTheme;

public class PagerTabStripPainter extends BasePainter {

	private final int mTextColor;
	private final int mBackgroundColor;
	private final int mTabIndicatorColor;

	public PagerTabStripPainter(Painter painter) {
		super(painter);

		mTextColor = 0xFFFFFFFF;
		mTabIndicatorColor = mTextColor;
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

	/**
	 * Get the background color for the pager tab strip.
	 * 
	 * @return The background color the pager tab strip.
	 */
	public int getTabIndicatorColor() {
		return mTabIndicatorColor;
	}
}