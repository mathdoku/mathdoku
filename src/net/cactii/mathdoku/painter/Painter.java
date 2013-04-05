package net.cactii.mathdoku.painter;

import net.cactii.mathdoku.painter.MaybeValuePainter.MaybeGridType;
import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.PathEffect;
import android.graphics.Typeface;

public class Painter {
	// Singleton reference to the GridPainter object.
	private static Painter mPainterSingletonInstance = null;

	// The context in which the painter is created.
	private Context mContext;

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
		CARVED, NEWSPAPER, DARK
	};

	// Theme installed in painter
	private GridTheme mTheme;

	// Reference to all sub painters
	private GridPainter mGridPainter;
	private CagePainter mCagePainter;
	private CellPainter mCellPainter;
	private UserValuePainter mUserValuePainter;
	private MaybeValuePainter mMaybe3x3Painter;
	private MaybeValuePainter mMaybe2x5Painter;
	private MaybeValuePainter mMaybe1x9Painter;
	
	/**
	 * Creates a new instance of {@link #GridPainter()}.
	 * 
	 * This object can not be instantiated directly. Use {@link #getInstance()}
	 * to get the singleton reference to the GridPainter object.
	 * 
	 * @param context
	 *            The context in which the GridPainter is created.
	 * 
	 */
	private Painter(Context context) {
		mContext = context;

		// Create the painters
		mGridPainter = new GridPainter(this);
		mCagePainter = new CagePainter(this);
		mCellPainter = new CellPainter(this);
		mUserValuePainter = new UserValuePainter(this);
		mMaybe3x3Painter = new MaybeValuePainter(this, MaybeGridType.GRID_3X3);
		mMaybe2x5Painter = new MaybeValuePainter(this, MaybeGridType.GRID_2X5);
		mMaybe1x9Painter = new MaybeValuePainter(this, MaybeGridType.GRID_1X9);

		// Set the size of the borders.
		setBorderSizes(false);

		// Apply default theme to the painters.
		setTheme(GridTheme.NEWSPAPER); // TODO: Add theme to constructor????
	}

	/**
	 * Gets the singleton reference to the GridPainter object. If it does not
	 * yet exist than it will be created.
	 * 
	 * @param context
	 *            The context in which the GridPainter is created.
	 * 
	 * @return The singleton reference to the GridPainter object.
	 */
	public static Painter getInstance(Context context) {
		if (mPainterSingletonInstance == null) {
			// Only the first time this method is called, the object will be
			// created.
			mPainterSingletonInstance = new Painter(context);
		}
		return mPainterSingletonInstance;
	}

	/**
	 * Gets the singleton reference to the GridPainter object. If it does not
	 * yet exist an exception will be thrown.
	 * 
	 * @return The singleton reference to the GridPainter object.
	 */
	public static Painter getInstance() {
		if (mPainterSingletonInstance == null) {
			throw new NullPointerException(
					"GridPainter can not be retrieved if not instantiated before.");
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
		setTypeface(theme);
		setPathEffects(theme);
		setTextColors(theme);

		// Propagate theme settings to children
		mGridPainter.setTheme(theme);
		mCagePainter.setTheme(theme);
		mCellPainter.setTheme(theme);
		mUserValuePainter.setTheme(theme);
		mMaybe3x3Painter.setTheme(theme);
		mMaybe2x5Painter.setTheme(theme);
		mMaybe1x9Painter.setTheme(theme);

		// To be sure, reapply size specific settings as well.
		setCellSize(mCellPainter.getCellSize());
	}

	/**
	 * Changes the painter objects to handle cells of a the given size.
	 * 
	 * @param size
	 *            The size of cells.
	 */
	public void setCellSize(float size) {
		if (size == mCellPainter.getCellSize()) {
			// No change in cell size.
			return;
		}

		// Set width of borders dependent on new size of cells.
		setBorderSizes(size <= 80);

		// Propagate the new cell size to all border-painters
		mCagePainter.setCellSize(size);
		mCellPainter.setCellSize(size);
		mUserValuePainter.setCellSize(size);
		mMaybe3x3Painter.setCellSize(size);
		mMaybe2x5Painter.setCellSize(size);
		mMaybe1x9Painter.setCellSize(size);
	}

	/**
	 * Set the default typeface for this theme.
	 * 
	 * @param gridTheme
	 *            The theme for which the typeface has to be set.
	 */
	private void setTypeface(GridTheme gridTheme) {
		if (gridTheme == GridTheme.CARVED) {
			mTypefaceTheme = Typeface.createFromAsset(mContext.getAssets(),
					"fonts/font.ttf");
		} else {
			mTypefaceTheme = Typeface.create(Typeface.SANS_SERIF,
					Typeface.NORMAL);
		}
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
	 * Set the default path effect for this theme.
	 * 
	 * @param gridTheme
	 *            The theme for which the path effect has to be set.
	 */
	private void setPathEffects(GridTheme gridTheme) {
		if (gridTheme == GridTheme.CARVED) {
			mPathEffectTheme = new DiscretePathEffect(20, 1);
		} else {
			mPathEffectTheme = new DashPathEffect(new float[] { 2, 2 }, 0);
		}
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
		case CARVED:
			mHighlightedTextColorNormalInputMode = 0xFF2215DD; 
			mHighlightedTextColorMaybeInputMode = 0xFFE61EBE; 
			mDefaultTextColor = 0xFF000000;
			break;
		case NEWSPAPER:
			mHighlightedTextColorNormalInputMode = 0xFF2215DD;
			mHighlightedTextColorMaybeInputMode = 0xFFE61EBE; 
			mDefaultTextColor = 0xFF000000;
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
	 * Get the maybe 3x3 painter.
	 * 
	 * @return The maybe 3x3 painter.
	 */
	public MaybeValuePainter getMaybe3x3Painter() {
		return mMaybe3x3Painter;
	}

	/**
	 * Get the maybe 2x5 painter.
	 * 
	 * @return The maybe 2x5 painter.
	 */
	public MaybeValuePainter getMaybe2x5Painter() {
		return mMaybe2x5Painter;
	}

	/**
	 * Get the maybe 1x9 painter.
	 * 
	 * @return The maybe 1x9 painter.
	 */
	public MaybeValuePainter getMaybe1x9Painter() {
		return mMaybe1x9Painter;
	}
}