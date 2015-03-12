package net.mathdoku.plus.puzzle.cheat;

import android.content.Context;

import net.mathdoku.plus.R;

public class CheckProgressUsedCheat extends Cheat {
    private static final String TIP_NAME = "Cheat.CheckProgress";
    private static final int PENALTY_TIME_IN_SECONDS = 20;
    private static final int PENALTY_TIME_PER_INVALID_ENTRY_IN_SECONDS = 15;
    private final long invalidEntriesPenaltyTimeInMilliseconds;

    public CheckProgressUsedCheat(Context context, int countInvalidEntries) {
        super(createCheatParameters(context));
        invalidEntriesPenaltyTimeInMilliseconds = countInvalidEntries *
                PENALTY_TIME_PER_INVALID_ENTRY_IN_SECONDS * MILLISECONDS_PER_SECOND;
    }

    private static CheatParameters createCheatParameters(Context context) {
        return new CheatParameters().setResources(context.getResources())
                .setPenaltyTimeInSeconds(PENALTY_TIME_IN_SECONDS)
                .setTipName(TIP_NAME)
                .setTipTitleResId(R.string.dialog_tip_cheat_check_progress_title)
                .setTipTextResId(R.string.dialog_tip_cheat_check_progress_text);
    }

    @Override
    protected String createTipText(CheatParameters cheatParameters) {
        return cheatParameters.getResources()
                .getString(cheatParameters.getTipTextResId(), super.getPenaltyTimeInMilliseconds(),
                           getPenaltyTimeText(invalidEntriesPenaltyTimeInMilliseconds));
    }

    @Override
    public long getPenaltyTimeInMilliseconds() {
        return super.getPenaltyTimeInMilliseconds() + invalidEntriesPenaltyTimeInMilliseconds;
    }
}
