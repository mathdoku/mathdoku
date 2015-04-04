package net.mathdoku.plus.painter;

import android.graphics.Typeface;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class SolvedTextPainter extends TextPainter {
    private Typeface typeface;

    @Override
    public void setTheme(Theme theme) {
        typeface = theme.getTypeface();
        setTextColor(theme.getSolvedAnimationTextColor());
        setBackgroundColor(theme.getSolvedAnimationBackgroundColor());
    }

    public Typeface getTypeface() {
        return typeface;
    }
}