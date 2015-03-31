package net.mathdoku.plus.painter;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class NavigationDrawerPainter extends BasePainter {
    private int mBackgroundColor;

    @Override
    public void setTheme(Theme theme) {
        mBackgroundColor = theme.getNavigationDrawerBackgroundColor();
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