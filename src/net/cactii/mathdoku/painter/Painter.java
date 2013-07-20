package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.DigitPositionGrid;
import android.graphics.DashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Typeface;

public class Painter {
	// Singleton reference to the GridPainter object.
	private static Painter mPainterSingletonInstance = null;

	// Typeface to be used
	private Typeface mTypefaceTheme;

	// Path effect to be used (theme specific)
	private PathEffect mPathEffectTheme;

	// Text colors (dependent on theme) per input mode
	private int mHighlightedTextColorNormalInputMode;
	private int mHighlightedTextColorMaybeInputMode;
	private int mDefaultTextColor;

	// Themes available
	public enum GridTheme {
		LIGHT, DARK
	};

	// Theme installed in painter
	private GridTheme mTheme;

	// Reference to all sub painters
	private GridPainter mGridPainter;
	private CagePainter mCagePainter;
	private CellPainter mCellPainter;
	private UserValuePainter mUserValuePainter;
	private MaybeValuePainter mMaybeGridPainter;
	private MaybeValuePainter mMaybeLinePainter;
	private SwipeBorderPainter mSwipeBorderPainter;

	// Background color of buttons.
	private int mButtonBackgroundColor;

	/**
	 * Creates a new instance of {@link #GridPainter()}.
	 * 
	 * This object can not be instantiated directly. Use {@link #getInstance()}
	 * to get the singleton reference to the GridPainter object.
	 * 
	 */
	private Painter() {
		// Create the painters
		mGridPainter = new GridPainter(this);
		mCagePainter = new CagePainter(this);
		mCellPainter = new CellPainter(this);
		mUserValuePainter = new UserValuePainter(this);
		mMaybeGridPainter = new MaybeValuePainter(this);
		mMaybeLinePainter = new MaybeValuePainter(this);
		mSwipeBorderPainter = new SwipeBorderPainter(this);

		// Set the typeface
		mTypefaceTheme = Typeface.create(Typeface.SANS_SERIF,
				Typeface.NORMAL);
		
		// Set the path effect
		mPathEffectTheme = new DashPathEffect(new float[] { 2, 2 }, 0);
		
		mButtonBackgroundColor = 0xFF33B5E5;

		// Set the size of the borders.
		setBorderSizes(false);

		// Apply default theme to the painters.
		setTheme(GridTheme.LIGHT);
	}

	/**
	 * Gets the singleton reference to the GridPainter object. If it does not
	 * yet exist then it will be created.
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
	 *            True in case a small border needs to be set. False in case a
	 *            normal border should be used.
	 */
	private void setBorderSizes(boolean thin) {
		mGridPainter.setBorderSizes(thin);
		mCagePainter.setBorderSizes(thin);
		mCellPainter.setBorderSizes(thin);
		mSwipeBorderPainter.setBorderSizes(thin);
	}

	/**
	 * Apply settings for the given theme on the painter objects.
	 * 
	 * @param theme
	 *            The theme to be set.
	 */
	public void setTheme(GridTheme theme) {
		if (theme == mTheme) {
			// Theme has not changed.
			return;
		}
		mTheme = theme;

		// Change default typeface and path effects
		setTextColors(theme);

		// Propagate theme settings to children
		mGridPainter.setTheme(theme);
		mCagePainter.setTheme(theme);
		mCellPainter.setTheme(theme);
		mUserValuePainter.setTheme(theme);
		mMaybeGridPainter.setTheme(theme);
		mMaybeLinePainter.setTheme(theme);
		mSwipeBorderPainter.setTheme(theme);
	}

	/**
	 * Changes the painter objects to handle cells of a the given size.
	 * 
	 * @param size
	 *            The size of cells.
	 * @param digitPositionGrid
	 *            The digit position grid used to display maybe values into a
	 *            grid.
	 */
	public void setCellSize(float size, DigitPositionGrid digitPositionGrid) {
		// Set width of borders dependent on new size of cells.
		setBorderSizes(size <= 80);

		// Propagate the new cell size to all border-painters
		mCagePainter.setCellSize(size);
		mCellPainter.setCellSize(size);
		mUserValuePainter.setCellSize(size);
		mMaybeGridPainter.setCellSize(size, digitPositionGrid);
		mMaybeLinePainter.setCellSize(size, null);
		mSwipeBorderPainter.setCellSize(size);
	}

	/**
	 * Get the typeface to be used for all painters.
	 * 
	 * @return The typeface to be used for all painters.
	 */
	public Typeface getTypeface() {
		return mTypefaceTheme;
	}

	/**
	 * Get the path effect to be used for all painters.
	 * 
	 * @return The path effect to be used for all painters.
	 */
	public PathEffect getPathEffect() {
		return mPathEffectTheme;
	}

	/**
	 * Set the text colors to be used in all painters.
	 * 
	 * @param gridTheme
	 *            The theme for which the path effect has to be set.
	 */
	private void setTextColors(GridTheme gridTheme) {
		switch (gridTheme) {
		case LIGHT:
			mHighlightedTextColorNormalInputMode = 0xFF0C97FA;
			mHighlightedTextColorMaybeInputMode = 0xFFFF8A00;
			mDefaultTextColor = 0xFF212121;
			break;
		case DARK:
			mHighlightedTextColorNormalInputMode = 0xFF9080ED;
			mHighlightedTextColorMaybeInputMode = 0xFFE61EBE;
			mDefaultTextColor = 0xFFFFFFFF;
			break;
		}
	}

	/**
	 * Get the color for highlighted text in normal input mode.
	 * 
	 * @return The color for highlighted text in normal input mode.
	 */
	public int getHighlightedTextColorNormalInputMode() {
		return mHighlightedTextColorNormalInputMode;
	}

	/**
	 * Get the color for highlighted text in maybe input mode.
	 * 
	 * @return The color for highlighted text in maybe input mode.
	 */
	public int getHighlightedTextColorMaybeInputMode() {
		return mHighlightedTextColorMaybeInputMode;
	}

	/**
	 * Get the color for default text.
	 * 
	 * @return The color for default text.
	 */
	protected int getDefaultTextColor() {
		return mDefaultTextColor;
	}

	/**
	 * Get the grid painter.
	 * 
	 * @return The grid painter.
	 */
	public GridPainter getGridPainter() {
		return mGridPainter;
	}

	/**
	 * Get the cage painter.
	 * 
	 * @return The cagepainter.
	 */
	public CagePainter getCagePainter() {
		return mCagePainter;
	}

	/**
	 * Get the cell painter.
	 * 
	 * @return The cell painter.
	 */
	public CellPainter getCellPainter() {
		return mCellPainter;
	}

	/**
	 * Get the user value painter.
	 * 
	 * @return The user value painter.
	 */
	public UserValuePainter getUserValuePainter() {
		return mUserValuePainter;
	}

	/**
	 * Get the maybe grid painter.
	 * 
	 * @return The maybe grid painter.
	 */
	public MaybeValuePainter getMaybeGridPainter() {
		return mMaybeGridPainter;
	}

	/**
	 * Get the maybe line painter.
	 * 
	 * @return The maybe line painter.
	 */
	public MaybeValuePainter getMaybeLinePainter() {
		return mMaybeLinePainter;
	}
	
	/**
	 * Get the swipe border painter.
	 * 
	 * @return The swipe border painter.
	 */
	public SwipeBorderPainter getSwipeBorderPainter() {
		return mSwipeBorderPainter;
	}

	/**
	 * Get the background color for a button.
	 * 
	 * @return The background color for a button.
	 */
	public int getButtonBackgroundColor() {
		return mButtonBackgroundColor;
	}
}