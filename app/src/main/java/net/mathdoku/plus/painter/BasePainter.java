package net.mathdoku.plus.painter;

import net.mathdoku.plus.painter.Painter.GridTheme;

abstract class BasePainter {
    final Painter mPainter;

    public BasePainter(Painter painter) {
        mPainter = painter;
    }

    public void setTheme(GridTheme theme) {
        // Override this method in case the painter should respond to a change of the theme.
    }

    protected void setCellSize(float size) {
        // Override this method in case the painter should respond to a change of the size of the cell.
    }
}
