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
		public Paint mInnerPaint;
		public Paint mOuterPaint;
		public int mBackgroundColor;
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

		public Paint mBackgroundWarningPaint;
		public Paint mBackgroundCheatedPaint;
		public Paint mBackgroundSelectedPaint;

		public Paint mBorderWrongPaint;
	}

	public CellPainter mCellPainter;

	// Painter for the user value
	public class UserValuePainter {
		public Paint mPaint;

		// Offsets of user value within cell
		public float mLeftOffset;
		public float mTopOffset;
	}

	public UserValuePainter mUserValuePainter;

	// Painter for 3x3 grid of possible values
	public class Maybe3x3Painter {
		public Paint mTextPaint;

		// Offset within cell
		public float mLeftOffset;
		public float mTopOffset;
		public float mScale;
	}

	public Maybe3x3Painter mMaybe3x3Painter;

	// Painter for single line of possible values
	public class Maybe1x9Painter {
		public Paint mTextPaint;

		// Offset within cell
		public float mLeftOffset;
		public float mTopOffset;
	}

	public Maybe1x9Painter mMaybe1x9Painter;

	// Painter for the cage
	public class CagePainter {
		public Paint mBorderPaint;
		public Paint mBorderSelectedPaint;

		public Paint mTextPaint;
	}

	public CagePainter mCagePainter;

	// Border sizes
	private final static int BORDER_STROKE_HAIR_LINE = 0;
	private final static int BORDER_STROKE_WIDTH_THIN = 1;
	private final static int BORDER_STROKE_WIDTH_NORMAL = 2;
	private final static int BORDER_STROKE_WIDTH_THICK = 5;

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
		// Create the grid painters
		mGridPainter = new GridPainter();
		mGridPainter.mInnerPaint = new Paint();
		mGridPainter.mInnerPaint.setColor(0x80000000);
		mGridPainter.mInnerPaint.setStrokeWidth(BORDER_STROKE_HAIR_LINE);

		mGridPainter.mOuterPaint = new Paint();
		mGridPainter.mOuterPaint.setColor(0xFF000000);
		mGridPainter.mOuterPaint.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
		mGridPainter.mOuterPaint.setStyle(Style.STROKE);

		// Typefaces
		mTypefaceCarved = Typeface.createFromAsset(context.getAssets(),
				"fonts/font.ttf");
		mTypefaceSansSerif = Typeface.create(Typeface.SANS_SERIF,
				Typeface.NORMAL);

		// Path effects
		mPathEffectHandDrawn = new DiscretePathEffect(20, 1);
		mPathEffectDashed = new DashPathEffect(new float[] { 2, 2 }, 0);

		// Create the painter for the cell.
		mCellPainter = new CellPainter();
		mCellPainter.mBorderWrongPaint = new Paint();
		mCellPainter.mBorderWrongPaint.setAntiAlias(true);
		mCellPainter.mBackgroundWarningPaint = new Paint();
		mCellPainter.mBackgroundWarningPaint.setColor(0x50FF0000);
		mCellPainter.mBackgroundWarningPaint.setStyle(Paint.Style.FILL);
		mCellPainter.mBackgroundCheatedPaint = new Paint();
		mCellPainter.mBackgroundCheatedPaint.setColor(0x90ffcea0);
		mCellPainter.mBackgroundCheatedPaint.setStyle(Paint.Style.FILL);
		mCellPainter.mBackgroundSelectedPaint = new Paint();
		mCellPainter.mBackgroundSelectedPaint.setColor(0xD0F0D042);

		// Create the painter for the user value
		mUserValuePainter = new UserValuePainter();
		mUserValuePainter.mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// Create the possible values 3x3 grid painter
		mMaybe3x3Painter = new Maybe3x3Painter();
		mMaybe3x3Painter.mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaybe3x3Painter.mTextPaint.setTextSize(10);
		mMaybe3x3Painter.mTextPaint.setTypeface(mTypefaceSansSerif);
		mMaybe3x3Painter.mTextPaint.setFakeBoldText(true);

		// Create the possible values single line painter
		mMaybe1x9Painter = new Maybe1x9Painter();
		mMaybe1x9Painter.mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mMaybe1x9Painter.mTextPaint.setTextSize(10);
		mMaybe1x9Painter.mTextPaint.setTypeface(mTypefaceSansSerif);
		mMaybe1x9Painter.mTextPaint.setFakeBoldText(false);

		// Create cage painter
		mCagePainter = new CagePainter();
		mCagePainter.mBorderPaint = new Paint();
		mCagePainter.mBorderPaint.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
		mCagePainter.mBorderSelectedPaint = new Paint();
		mCagePainter.mBorderSelectedPaint
				.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
		mCagePainter.mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCagePainter.mTextPaint.setTextSize(14);

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
			mGridPainter.mInnerPaint.setAntiAlias(true);
			mGridPainter.mInnerPaint.setPathEffect(mPathEffectHandDrawn);
			mGridPainter.mInnerPaint.setColor(0xbf906050);

			mGridPainter.mOuterPaint.setAntiAlias(true);
			mGridPainter.mOuterPaint.setPathEffect(mPathEffectHandDrawn);
			mGridPainter.mOuterPaint.setColor(0xff000000);

			mGridPainter.mBackgroundColor = 0x7ff0d090;

			mGridPainter.mSolvedTypeface = mTypefaceCarved;

			mCellPainter.mBorderWrongPaint.setColor(0xFFBB0000);
			mCellPainter.mBorderWrongPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCellPainter.mBorderWrongPaint.setPathEffect(mPathEffectHandDrawn);

			mCellPainter.mBackgroundSelectedPaint.setStyle(Paint.Style.FILL);

			mUserValuePainter.mPaint.setColor(0xFF000000);
			mUserValuePainter.mPaint.setTypeface(mTypefaceCarved);

			mCagePainter.mTextPaint.setColor(0xFF0000A0);
			mCagePainter.mTextPaint.setTypeface(mTypefaceCarved);

			mCagePainter.mBorderPaint.setColor(0xFF000000);
			mCagePainter.mBorderPaint.setAntiAlias(true);
			mCagePainter.mBorderPaint.setPathEffect(mPathEffectHandDrawn);

			mCagePainter.mBorderSelectedPaint.setColor(0xFF000000);
			mCagePainter.mBorderSelectedPaint.setAntiAlias(true);
			mCagePainter.mBorderSelectedPaint
					.setPathEffect(mPathEffectHandDrawn);

			mMaybe3x3Painter.mTextPaint.setColor(0xFF000000);

			mMaybe1x9Painter.mTextPaint.setColor(0xFF000000);
			break;
		case NEWSPAPER:
			mGridPainter.mInnerPaint.setAntiAlias(true);
			mGridPainter.mInnerPaint.setPathEffect(mPathEffectDashed);
			mGridPainter.mInnerPaint.setColor(0x80000000);

			mGridPainter.mOuterPaint.setAntiAlias(false);
			mGridPainter.mOuterPaint.setPathEffect(null);
			mGridPainter.mOuterPaint.setColor(0xff000000);

			mGridPainter.mBackgroundColor = 0xffffffff;

			mGridPainter.mSolvedTypeface = mTypefaceSansSerif;

			mCellPainter.mBorderWrongPaint.setColor(0xFFBB0000);
			mCellPainter.mBorderWrongPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
			mCellPainter.mBorderWrongPaint.setPathEffect(null);

			mCellPainter.mBackgroundSelectedPaint.setStyle(Paint.Style.FILL);

			mUserValuePainter.mPaint.setColor(0xFF000000);
			mUserValuePainter.mPaint.setTypeface(mTypefaceSansSerif);

			mCagePainter.mTextPaint.setColor(0xFF0000A0);
			mCagePainter.mTextPaint.setTypeface(mTypefaceSansSerif);

			mCagePainter.mBorderPaint.setColor(0xFF000000);
			mCagePainter.mBorderPaint.setAntiAlias(false);
			mCagePainter.mBorderPaint.setPathEffect(null);

			mCagePainter.mBorderSelectedPaint.setColor(0xFF000000);
			mCagePainter.mBorderSelectedPaint.setAntiAlias(false);
			mCagePainter.mBorderSelectedPaint.setPathEffect(null);

			mMaybe3x3Painter.mTextPaint.setColor(0xFF000000);

			mMaybe1x9Painter.mTextPaint.setColor(0xFF000000);
			break;
		case DARK:
			mGridPainter.mInnerPaint.setAntiAlias(true);
			mGridPainter.mInnerPaint.setPathEffect(mPathEffectDashed);
			mGridPainter.mInnerPaint.setColor(0xff7f7f7f);

			mGridPainter.mOuterPaint.setAntiAlias(true);
			mGridPainter.mOuterPaint.setPathEffect(null);
			mGridPainter.mOuterPaint.setColor(0xffe0e0e0);

			mGridPainter.mBackgroundColor = 0xff000000;

			mGridPainter.mSolvedTypeface = mTypefaceSansSerif;

			mCellPainter.mBorderWrongPaint.setColor(0xFFBB0000);
			mCellPainter.mBorderWrongPaint
					.setStrokeWidth(BORDER_STROKE_WIDTH_THICK);
			mCellPainter.mBorderWrongPaint.setPathEffect(null);

			mCellPainter.mBackgroundSelectedPaint.setStyle(Paint.Style.STROKE);

			mUserValuePainter.mPaint.setColor(0xFFFFFFFF);
			mUserValuePainter.mPaint.setTypeface(mTypefaceSansSerif);

			mCagePainter.mTextPaint.setColor(0xFFFFFFC0);
			mCagePainter.mTextPaint.setTypeface(mTypefaceSansSerif);

			mCagePainter.mBorderPaint.setColor(0xFFFFFFFF);
			mCagePainter.mBorderPaint.setAntiAlias(true);
			mCagePainter.mBorderPaint.setPathEffect(null);

			mCagePainter.mBorderSelectedPaint.setColor(0xB0A0A030);
			mCagePainter.mBorderSelectedPaint.setAntiAlias(true);
			mCagePainter.mBorderSelectedPaint.setPathEffect(null);

			mMaybe3x3Painter.mTextPaint.setColor(0xFFFFFFFF);

			mMaybe1x9Painter.mTextPaint.setColor(0xB0A0A030);
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

		// Text size is 75% of cell size
		mUserValuePainter.mPaint.setTextSize((int) (size * 3 / 4));

		// Compute the offsets at which the user value will be displayed within
		// the cell
		mUserValuePainter.mLeftOffset = mCellPainter.mCellSize / 2
				- mUserValuePainter.mPaint.getTextSize() / 4;
		if (mTheme == GridTheme.NEWSPAPER) {
			mUserValuePainter.mTopOffset = mCellPainter.mCellSize / 2
					+ mUserValuePainter.mPaint.getTextSize() * 2 / 5;
		} else {
			mUserValuePainter.mTopOffset = mCellPainter.mCellSize / 2
					+ mUserValuePainter.mPaint.getTextSize() / 3;
		}

		// Compute the offsets at which the 3x3 grid of possible values will be
		// displayed within the cell
		mMaybe3x3Painter.mTextPaint
				.setTextSize((int) (this.mCellPainter.mCellSize / 4.5));
		mMaybe3x3Painter.mLeftOffset = (int) (mCellPainter.mCellSize / 3);
		mMaybe3x3Painter.mTopOffset = (int) (mCellPainter.mCellSize / 2) + 1;
		mMaybe3x3Painter.mScale = (float) 0.21 * mCellPainter.mCellSize;

		// Compute the offsets at which the 1x9 grid of possible values will be
		// displayed within the cell
		mMaybe1x9Painter.mTextPaint
				.setTextSize((int) (this.mCellPainter.mCellSize / 4));
		mMaybe1x9Painter.mLeftOffset = 3;
		mMaybe1x9Painter.mTopOffset = mCellPainter.mCellSize
				- mMaybe1x9Painter.mTextPaint.getTextSize();

		// Text size of cage text is 1/3 of cell size
		mCagePainter.mTextPaint.setTextSize((int) (mCellPainter.mCellSize / 3));
	}

	/**
	 * Change the width of the border of the grid.
	 * 
	 * @param thin
	 *            True in case a small border needs to be set. False in case a
	 *            normal border should be used.
	 */
	public void setGridBorder(boolean thin) {
		if (thin) {
			mGridPainter.mOuterPaint.setStrokeWidth(BORDER_STROKE_WIDTH_THIN);
		} else {
			mGridPainter.mOuterPaint.setStrokeWidth(BORDER_STROKE_WIDTH_NORMAL);
		}
	}
}