package net.mathdoku.plus.painter;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

public class PagerTabStripPainter extends TextPainter {
    @Override
    public void setTheme(Theme theme) {
        setTextColor(theme.getActionBarTextColor());
        setBackgroundColor(theme.getActionBarBackgroundColor());
    }
}