package net.cactii.mathdoku;

import android.content.Context;
import android.graphics.DashPathEffect;
import android.graphics.DiscretePathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;

public class Painter {
	// Singleton reference to the GridPainter object.
	private static Painter mPainterSingletonInstance = null;

	// Themes available
	public enum GridTheme {
		CARVED, NEWSPAPER, DARK
	};

	// Theme installed in painter
	private GridTheme mTheme;

	// Painters for the grid itself
	public class GridPainter {
		public Paint mBorderPaint;
		public Paint mBackgroundPaint;
		public Typeface mSolvedTypeface;
	}

	public GridPainter mGridPainter;

	// Typefaces used
	private Typeface mTypefaceCarved;
	private Typeface mTypefaceSansSerif;

	// Path effects
	private DiscretePathEffect mPathEffectHandDrawn;
	private DashPathEffect mPathEffectDashed;

	// Painter for the cell
	public class CellPainter {
		public float mCellSize;

		public Paint mUnusedBorderPaint;

		public class CellBorderBackground {
			public Paint mBorderPaint;
			public Paint mBackgroundPaint;
		}

		public CellBorderBackground mInvalid;
		public CellBorderBackground mCheated;
		public CellBorderBackground mWarning;
		public CellBorderBackground mSelected;

		public CellPainter() {
			mInvalid = new CellBorderBackground();
			mCheated = new CellBorderBackground();
			mWarning = new CellBorderBackground();
			mSelected = new CellBorderBackground();
		}
	}

	public CellPainter mCellPainter;

	// Painter for the user value
	public class UserValuePainter {
		public Paint mTextPaintNormalInputMode;
		public Paint mTextPaintMaybeInputMode;

		// Offsets of user value within cell
		public float mLeftOffset;
		public float mTopOffset;
	}

	public UserValuePainter mUserValuePainter;

	// Painter for 3x3 grid of possible values
	public class Maybe3x3Painter {
		public Paint mTextPaintNormalInputMode;
		public Paint mTextPaintMaybeInputMode;

		// Offset within cell
		public float mLeftOffset;
		public float mTopOffset;
		public float mScale;
	}

	public Maybe3x3Painter mMaybe3x3Painter;

	// Painter for single line of possible values
	public class Maybe1x9Painter {
		public Paint mTextPaintNormalInputMode;
		public Paint mTextPaintMaybeInputMode;

		// Offset within cell
		public float mLeftOffset;
		public float mTopOffset;
	}

	public Maybe1x9Painter mMaybe1x9Painter;

	// Painter for the cage
	public class CagePainter {
		public Paint mBorderPaint;
		public Paint mBorderSelectedPaint;
		public Paint mBorderBadMathPaint;
		public Paint mBorderSelectedBadMathPaint;

		public Paint mTextPaint;
	}

	public CagePainter mCagePainter;
	
	// Text colors (dependent on theme) per input mode
	public int mHighlightedTextColorNormalInputMode;
	public int mHighlightedTextColorMaybeInputMode;
	public int mDefaultTextColor;

	// Border sizes
	private int BORDER_STROKE_HAIR_LINE = 0;
	private int BORDER_STROKE_WIDTH_THIN = 1;
	private int BORDER_STROKE_WIDTH_NORMAL = 2;
	private int BORDER_STROKE_WIDTH_MEDIUM = 3;
	private int BORDER_STROKE_WIDTH_THICK = 4;

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
		// Initialize the common painter properties which will not vary during
		// the lifecycle.
		setCommonPaintProperties(context);

		// Set the size of the borders.
		setBorderSizes(false);

