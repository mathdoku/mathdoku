package net.mathdoku.plus.painter;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public abstract class TextPainter extends BasePainter {
    private int textColor;
    private int backgroundColor;

    public abstract void setTheme(Theme theme);

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
