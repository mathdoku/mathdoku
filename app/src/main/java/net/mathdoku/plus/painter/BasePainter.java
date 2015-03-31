package net.mathdoku.plus.painter;

import net.mathdoku.plus.puzzle.ui.theme.Theme;

abstract class BasePainter {
    public void setTheme(Theme theme) {
        // Override this method in case the painter should respond to a change of the theme.
    }

    protected void setCellSize(float size) {
        // Override this method in case the painter should respond to a change of the size of the cell.
    }
}
