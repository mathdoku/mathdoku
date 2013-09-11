package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.R;
import android.graphics.DashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Typeface;

public class Painter {
	// Singleton reference to the GridPainter object.
	private static Painter mPainterSingletonInstance = null;

	// Typeface to be used
	private final Typeface mTypefaceTheme;

	// Path effect to be used (theme specific)
	private final PathEffect mPathEffectTheme;

	// Text colors (dependent on theme) per input mode
	private int mHighlightedTextColorNormalInputMode;
	private int mHighlightedTextColorMaybeInputMode;
	private int mNormalInputModeButtonResId;
	private int mMaybeInputModeButtonResId;
	private int mDefaultTextColor;

	// Themes available
	public enum GridTheme {
		LIGHT, DARK
	};

	public enum DigitPainterMode {
		INPUT_MODE_BASED, MONOCHROME
	};

	// Theme installed in painter
	private GridTheme mTheme;

	// Reference to all sub painters
	private final GridPainter mGridPainter;
	private final CagePainter mCagePainter;
	private final CellPainter mCellPainter;
	private final UserValuePainter mUserValuePainter;
	private final MaybeValuePainter mMaybeGridPainter;
	private final MaybeValuePainter mMaybeLinePainter;
	private final SwipeBorderPainter mSwipeBorderPainter;
	private final TickerTapePainter mTickerTapePainter;
	private final PagerTabStripPainter mPagerTabStripPainter;
	private final NavigationDrawerPainter mNavigationDrawerPainter;
	private final SolvedTextPainter mSolvedTextPainter;

	// Background color of buttons and ticker tape
	private final int mButtonBackgroundColor;
	
	// Foreground colour of maybe button mode.
	private final int mDigitFgColor;
	private final int mDigitFgMaybeColor;

	/**
	 * Creates a new instance of {@link #GridPainter()}.
	 * 
	 * This object can not be instantiated directly. Use {@link #getInstance()}
	 * to get the singleton reference to the GridPainter object.
	 * 
	 */
	private Painter() {
		// Set the typeface
		mTypefaceTheme = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

		// Set the path effect
		mPathEffectTheme = new DashPathEffect(new float[] { 2, 2 }, 0);

		// Button background color
		mButtonBackgroundColor = 0xFF33B5E5;
		
		mDigitFgColor = 0xFFFFFFFF;
		mDigitFgMaybeColor = 0xFFFFFF00;

		// Create the painters
		mGridPainter = new GridPainter(this);
		mCagePainter = new CagePainter(this);
		mCellPainter = new CellPainter(this);
		mUserValuePainter = new UserValuePainter(this);
		mMaybeGridPainter = new MaybeValuePainter(this);
		mMaybeLinePainter = new MaybeValuePainter(this);
		mSwipeBorderPainter = new SwipeBorderPainter(this);
		mTickerTapePainter = new TickerTapePainter(this);
		mPagerTabStripPainter = new PagerTabStripPainter(this);
		mNavigationDrawerPainter = new NavigationDrawerPainter(this);
		mSolvedTextPainter = new SolvedTextPainter(this);

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
		setInputModeColors(theme);

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
	public void setCellSize(float size) {
		// Set width of borders dependent on new size of cells.
		setBorderSizes(size <= 80);

		// Propagate the new cell size to all border-painters
		mCagePainter.setCellSize(size);
		mCellPainter.setCellSize(size);
		mUserValuePainter.setCellSize(size);
		mMaybeGridPainter.setCellSize(size);
		mMaybeLinePainter.setCellSize(size);
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
	private void setInputModeColors(GridTheme gridTheme) {
		// Colors of highlighted digits (also use as base for the swipe border
		// colors) should be equal for all themes as the input_mode-images are
		// currently only available in this color set.
		mHighlightedTextColorNormalInputMode = 0xFF0C97FA;
		mHighlightedTextColorMaybeInputMode = 0xFFFF8A00;

		// The default color will of course be set relevant to the theme.
		switch (gridTheme) {
		case LIGHT:
			mMaybeInputModeButtonResId = R.drawable.input_mode_maybe_light;
			mNormalInputModeButtonResId = R.drawable.input_mode_normal_light;
			mDefaultTextColor = 0xFF212121;
			break;
		case DARK:
			mMaybeInputModeButtonResId = R.drawable.input_mode_maybe_dark;
			mNormalInputModeButtonResId = R.drawable.input_mode_normal_dark;
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
	
	/**
	 * Get the foreground colour of the digit buttons.
	 *  
	 * @return the colour for the button.
	 */
	public int getDigitFgColor() {
		return mDigitFgColor;
	}
	/**
	 * Get the foreground colour of the digit buttons in maybe mode.
	 *  
	 * @return the colour for the button.
	 */
	public int getDigitFgMaybeColor() {
		return mDigitFgMaybeColor;
	}

	/**
	 * Get the ticker tape painter.
	 * 
	 * @return The ticker tape painter.
	 */
	public TickerTapePainter getTickerTapePainter() {
		return mTickerTapePainter;
	}

	/**
	 * Get the pager tab strip painter.
	 * 
	 * @return The pager tab strip painter.
	 */
	public PagerTabStripPainter getPagerTabStripPainter() {
		return mPagerTabStripPainter;
	}

	/**
	 * Get the navigation drawer painter.
	 * 
	 * @return The navigation drawer painter.
	 */
	public NavigationDrawerPainter getNavigationDrawerPainter() {
		return mNavigationDrawerPainter;
	}

	/**
	 * Get the solved text painter.
	 * 
	 * @return The solved text painter.
	 */
	public SolvedTextPainter getSolvedTextPainter() {
		return mSolvedTextPainter;
	}

	/**
	 * Set the color mode of the digit painters.
	 * 
	 * @param distinctColors
	 *            True in case distinct colors should be used dependent on the
	 *            input mode. False in case a monochrome color should be used.
	 */
	public void setColorMode(DigitPainterMode digitPainterMode) {
		mMaybeGridPainter.setColorMode(digitPainterMode);
		mMaybeLinePainter.setColorMode(digitPainterMode);
		mUserValuePainter.setColorMode(digitPainterMode);

	}

	/**
	 * Get the normal input mode button.
	 * 
	 * @return The normal input mode button.
	 */
	public int getNormalInputModeButton() {
		return mNormalInputModeButtonResId;
	}

	/**
	 * Get the maybe input mode button.
	 * 
	 * @return The maybe input mode button.
	 */
	public int getMaybeInputModeButton() {
		return mMaybeInputModeButtonResId;
	}
}