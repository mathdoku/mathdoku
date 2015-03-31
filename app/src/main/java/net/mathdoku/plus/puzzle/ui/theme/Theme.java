package net.mathdoku.plus.puzzle.ui.theme;

import android.graphics.DashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Typeface;

public abstract class Theme {
    private final static Typeface typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private final PathEffect pathEffect = new DashPathEffect(new float[]{2, 2}, 0);

    public abstract int getNameResId();

    public final int getHighlightedTextColorNormalInputMode() {
        // Colors of highlighted digits should be equal for all themes as the input_mode-images are currently only
        // available in this color set.
        return 0xFF0C97FA;
    }

    public final int getHighlightedTextColorMaybeInputMode() {
        // Colors of highlighted digits should be equal for all themes as the input_mode-images are currently only
        // available in this color set.
        return 0xFFFF8A00;
    }

    public abstract int getGridBackgroundColor();
    public abstract int getDefaultTextColor();
    public abstract int getDefaultCageBorderColor();
    public abstract int getBadMathCageBorderColor();
    public abstract int getSelectedCageBorderColor();
    public abstract int getSelectedBadMathCageBorderColor();
    public abstract int getInnerCageCellBorderColor();
    public abstract int getSelectedCellBorderColor();
    public abstract int getSelectedCellBackgroundColor();
    public abstract boolean getBorderAntiAlias();

    public abstract int getNormalInputModeMonochromeResId();
    public abstract int getMaybeInputModeMonochromeResId();

    public Typeface getTypeface() {
        return typeface;
    }

    public PathEffect getPathEffect() {
        return pathEffect;
    }

    public int getActionBarTextColor() {
        return 0xFFFFFFFF;
    }

    public int getActionBarBackgroundColor() {
        return 0xFF33B5E5;
    }

    public int getNavigationDrawerBackgroundColor() {
        return 0xFF222222;
    }
}
