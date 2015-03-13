package net.mathdoku.plus.painter;

import net.mathdoku.plus.painter.Painter.GridTheme;

abstract class BasePainter {
    final Painter mPainter;

    public BasePainter(Painter painter) {
        mPainter = painter;
    }

    // Override this method in case the painter should respond to a change of the theme.
    public void setTheme(GridTheme theme) {}

    // Override this method in case the painter should respond to a change of the size of the cell.
    protected void setCellSize(float size) {}
}
