package net.mathdoku.plus.tip;

import android.content.Context;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.puzzle.cage.Cage;
import net.mathdoku.plus.puzzle.cell.Cell;
import net.mathdoku.plus.puzzle.grid.Grid;

public class TipCopyCellValues extends TipDialog {
    private static final String TIP_NAME = "CopyCellValues";
    private static final TipPriority TIP_PRIORITY = TipPriority.LOW;

    /**
     * Creates a new tip dialog which explains that cell values can be copied from one cell to another cell.
     * <p/>
     * For performance reasons this method should only be called in case the static call to method {@link
     * #toBeDisplayed} returned true.
     *
     * @param context
     *         The activity in which this tip has to be shown.
     */
    public TipCopyCellValues(Context context) {
        super(context, TIP_NAME, TIP_PRIORITY);

        build(R.drawable.lightbulb, context.getResources()
                .getString(R.string.dialog_tip_copy_cell_values_title), context.getResources()
                      .getString(R.string.dialog_tip_copy_cell_values_text), null);
    }

    /**
     * Checks whether this tip has to be displayed. Should be called statically before creating this object.
     *
     * @param preferences
     *         Preferences of the activity for which has to be checked whether this tip should be shown.
     * @param grid
     *         The grid which will be checked if now is the appropriate time to display the tip.
     * @return True in case the tip might be displayed. False otherwise.
     */
    public static boolean toBeDisplayed(Preferences preferences, Grid grid) {
        // Do not display in case the copy function has been used
        if (preferences.getInputModeCopyCounter() > 0) {
            return false;
        }

        if (doNotDisplayBasedOnGrid(grid)) {
            return false;
        }

        // Do not display in case it was displayed less than 2 hours ago.
        if (preferences.getTipLastDisplayTime(TIP_NAME) > System.currentTimeMillis() - 2 * 60 * 60 * 1000) {
            return false;
        }

        // Determine on basis of preferences whether the tip should be shown.
        return TipDialog.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
    }

    private static boolean doNotDisplayBasedOnGrid(Grid grid) {
        if (grid == null) {
            return true;
        }

        Cell cell = grid.getSelectedCell();
        if (cell == null) {
            return true;
        }

        // Only display in case the cell contains at least 3 maybe values. This
        // is not strictly necessary for copying but the tip will make more
        // sense when a cell contains multiple values.
        if (cell.countPossibles() < 3) {
            return true;
        }

        Cage cage = grid.getCage(cell);
        if (doNotDisplayBasedOnCage(cage)) {
            return true;
        }

        return false;
    }

    private static boolean doNotDisplayBasedOnCage(Cage cage) {
        // Ensure that the cage has multiple cells of which at least one cell is
        // still empty. This is not strictly necessary for copying but the tip
        // will make more sense in case the cell values can be copied to another
        // cell in the same cage.
        return cage == null || cage.getNumberOfCells() <= 1 || !cage.hasEmptyCells();
    }
}
