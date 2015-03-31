package net.mathdoku.plus.puzzle.ui.theme;

public abstract class Theme {
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

}
