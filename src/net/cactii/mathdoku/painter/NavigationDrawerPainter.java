package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.Painter.GridTheme;

public class NavigationDrawerPainter extends BasePainter {

	private int mBackgroundColor;

	public NavigationDrawerPainter(Painter painter) {
		super(painter);

		mBackgroundColor = 0xFF222222;
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
	 * Get the background color for the inactive item.
	 * 
	 * @return The background color the inactive item.
	 */
	public int getBackgroundColor() {
		return mBackgroundColor;
	}
}