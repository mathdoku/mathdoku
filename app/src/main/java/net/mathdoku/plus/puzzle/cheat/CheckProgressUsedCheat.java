package net.mathdoku.plus.puzzle.cheat;

import android.content.Context;

public class CheckProgressUsedCheat extends Cheat {
	public CheckProgressUsedCheat(Context context, int occurrencesConditionalPenalty) {
		super(context, CheatType.CHECK_PROGRESS_USED, occurrencesConditionalPenalty);
	}
}
