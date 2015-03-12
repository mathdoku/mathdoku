package net.mathdoku.plus.painter;

import android.graphics.Paint;

import net.mathdoku.plus.painter.Painter.DigitPainterMode;
import net.mathdoku.plus.painter.Painter.GridTheme;

public class EnteredValuePainter extends DigitPainter {

    /**
     * Creates a new instance of {@link EnteredValuePainter}.
     *
     * @param painter
     *         The global container for all painters.
     */
    public EnteredValuePainter(Painter painter) {
        super(painter);
        mTextPaintNormalInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaintMaybeInputMode = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    public void setTheme(GridTheme theme) {
        mTextPaintNormalInputMode.setColor(mPainter.getHighlightedTextColorNormalInputMode());
        mTextPaintMaybeInputMode.setColor(mPainter.getDefaultTextColor());

        mTextPaintNormalInputMode.setTypeface(mPainter.getTypeface());

        mTextPaintMaybeInputMode.setTypeface(mPainter.getTypeface());

    }

    @Override
    protected void setCellSize(float size) {
        // 1/3 of cell is used for the cage results. Remaining space can be sued
        // to display the user value.
        int bottomMargin = (int) (size * 0.1);
        int enteredValueTextSize = (int) (size * 2 / 3);

        mTextPaintNormalInputMode.setTextSize(enteredValueTextSize);
        mTextPaintMaybeInputMode.setTextSize(enteredValueTextSize);

        // Compute the offsets at which the user value will be displayed within
        // the cell
        mLeftOffset = size / 2 - enteredValueTextSize / 4;

        mBottomOffset = size - bottomMargin;
    }

    @Override
    public Paint getTextPaintNormalInputMode() {
        return mDigitPainterMode == DigitPainterMode.INPUT_MODE_BASED ? mTextPaintNormalInputMode : mTextPaintMaybeInputMode;
    }
}
