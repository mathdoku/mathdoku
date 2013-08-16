package net.cactii.mathdoku.grid.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.painter.Painter;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * This GridPlayerRelativeLayout should be used in case a relative layout is
 * needed which is aligned with the visible grid (i.e. excluding the swipe
 * border on the outside of the visible grid).
 */
public class GridPlayerRelativeLayout extends RelativeLayout {
	public final static String TAG = "MathDoku.GridPlayerRelativeLayout";

	private boolean mMarginsInitialised;

	private final boolean mLeftMarginAdjustment;
	private final boolean mTopMarginAdjustment;
	private final boolean mRightMarginAdjustment;
	private final boolean mBottomMarginAdjustment;
	private final boolean mMarginAdjustment;

	private int mLeftMargin;
	private int mTopMargin;
	private int mRightMargin;
	private int mBottomMargin;

	public GridPlayerRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray typedArray = context.getTheme().obtainStyledAttributes(
				attrs, R.styleable.GridPlayerViewLayoutAlign, 0, 0);

		try {
			mLeftMarginAdjustment = (typedArray
					.getInt(R.styleable.GridPlayerViewLayoutAlign_layout_alignGridPlayerViewLeft,
							1) == 0);
			mTopMarginAdjustment = (typedArray
					.getInt(R.styleable.GridPlayerViewLayoutAlign_layout_alignGridPlayerViewTop,
							1) == 0);
			mRightMarginAdjustment = (typedArray
					.getInt(R.styleable.GridPlayerViewLayoutAlign_layout_alignGridPlayerViewRight,
							1) == 0);
			mBottomMarginAdjustment = (typedArray
					.getInt(R.styleable.GridPlayerViewLayoutAlign_layout_alignGridPlayerViewBottom,
							1) == 0);
		} finally {
			typedArray.recycle();
		}

		mMarginAdjustment = (mLeftMarginAdjustment || mTopMarginAdjustment
				|| mRightMarginAdjustment || mBottomMarginAdjustment);

		// Additional margins will be determined in first pass of onMeasure,
		// except no margins needs to be adjusted at all.
		mMarginsInitialised = (mMarginAdjustment == false);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mMarginAdjustment) {
			// Initialize margins if not yet done
			if (mMarginsInitialised == false) {
				mMarginsInitialised = true;

				// In case a margin has to be adjusted, it will be set to the
				// width of the swipe border which itself is 50% of the grid
				// cell size.
				if (mLeftMarginAdjustment) {
					mLeftMargin = (int) Painter.getInstance().getCellPainter()
							.getCellSize() / 2;
				}
				if (mTopMarginAdjustment) {
					mTopMargin = (int) Painter.getInstance().getCellPainter()
							.getCellSize() / 2;
				}
				if (mRightMarginAdjustment) {
					mRightMargin = (int) Painter.getInstance().getCellPainter()
							.getCellSize() / 2;
				}
				if (mBottomMarginAdjustment) {
					mBottomMargin = (int) Painter.getInstance()
							.getCellPainter().getCellSize() / 2;
				}
			}

			// Adjust the margins
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
			layoutParams.setMargins(mLeftMargin, mTopMargin, mRightMargin,
					mBottomMargin);
			setLayoutParams(layoutParams);
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