		// Apply default theme to the painters.
		setTheme(GridTheme.NEWSPAPER);
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
	 * Sets all common paint properties which will not vary during the lifecylce
	 * of the global painter object.
	 */
	private void setCommonPaintProperties(Context context) {
		// Create the grid painters
		mGridPainter = new GridPainter();

		mGridPainter.mBorderPaint = new Paint();
		mGridPainter.mBorderPaint.setStyle(Style.STROKE);

		mGridPainter.mBackgroundPaint = new Paint();
		mGridPainter.mBorderPaint.setStyle(Style.FILL);

		// Typefaces
		mTypefaceCarved = Typeface.createFromAsset(context.getAssets(),
				"fonts/font.ttf");
		mTypefaceSansSerif = Typeface.create(Typeface.SANS_SERIF,
				Typeface.NORMAL);

		// Path effects
		mPathEffectHandDrawn = new DiscretePathEffect(20, 1);
		mPathEffectDashed = new DashPathEffect(new float[] { 2, 2 }, 0);

		// Create the painters for the cell.
		mCellPainter = new CellPainter();

		mCellPainter.mUnusedBorderPaint = new Paint();
		mCellPainter.mUnusedBorderPaint.setColor(0x80000000);
		mCellPainter.mUnusedBorderPaint.setStyle(Style.STROKE);

		mCellPainter.mInvalid.mBorderPaint = new Paint();
		mCellPainter.mInvalid.mBorderPaint.setAntiAlias(true);

		mCellPainter.mInvalid.mBackgroundPaint = new Paint();

		mCellPainter.mWarning.mBorderPaint = new Paint();
		mCellPainter.mWarning.mBorderPaint.setColor(0x50FF0000);

		mCellPainter.mWarning.mBackgroundPaint = new Paint();
		mCellPainter.mWarning.mBackgroundPaint.setColor(0x50FF0000);

		mCellPainter.mCheated.mBorderPaint = new Paint();
		mCellPainter.mCheated.mBorderPaint.setColor(0x90ffcea0);

		mCellPainter.mCheated.mBackgroundPaint = new Paint();
		mCellPainter.mCheated.mBackgroundPaint.setColor(0x90ffcea0);

		mCellPainter.mSelected.mBorderPaint = new Paint();
		mCellPainter.mSelected.mBorderPaint.setColor(0xD0F0D042);

		mCellPainter.mSelected.mBackgroundPaint = new Paint();
		mCellPainter.mSelected.mBackgroundPaint.setColor(0xD0F0D042);

		// Create the painters for the user value
		mUserValuePainter = new UserValuePainter();

		mUserValuePainter.mTextPaintNormalInputMode = new Paint(
				Paint.ANTI_ALIAS_FLAG);
		mUserValuePainter.mTextPaintMaybeInputMode = new Paint(
				Paint.ANTI_ALIAS_FLAG);

		// Create painters for the possible values 3x3 grid
		mMaybe3x3Painter = new Maybe3x3Painter();

		mMaybe3x3Painter.mTextPaintNormalInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaybe3x3Painter.mTextPaintNormalInputMode.setTextSize(10);
		mMaybe3x3Painter.mTextPaintNormalInputMode.setTypeface(mTypefaceSansSerif);
		mMaybe3x3Painter.mTextPaintNormalInputMode.setFakeBoldText(true);

		mMaybe3x3Painter.mTextPaintMaybeInputMode = new Paint(
				Paint.ANTI_ALIAS_FLAG);
		mMaybe3x3Painter.mTextPaintMaybeInputMode.setTextSize(10);
		mMaybe3x3Painter.mTextPaintMaybeInputMode.setTypeface(mTypefaceSansSerif);
		mMaybe3x3Painter.mTextPaintMaybeInputMode.setFakeBoldText(true);

		// Create the painters for the possible values single line
		mMaybe1x9Painter = new Maybe1x9Painter();

		mMaybe1x9Painter.mTextPaintNormalInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaybe1x9Painter.mTextPaintNormalInputMode.setTextSize(10);
		mMaybe1x9Painter.mTextPaintNormalInputMode.setTypeface(mTypefaceSansSerif);
		mMaybe1x9Painter.mTextPaintNormalInputMode.setFakeBoldText(false);

		mMaybe1x9Painter.mTextPaintMaybeInputMode = new Paint(
				Paint.ANTI_ALIAS_FLAG);
		mMaybe1x9Painter.mTextPaintMaybeInputMode.setTextSize(10);
		mMaybe1x9Painter.mTextPaintMaybeInputMode.setTypeface(mTypefaceSansSerif);
		mMaybe1x9Painter.mTextPaintMaybeInputMode.setFakeBoldText(false);

		// Create cage painter
		mCagePainter = new CagePainter();
		mCagePainter.mBorderPaint = new Paint();
		mCagePainter.mBorderSelectedPaint = new Paint();
		mCagePainter.mBorderBadMathPaint = new Paint();
		mCagePainter.mBorderSelectedBadMathPaint = new Paint();
		mCagePainter.mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCagePainter.mTextPaint.setTextSize(14);
	}

