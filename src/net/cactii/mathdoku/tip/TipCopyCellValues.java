package net.cactii.mathdoku.tip;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.grid.GridCage;
import net.cactii.mathdoku.grid.GridCell;
import android.content.Context;

public class TipCopyCellValues extends TipDialog {

	public static String TIP_NAME = "Tip.CopyCellValues.DisplayAgain";
	private static TipPriority TIP_PRIORITY = TipPriority.LOW;

	/**
	 * Creates a new tip dialog which explains that cell values can be copied
	 * from one cell to another cell.</br>
	 * 
	 * For performance reasons this method should only be called in case the
	 * static call to method {@link #toBeDisplayed} returned true.
	 * 
	 * @param context
	 *            The activity in which this tip has to be shown.
	 */
	public TipCopyCellValues(Context context) {
		super(context, TIP_NAME, TIP_PRIORITY);

		build(
				context.getResources().getString(
						R.string.dialog_tip_copy_cell_values_title),
				context.getResources().getString(
						R.string.dialog_tip_copy_cell_values_text),
				context.getResources().getDrawable(
						R.drawable.tip_copy_cell_values)).show();
	}

	/**
	 * Checks whether this tip has to be displayed. Should be called statically
	 * before creating this object.
	 * 
	 * @param preferences
	 *            Preferences of the activity for which has to be checked
	 *            whether this tip should be shown.
	 * @param cage
	 * @return
	 */
	public static boolean toBeDisplayed(Preferences preferences,
			GridCell gridCell) {
		// No tip to be displayed for non existing cell
		if (gridCell == null) {
			return false;
		}

		// Only display in case the cell contains at least 3 maybe values. This
		// is not strictly necessary for copying but the tip will make more
		// sense when a cell contains multiple values.
		if (gridCell.countPossibles() < 3) {
			return false;
		}

		// Ensure that the cell is contained in a multiple cell cage from which
		// at least one cell is still empty. This is not strictly necessary for
		// copying but the tip will make more sense in case the cell values can
		// be copied to another cell in the same cage.
		if (existsNonEmptyCellInCage(gridCell.getCage()) == false) {
			return false;
		}

		// Do not display in case it was displayed less than 2 hours ago.
		if (preferences.getTipLastDisplayTime(TIP_NAME) > System
				.currentTimeMillis() - (2 * 60 * 60 * 1000)) {
			return false;
		}

		// Determine on basis of preferences whether the tip should be shown.
		return TipDialog
				.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
	}

	/**
	 * Checks whether the cage contains an empty cell.
	 * 
	 * @param gridCage
	 *            The cage to be checked.
	 * @return True in case the cage contains an empty cell. False otherwise.
	 */
	private static boolean existsNonEmptyCellInCage(GridCage gridCage) {
		if (gridCage == null || gridCage.mCells == null) {
			return false;
		}
		for (GridCell cell : gridCage.mCells) {
			if (cell.isEmpty()) {
				return true;
			}
		}

		return false;
	}
}