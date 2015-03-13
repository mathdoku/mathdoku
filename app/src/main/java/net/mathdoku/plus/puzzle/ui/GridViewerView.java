package net.mathdoku.plus.puzzle.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.painter.GridPainter;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.painter.Painter.DigitPainterMode;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.digitpositiongrid.DigitPositionGrid;
import net.mathdoku.plus.puzzle.grid.Grid;

public class GridViewerView extends View {
    @SuppressWarnings("unused")
    private static final String TAG = GridViewerView.class.getName();

    // Context and preferences in context
    Context mContext;
    Preferences mPreferences;

    // Actual content of the puzzle in this grid view
    Grid mGrid;

    // All cell drawers needed to draw the grid
    private CellDrawer[][] mCellDrawer;

    // Size (in cells and pixels) of the grid view and size (in pixel) of cells
    // in grid
    int mGridSize;
    private float mViewSize;
    float mBorderWidth;
    float mCellSize;

    // Reference to the global (grid) painter object
    private Painter mPainter;
    private GridPainter mGridPainter;

    // Current orientation of the device
    private int mOrientation;

    // Preferences used when drawing the grid
    private boolean mPrefShowDupeDigits;
    private boolean mPrefShowBadCageMaths;
    private boolean mPrefShowMaybesAs3x3Grid;

    // In case the grid viewer view is displayed in a scroll view and the device
    // is in landscape mode, it is necessary to restrict the size of the grid
    // viewer view explicitly. In all other case it can be correctly determined
    // based on the width and height of the device.
    private boolean mInScrollView;
    private float mMaxViewSize;

    // Flag which determine whether a swipe border should be reserved around the
    // visible grid.
    private boolean mSwipeBorder;

    @SuppressWarnings("WeakerAccess")
    public GridViewerView(Context context) {
        super(context);
        initGridView(context);
    }

