package net.mathdoku.plus.painter;

import net.mathdoku.plus.puzzle.ui.theme.LightTheme;
import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class Painter {
    // Singleton reference to the GridPainter object.
    private static Painter mPainterSingletonInstance = null;

    // Text colors (dependent on theme) per input mode
    private int mHighlightedTextColorNormalInputMode;
    private int mHighlightedTextColorMaybeInputMode;

    public enum DigitPainterMode {
        INPUT_MODE_BASED,
        MONOCHROME
    }

    // Theme installed in painter
    private Theme theme;

    // Reference to all sub painters
    private final GridPainter mGridPainter;
    private final CagePainter mCagePainter;
    private final CellPainter mCellPainter;
    private final EnteredValuePainter mEnteredValuePainter;
    private final MaybeValuePainter mMaybeGridPainter;
    private final MaybeValuePainter mMaybeLinePainter;
    private final InputModeBorderPainter mInputModeBorderPainter;
    private final NavigationDrawerPainter mNavigationDrawerPainter;

    /**
     * Creates a new instance of {@link net.mathdoku.plus.painter.Painter}.
     * <p/>
     * This object can not be instantiated directly. Use {@link #getInstance()} to get the singleton reference to the
     * GridPainter object.
     */
    private Painter() {
        // Create the painters
        mGridPainter = new GridPainter();
        mCagePainter = new CagePainter();
        mCellPainter = new CellPainter();
        mEnteredValuePainter = new EnteredValuePainter();
        mMaybeGridPainter = new MaybeValuePainter();
        mMaybeLinePainter = new MaybeValuePainter();
        mInputModeBorderPainter = new InputModeBorderPainter();
        mNavigationDrawerPainter = new NavigationDrawerPainter();

        // Set the size of the borders.
        setBorderSizes(false);

        setTheme(LightTheme.getInstance());
    }

    /**
     * Gets the singleton reference to the GridPainter object. If it does not yet exist then it will be created.
     *
     * @return The singleton reference to the GridPainter object.
     */
    public static Painter getInstance() {
        if (mPainterSingletonInstance == null) {
            // Only the first time this method is called, the object will be
            // created.
            mPainterSingletonInstance = new Painter();
        }
        return mPainterSingletonInstance;
    }

    /**
     * Change the width of the border of the grid.
     *
     * @param thin
     *         True in case a small border needs to be set. False in case a normal border should be used.
     */
    private void setBorderSizes(boolean thin) {
        mGridPainter.setBorderSizes(thin);
        mCagePainter.setBorderSizes(thin);
        mCellPainter.setBorderSizes(thin);
    }

    /**
     * Apply settings for the given gridTheme on the painter objects.
     *
     * @param theme
     *         The theme to be set.
     */
    public void setTheme(Theme theme) {
        if (this.theme == theme) {
            // Theme has not changed.
            return;
        }

        setInputModeColors(theme);

        mGridPainter.setTheme(theme);
        mCagePainter.setTheme(theme);
        mCellPainter.setTheme(theme);
        mEnteredValuePainter.setTheme(theme);
        mMaybeGridPainter.setTheme(theme);
        mMaybeLinePainter.setTheme(theme);
        mInputModeBorderPainter.setTheme(theme);
        mNavigationDrawerPainter.setTheme(theme);
    }

    /**
     * Changes the painter objects to handle cells of a the given size.
     *
     * @param size
     *         The size of cells.
     */
    public void setCellSize(float size) {
        // Set width of borders dependent on new size of cells.
        setBorderSizes(size <= 80);

        // Propagate the new cell size to all border-painters
        mCagePainter.setCellSize(size);
        mCellPainter.setCellSize(size);
        mEnteredValuePainter.setCellSize(size);
        mMaybeGridPainter.setCellSize(size);
        mMaybeLinePainter.setCellSize(size);
        mInputModeBorderPainter.setCellSize(size);
    }

    /**
     * Set the text colors to be used in all painters.
     *
     * @param theme
     *         The theme for which the path effect has to be set.
     */
    private void setInputModeColors(Theme theme) {
        mHighlightedTextColorNormalInputMode = theme.getHighlightedTextColorNormalInputMode();
        mHighlightedTextColorMaybeInputMode = theme.getHighlightedTextColorMaybeInputMode();
    }

    public int getHighlightedTextColorNormalInputMode() {
        return mHighlightedTextColorNormalInputMode;
    }

    public int getHighlightedTextColorMaybeInputMode() {
        return mHighlightedTextColorMaybeInputMode;
    }

    public GridPainter getGridPainter() {
        return mGridPainter;
    }

    public CagePainter getCagePainter() {
        return mCagePainter;
    }

    public CellPainter getCellPainter() {
        return mCellPainter;
    }

    public EnteredValuePainter getEnteredValuePainter() {
        return mEnteredValuePainter;
    }

    public MaybeValuePainter getMaybeGridPainter() {
        return mMaybeGridPainter;
    }

    public MaybeValuePainter getMaybeLinePainter() {
        return mMaybeLinePainter;
    }

    public InputModeBorderPainter getInputModeBorderPainter() {
        return mInputModeBorderPainter;
    }

    public NavigationDrawerPainter getNavigationDrawerPainter() {
        return mNavigationDrawerPainter;
    }

    /**
     * Set the color mode of the digit painters.
     *
     * @param digitPainterMode
     *         The digit painter mode (colored digits or monochrome) which has to be used.
     */
    public void setColorMode(DigitPainterMode digitPainterMode) {
        mMaybeGridPainter.setColorMode(digitPainterMode);
        mMaybeLinePainter.setColorMode(digitPainterMode);
        mEnteredValuePainter.setColorMode(digitPainterMode);
    }
}