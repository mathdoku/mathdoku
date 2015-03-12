package net.mathdoku.plus.tip;

import android.content.Context;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.enums.CageOperator;
import net.mathdoku.plus.puzzle.cage.Cage;

public class TipOrderOfValuesInCage extends TipDialog {

    private static final String TIP_NAME = "OrderOfValuesInCage";
    private static final TipPriority TIP_PRIORITY = TipPriority.LOW;

    /**
     * Creates a new tip dialog which explains that the order of values in the cell of the cage is
     * not relevant for solving the cage arithmetic. </br>
     * <p/>
     * For performance reasons this method should only be called in case the static call to method
     * {@link #toBeDisplayed} returned true.
     *
     * @param context
     *         The activity in which this tip has to be shown.
     */
    public TipOrderOfValuesInCage(Context context) {
        super(context, TIP_NAME, TIP_PRIORITY);

        build(R.drawable.lightbulb, context.getResources()
                      .getString(R.string.dialog_tip_order_of_values_in_cage_title),
              context.getResources()
                      .getString(R.string.dialog_tip_order_of_values_in_cage_text),
              context.getResources()
                      .getDrawable(R.drawable.tip_order_of_values_in_cage));
    }

    /**
     * Checks whether this tip has to be displayed. Should be called statically before creating this
     * object.
     *
     * @param preferences
     *         Preferences of the activity for which has to be checked whether this tip should be
     *         shown.
     * @param cage
     *         The cage for which it will be checked if it is appropriate to show this tip.
     * @return True in case the tip might be displayed. False otherwise.
     */
    public static boolean toBeDisplayed(Preferences preferences, Cage cage) {
        // No tip to be displayed for non existing cages
        if (cage == null) {
            return false;
        }

        CageOperator cageOperator = cage.getOperator();

        // No tip to be displayed for cages without operators
        if (cageOperator == CageOperator.NONE) {
            return false;
        }

        // No tip to be displayed in case operators are visible and values have
        // to be added or multiplied.
        if (isOrderOfValuesNotRelevant(cage, cageOperator)) {
            return false;
        }

        // Do not display in case it was displayed less than 2 minutes ago.
        if (preferences.getTipLastDisplayTime(
                TIP_NAME) > System.currentTimeMillis() - 5 * 60 * 1000) {
            return false;
        }

        // Determine on basis of preferences whether the tip should be shown.
        return TipDialog.getDisplayTipAgain(preferences, TIP_NAME, TIP_PRIORITY);
    }

    private static boolean isOrderOfValuesNotRelevant(Cage cage, CageOperator cageOperator) {
        return !cage.isOperatorHidden() && (cageOperator == CageOperator.ADD || cageOperator == CageOperator.MULTIPLY);
    }
}