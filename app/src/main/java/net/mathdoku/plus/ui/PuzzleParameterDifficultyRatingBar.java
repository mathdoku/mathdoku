package net.mathdoku.plus.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RatingBar;

import net.mathdoku.plus.enums.PuzzleComplexity;

import org.jetbrains.annotations.NotNull;

public class PuzzleParameterDifficultyRatingBar extends RatingBar {
	private int mWidth, mHeight, mOldWidth, mOldHeight;

	public PuzzleParameterDifficultyRatingBar(Context context) {
		super(context);
		setEnabled(false);
	}

	public PuzzleParameterDifficultyRatingBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setEnabled(false);
	}

	public PuzzleParameterDifficultyRatingBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setEnabled(false);
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldWidth,
			int oldHeight) {
		super.onSizeChanged(height, width, oldHeight, oldWidth);
		mWidth = width;
		mHeight = height;
		mOldWidth = oldWidth;
		mOldHeight = oldHeight;
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec,
			int heightMeasureSpec) {
		// Because of rotation of the rating bar, the parameter are swapped when
		// passing to super.
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	protected void onDraw(@NotNull Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);
		super.onDraw(c);
	}

	@Override
	public boolean onTouchEvent(@NotNull MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:

			setSelected(true);
			setPressed(true);
			break;
		case MotionEvent.ACTION_MOVE:
			setProgress(getMax()
					- (int) (getMax() * event.getY() / getHeight()));
			onSizeChanged(getWidth(), getHeight(), 0, 0);

			break;
		case MotionEvent.ACTION_UP:
			setSelected(false);
			setPressed(false);
			break;

		case MotionEvent.ACTION_CANCEL:
			break;
		}
		return true;
	}

	@Override
	public synchronized void setProgress(int progress) {
		if (progress >= 0) {
			super.setProgress(progress);
		}
		else {
			super.setProgress(0);
		}
		onSizeChanged(mWidth, mHeight, mOldWidth, mOldHeight);

	}

	public void setNumStars(PuzzleComplexity puzzleComplexity) {
		switch (puzzleComplexity) {
			case RANDOM:
				// Note: puzzles will never be stored with this complexity.
				setNumStars(0);
				break;
			case VERY_EASY:
				setNumStars(1);
				break;
			case EASY:
				setNumStars(2);
				break;
			case NORMAL:
				setNumStars(3);
				break;
			case DIFFICULT:
				setNumStars(4);
				break;
			case VERY_DIFFICULT:
				setNumStars(5);
				break;
			default:
				throw new UnsupportedOperationException(
						"Unhandled puzzle complexity value");
		}
	}
}