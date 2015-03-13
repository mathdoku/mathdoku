package net.mathdoku.plus.painter;

public class NavigationDrawerPainter extends BasePainter {

    private final int mBackgroundColor;

    public NavigationDrawerPainter(Painter painter) {
        super(painter);

        mBackgroundColor = 0xFF222222;
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