    @SuppressWarnings("WeakerAccess")
    public GridViewerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initGridView(context);
    }

    @SuppressWarnings("WeakerAccess")
    public GridViewerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initGridView(context);
    }

    private void initGridView(Context context) {
        mContext = context;
        mPreferences = Preferences.getInstance(mContext);

        mViewSize = 0;
        mPainter = Painter.getInstance();
        mGridPainter = Painter.getInstance()
                .getGridPainter();

        // noinspection ConstantConditions,ConstantConditions
        mOrientation = getResources().getConfiguration().orientation;
        mInScrollView = false;
        mMaxViewSize = Float.MAX_VALUE;

        mSwipeBorder = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mGrid == null) {
            // As long as no grid has been attached to the grid view, it can not
            // be drawn.
            return;
        }

        synchronized (mGrid.getLock()) {
            onDrawLocked(canvas);
        }
    }

    /**
     * Actually draw the locked. This method should always be encapsulated within a synchronize block</p> <code>
     * synchronized (mGrid.mLock) { onDrawLocked(canvas); // other stuff } </code>
     *
     * @param canvas
     *         The canvas on which should be drawn.
     */
    void onDrawLocked(Canvas canvas) {
        // Draw outer grid border. For support of transparent borders it has
        // to be avoided that lines do overlap.
        Paint borderPaint = mGridPainter.getBorderPaint();
        canvas.drawRect(0, 0, mViewSize - mBorderWidth, mBorderWidth, borderPaint);
        canvas.drawRect(mViewSize - mBorderWidth, 0, mViewSize, mViewSize - mBorderWidth, borderPaint);
        canvas.drawRect(mBorderWidth, mViewSize - mBorderWidth, mViewSize, mViewSize, borderPaint);
        canvas.drawRect(0, mBorderWidth, 0 + mBorderWidth, mViewSize, borderPaint);

        // Draw background of the inner grid itself. This background is a
        // bit extended outside the inner grid which gives a nice outline
        // around the grid in the light theme.
        canvas.drawRect(mBorderWidth - 1, mBorderWidth - 1, mViewSize - mBorderWidth + 2, mViewSize - mBorderWidth + 2,
                        mGridPainter.getBackgroundPaint());

        // Update the current cell size
        mPainter.setCellSize(mCellSize);

        // Draw cells
        GridInputMode gridInputMode = getRestrictedGridInputMode();
        for (int row = 0; row < mGridSize; row++) {
            for (int column = 0; column < mGridSize; column++) {
                mCellDrawer[row][column].draw(canvas, mBorderWidth, gridInputMode, 0);
            }
        }
    }

    /**
     * Get the current grid input mode.
     *
     * @return The current grid input mode.
     */
    GridInputMode getRestrictedGridInputMode() {
        return GridInputMode.NORMAL;
    }

    public void loadNewGrid(Grid grid) {
        mGrid = grid;

        // Compute grid size. Set to 1 in case grid is null to avoid problems in
        // onMeasure as this will be called before the grid is loaded.
        mGridSize = mGrid == null ? 1 : mGrid.getGridSize();

        // Build the matrix of cell drawers
        mCellDrawer = new CellDrawer[mGridSize][mGridSize];
        Cell[][] cells = new Cell[mGridSize][mGridSize];
        for (int row = 0; row < mGridSize; row++) {
            for (int column = 0; column < mGridSize; column++) {
                cells[row][column] = mGrid.getCellAt(row, column);
                mCellDrawer[row][column] = new CellDrawer(this, cells[row][column]);
            }
        }
        // For each cell drawer set the adjacent cell drawers
        CellDrawer mCellDrawerAbove;
        CellDrawer mCellDrawerToRight;
        CellDrawer mCellDrawerBelow;
        CellDrawer mCellDrawerToLeft;
        for (int row = 0; row < mGridSize; row++) {
            for (int column = 0; column < mGridSize; column++) {
                if (row > 0) {
                    mCellDrawer[row][column].setReferencesToCellAbove(cells[row - 1][column],
                                                                      mCellDrawer[row - 1][column]);
                }
                if (column + 1 < mGridSize) {
                    mCellDrawer[row][column].setReferencesToCellToRight(cells[row][column + 1],
                                                                        mCellDrawer[row][column + 1]);
                }
                if (row + 1 < mGridSize) {
                    mCellDrawer[row][column].setReferencesToCellBelow(cells[row + 1][column],
                                                                      mCellDrawer[row + 1][column]);
                }
                if (column > 0) {
                    mCellDrawer[row][column].setReferencesToCellToLeft(cells[row][column - 1],
                                                                       mCellDrawer[row][column - 1]);
                }
            }
        }

        setDigitPositionGrid();

        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get maximum width and height available to display the grid view.
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        // Get the maximum space available for the grid. As it is a square we
        // need the minimum of width and height.
        int maxSize = Math.min(measuredWidth, measuredHeight);

        // In case a swipe border has to be measured, all cells in the visible
        // grid will be reduced in size so that the swipe border can be drawn
        // around the grid.
        if (mSwipeBorder) {
            // As the size of the swipe border is 50% of a normal cell and the
            // border is displayed at all sides of the grid the cell size is
            // calculated as if a grid with size (size + 1) would be drawn
            // without a swipe border.
            mCellSize = (float) Math.floor(maxSize / (mGridSize + 1));

            // The grid border width equals 50% of a normal cell
            mBorderWidth = mCellSize / 2;
        } else {
            // Force to compute the cell size based on the maximum width
            // available for the entire grid without a swipe border.
            mBorderWidth = -1;
        }

        // The grid view border has to be set to a minimal width which is big
        // enough to catch a swipe motion event. This is needed to be able to
        // end a swipe motion for cells at the outer edge of the grid. In case
        // the border width was already computed to display the swipe border,
        // but is less than the minimal width, both the border width as cell
        // size has to be recomputed as well.
        float minGridBorderWidth = Math.max(mGridPainter.getBorderPaint()
                                                    .getStrokeWidth(), mSwipeBorder ? 15 : 0);
        if (mBorderWidth < minGridBorderWidth) {
            mBorderWidth = minGridBorderWidth;
            mCellSize = (float) Math.floor((maxSize - 2 * mBorderWidth) / mGridSize);
        }

        Painter.getInstance()
                .setCellSize(mCellSize);

        // Finally compute the total size of the grid
        mViewSize = 2 * mBorderWidth + mGridSize * mCellSize;

        // Last but not least restrict the size of the grid to the given
        // maximum in case the view is displayed inside a scroll view.
        if (mInScrollView && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mViewSize = Math.min(mViewSize, mMaxViewSize);
        }

        setMeasuredDimension((int) mViewSize, (int) mViewSize);
    }

    private int measure(int measureSpec) {
        return MeasureSpec.getSize(measureSpec);
    }

    /**
     * Sets the {@link net.mathdoku.plus.puzzle.digitpositiongrid.DigitPositionGrid} used to position the digit buttons
     * for reuse when drawing the maybe values.
     */
    private void setDigitPositionGrid() {
        // Determine the layout which has to be used for drawing the possible
        // values inside a cell.
        DigitPositionGrid mDigitPositionGrid = mGrid != null ? new DigitPositionGrid(mGrid.getGridSize()) : null;

        // Propagate the digit position grid to the maybe painter which is used
        // by the cell drawer to paint the maybe digits inside a cell.
        mPainter.getMaybeGridPainter()
                .setDigitPositionGrid(mDigitPositionGrid);

        // Propagate the digit position grid to the cell drawers
        if (mCellDrawer != null) {
            for (int row = 0; row < mGridSize; row++) {
                for (int column = 0; column < mGridSize; column++) {
                    mCellDrawer[row][column].setDigitPositionGrid(mDigitPositionGrid);
                }
            }
        }
    }

    /**
     * Get the orientation of the device.
     */
    int getOrientation() {
        return mOrientation;
    }

    /**
     * Get the grid which is displayed in the grid viewer view.
     *
     * @return The grid which is displayed in the grid viewer view.
     */
    Grid getGrid() {
        return mGrid;
    }

    /**
     * Set the maximum size of the grid viewer view. Should only be called in case the the grid viewer view is displayed
     * in a scroll view while the device is in landscape mode.
     *
     * @param maxSize
     *         The maximum size (width and height) to be used for the grid view.
     */
    public void setMaximumWidth(float maxSize) {
        mMaxViewSize = maxSize;
    }

    /**
     * Indicates whether this grid viewer view is displayed inside a scroll view.
     *
     * @param inScrollView
     *         True in case the grid viewer view is displayed inside a scroll view. False otherwise.
     */
    @SuppressWarnings("SameParameterValue")
    public void setInScrollView(boolean inScrollView) {
        mInScrollView = inScrollView;
    }

    /**
     * Enables/disables an additional border around the visible grid which can be use for handling the swipe events.
     *
     * @param swipeBorder
     *         True in case an additional swipe border has to be measured by the viewer. False otherwise.
     */
    void setSwipeBorder(boolean swipeBorder) {
        mSwipeBorder = swipeBorder;
    }

    /**
     * Set preferences which are used for drawing the grid.
     */
    public void setPreferences() {
        Preferences preferences = Preferences.getInstance();
        mPrefShowDupeDigits = preferences.isDuplicateDigitHighlightVisible();
        mPrefShowMaybesAs3x3Grid = preferences.isMaybesDisplayedInGrid();
        mPrefShowBadCageMaths = preferences.isBadCageMathHighlightVisible();

        // Update the painter which is used by the cell drawer when drawing the
        // cell.
        mPainter.setColorMode(
                mPreferences.isColoredDigitsVisible() ? DigitPainterMode.INPUT_MODE_BASED : DigitPainterMode
                        .MONOCHROME);

        // Reset borders of cells as they are affected by the preferences.
        mGrid.invalidateBordersOfAllCells();
    }

    public boolean hasPrefShowDupeDigits() {
        return mPrefShowDupeDigits;
    }

    public boolean hasPrefShowBadCageMaths() {
        return mPrefShowBadCageMaths;
    }

    public boolean hasPrefShowMaybesAs3x3Grid() {
        return mPrefShowMaybesAs3x3Grid;
    }

    public CellDrawer getCellDrawer(int row, int column) {
        if (mCellDrawer == null) {
            return null;
        }
        if (row < 0 || row >= mGridSize) {
            return null;
        }
        if (column < 0 || column >= mGridSize) {
            return null;
        }
        return mCellDrawer[row][column];
    }
}
