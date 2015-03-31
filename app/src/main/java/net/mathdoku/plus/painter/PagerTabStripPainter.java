package net.mathdoku.plus.painter;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class PagerTabStripPainter extends BasePainter {
    private int mTextColor;
    private int mBackgroundColor;

    @Override
    public void setTheme(Theme theme) {
        mTextColor = theme.getActionBarTextColor();
        mBackgroundColor = theme.getActionBarBackgroundColor();
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