	/**
	 * Change the width of the border of the grid.
	 * 
	 * @param thin
	 *            True in case a small border needs to be set. False in case a
	 *            normal border should be used.
	 */
	private void setBorderSizes(boolean thin) {
		mGridPainter.mBorderPaint.setStrokeWidth(BORDER_STROKE_WIDTH_THIN);
		if (thin) {
			mCellPainter.mUnusedBorderPaint
					.setStrokeWidth(BORDER_STROKE_HAIR_LINE);
			mCellPainter.mInvalid.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCellPainter.mWarning.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCellPainter.mCheated.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCellPainter.mSelected.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCagePainter.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCagePainter.mBorderSelectedPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_MEDIUM);
			mCagePainter.mBorderBadMathPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCagePainter.mBorderSelectedBadMathPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_MEDIUM);
		} else {
			mCellPainter.mUnusedBorderPaint
					.setStrokeWidth(BORDER_STROKE_HAIR_LINE);
			mCellPainter.mInvalid.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
			mCellPainter.mWarning.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
			mCellPainter.mCheated.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
			mCellPainter.mSelected.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
			mCagePainter.mBorderPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCagePainter.mBorderSelectedPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
			mCagePainter.mBorderBadMathPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCagePainter.mBorderSelectedBadMathPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
		}
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

		switch (theme) {
		case CARVED:
			mHighlightedTextColorNormalInputMode = 0xFF15DC23; // TODO: determine good color
			mHighlightedTextColorMaybeInputMode = 0xFFE61EBE; // TODO: determine good color
			mDefaultTextColor = 0xFF000000;
			break;
		case NEWSPAPER:
			mHighlightedTextColorNormalInputMode = 0xFF15DC23; // TODO: determine good color
			mHighlightedTextColorMaybeInputMode = 0xFFE61EBE; // TODO: determine good color
			mDefaultTextColor = 0xFF000000;
			break;
		case DARK:
			mHighlightedTextColorNormalInputMode = 0xFF15DC23; // TODO: determine good color
			mHighlightedTextColorMaybeInputMode = 0xFFE61EBE; // TODO: determine good color
			mDefaultTextColor = 0xFFFFFFFF;
			break;
		}
		mUserValuePainter.mTextPaintNormalInputMode.setColor(mHighlightedTextColorNormalInputMode);
		mUserValuePainter.mTextPaintMaybeInputMode.setColor(mDefaultTextColor);
		mMaybe3x3Painter.mTextPaintNormalInputMode.setColor(mDefaultTextColor);
		mMaybe3x3Painter.mTextPaintMaybeInputMode.setColor(mHighlightedTextColorMaybeInputMode);
		mMaybe1x9Painter.mTextPaintNormalInputMode.setColor(mDefaultTextColor);
		mMaybe1x9Painter.mTextPaintMaybeInputMode.setColor(mHighlightedTextColorMaybeInputMode);

		switch (theme) {
		case CARVED:
			mGridPainter.mBorderPaint.setAntiAlias(true);
			mGridPainter.mBorderPaint.setPathEffect(mPathEffectHandDrawn);
			mGridPainter.mBorderPaint.setColor(0xff000000);

			mGridPainter.mBackgroundPaint.setColor(0x7ff0d090);

			mGridPainter.mSolvedTypeface = mTypefaceCarved;

			mCellPainter.mUnusedBorderPaint.setAntiAlias(true);
			mCellPainter.mUnusedBorderPaint.setPathEffect(mPathEffectHandDrawn);
			mCellPainter.mUnusedBorderPaint.setColor(0xbf906050);

			mCellPainter.mInvalid.mBorderPaint.setColor(0xFFBB0000);

			mCellPainter.mInvalid.mBackgroundPaint.setColor(0xFFBB0000);
			mCellPainter.mInvalid.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mCellPainter.mWarning.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mCellPainter.mCheated.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mCellPainter.mSelected.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mUserValuePainter.mTextPaintNormalInputMode.setTypeface(mTypefaceCarved);

			mUserValuePainter.mTextPaintMaybeInputMode
					.setTypeface(mTypefaceCarved);

			mCagePainter.mTextPaint.setColor(0xFF0000A0);
			mCagePainter.mTextPaint.setTypeface(mTypefaceCarved);

			mCagePainter.mBorderPaint.setColor(0xFF000000);
			mCagePainter.mBorderPaint.setAntiAlias(true);
			mCagePainter.mBorderPaint.setPathEffect(mPathEffectHandDrawn);

			mCagePainter.mBorderBadMathPaint.setColor(0xFFBB0000);
			mCagePainter.mBorderBadMathPaint.setAntiAlias(true);
			mCagePainter.mBorderBadMathPaint
					.setPathEffect(mPathEffectHandDrawn);

			mCagePainter.mBorderSelectedPaint.setColor(0xFF000000);
			mCagePainter.mBorderSelectedPaint.setAntiAlias(true);
			mCagePainter.mBorderSelectedPaint
					.setPathEffect(mPathEffectHandDrawn);

			mCagePainter.mBorderSelectedBadMathPaint.setColor(0xFFBB0000);
			mCagePainter.mBorderSelectedBadMathPaint.setAntiAlias(true);
			mCagePainter.mBorderSelectedBadMathPaint
					.setPathEffect(mPathEffectHandDrawn);
			break;
		case NEWSPAPER:
			mGridPainter.mBorderPaint.setAntiAlias(false);
			mGridPainter.mBorderPaint.setPathEffect(null);
			mGridPainter.mBorderPaint.setColor(0xFFAAAAAA);

			mGridPainter.mBackgroundPaint.setColor(0xffffffff);

			mGridPainter.mSolvedTypeface = mTypefaceSansSerif;

			mCellPainter.mUnusedBorderPaint.setAntiAlias(true);
			mCellPainter.mUnusedBorderPaint.setPathEffect(mPathEffectDashed);
			mCellPainter.mUnusedBorderPaint.setColor(0x80000000);

			mCellPainter.mInvalid.mBorderPaint.setColor(0xFFBB0000);

			mCellPainter.mInvalid.mBackgroundPaint.setColor(0xFFBB0000);
			mCellPainter.mInvalid.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mCellPainter.mWarning.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mCellPainter.mCheated.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mCellPainter.mSelected.mBackgroundPaint.setStyle(Paint.Style.FILL);

			mUserValuePainter.mTextPaintNormalInputMode
					.setTypeface(mTypefaceSansSerif);

			mUserValuePainter.mTextPaintMaybeInputMode
					.setTypeface(mTypefaceSansSerif);

			mCagePainter.mTextPaint.setColor(0xFF0000A0);
			mCagePainter.mTextPaint.setTypeface(mTypefaceSansSerif);

			mCagePainter.mBorderPaint.setColor(0xFF000000);
			mCagePainter.mBorderPaint.setAntiAlias(false);
			mCagePainter.mBorderPaint.setPathEffect(null);

			mCagePainter.mBorderBadMathPaint.setColor(0xFFBB0000);
			mCagePainter.mBorderBadMathPaint.setAntiAlias(true);
			mCagePainter.mBorderBadMathPaint.setPathEffect(null);

			mCagePainter.mBorderSelectedPaint.setColor(0xFF000000);
			mCagePainter.mBorderSelectedPaint.setAntiAlias(false);
			mCagePainter.mBorderSelectedPaint.setPathEffect(null);

			mCagePainter.mBorderSelectedBadMathPaint.setColor(0xFFBB0000);
			mCagePainter.mBorderSelectedBadMathPaint.setAntiAlias(true);
			mCagePainter.mBorderSelectedBadMathPaint.setPathEffect(null);
			break;
		case DARK:
			mGridPainter.mBorderPaint.setAntiAlias(true);
			mGridPainter.mBorderPaint.setPathEffect(null);
			mGridPainter.mBorderPaint.setColor(0xFFAAAAAA);

			mGridPainter.mBackgroundPaint.setColor(0xff000000);

			mGridPainter.mSolvedTypeface = mTypefaceSansSerif;

			mCellPainter.mUnusedBorderPaint.setAntiAlias(true);
			mCellPainter.mUnusedBorderPaint.setPathEffect(mPathEffectDashed);
			mCellPainter.mUnusedBorderPaint.setColor(0xff7f7f7f);

			mCellPainter.mInvalid.mBorderPaint.setColor(0xFFBB0000);
			mCellPainter.mInvalid.mBackgroundPaint.setStyle(Paint.Style.STROKE);

			mCellPainter.mWarning.mBackgroundPaint.setStyle(Paint.Style.STROKE);

			mCellPainter.mCheated.mBackgroundPaint.setStyle(Paint.Style.STROKE);

			mCellPainter.mSelected.mBackgroundPaint
					.setStyle(Paint.Style.STROKE);

			mUserValuePainter.mTextPaintNormalInputMode
					.setTypeface(mTypefaceSansSerif);

			mUserValuePainter.mTextPaintMaybeInputMode
					.setTypeface(mTypefaceSansSerif);

			mCagePainter.mTextPaint.setColor(0xFFFFFFC0);
			mCagePainter.mTextPaint.setTypeface(mTypefaceSansSerif);

			mCagePainter.mBorderPaint.setColor(0xFFFFFFFF);
			mCagePainter.mBorderPaint.setAntiAlias(true);
			mCagePainter.mBorderPaint.setPathEffect(null);

			mCagePainter.mBorderBadMathPaint.setColor(0xFFBB0000);
			mCagePainter.mBorderBadMathPaint.setAntiAlias(true);
			mCagePainter.mBorderBadMathPaint.setPathEffect(null);

			mCagePainter.mBorderSelectedPaint.setColor(0xFFA0A030);
			mCagePainter.mBorderSelectedPaint.setAntiAlias(true);
			mCagePainter.mBorderSelectedPaint.setPathEffect(null);

			mCagePainter.mBorderSelectedBadMathPaint.setColor(0xFFBB0000);
			mCagePainter.mBorderSelectedBadMathPaint.setAntiAlias(true);
			mCagePainter.mBorderSelectedBadMathPaint.setPathEffect(null);
			break;
		}

		// To be sure, reapply size specific settings as well.
		setCellSize(mCellPainter.mCellSize);
	}

	/**
	 * Changes the painter objects to handle cells of a the given size.
	 * 
	 * @param size
	 *            The size of cells.
	 */
	public void setCellSize(float size) {
		mCellPainter.mCellSize = size;

		// Set width of borders dependent on new size of cells.
		setBorderSizes(size <= 80);

		// Text size is 75% of cell size
		int userValueTextSize = (int) (size * 3 / 4);
		mUserValuePainter.mTextPaintNormalInputMode.setTextSize(userValueTextSize);
		mUserValuePainter.mTextPaintMaybeInputMode.setTextSize(userValueTextSize);

		// Compute the offsets at which the user value will be displayed within
		// the cell
		mUserValuePainter.mLeftOffset = mCellPainter.mCellSize / 2
				- userValueTextSize / 4;
		if (mTheme == GridTheme.NEWSPAPER) {
			mUserValuePainter.mTopOffset = mCellPainter.mCellSize / 2
					+ userValueTextSize * 2 / 5;
		} else {
			mUserValuePainter.mTopOffset = mCellPainter.mCellSize / 2
					+ userValueTextSize / 3;
		}

		// Compute the offsets at which the 3x3 grid of possible values will be
		// displayed within the cell
		int maybe3x3TextSize = (int) (this.mCellPainter.mCellSize / 4.5);
		mMaybe3x3Painter.mTextPaintNormalInputMode.setTextSize(maybe3x3TextSize);
		mMaybe3x3Painter.mTextPaintMaybeInputMode.setTextSize(maybe3x3TextSize);
		mMaybe3x3Painter.mLeftOffset = (int) (mCellPainter.mCellSize / 3);
		mMaybe3x3Painter.mTopOffset = (int) (mCellPainter.mCellSize / 2) + 1;
		mMaybe3x3Painter.mScale = (float) 0.21 * mCellPainter.mCellSize;

		// Compute the offsets at which the 1x9 grid of possible values will be
		// displayed within the cell
		int maybe1x9TextSize = (int) (this.mCellPainter.mCellSize / 4);
		mMaybe1x9Painter.mTextPaintNormalInputMode.setTextSize(maybe1x9TextSize);
		mMaybe1x9Painter.mTextPaintMaybeInputMode.setTextSize(maybe1x9TextSize);
		mMaybe1x9Painter.mLeftOffset = 3;
		mMaybe1x9Painter.mTopOffset = mCellPainter.mCellSize - maybe1x9TextSize;

		// Text size of cage text is 1/3 of cell size
		mCagePainter.mTextPaint.setTextSize((int) (mCellPainter.mCellSize / 3));
	}
}