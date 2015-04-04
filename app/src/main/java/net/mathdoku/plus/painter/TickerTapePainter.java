package net.mathdoku.plus.painter;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class TickerTapePainter extends TextPainter {
    @Override
    public void setTheme(Theme theme) {
        setTextColor(theme.getTickerTaperTextColor());
        setBackgroundColor(theme.getTickerTapeBackgroundColor());
    }
}