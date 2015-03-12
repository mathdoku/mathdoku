package net.mathdoku.plus.puzzle.cheat;

import android.content.Context;

import net.mathdoku.plus.R;

public class OperatorRevealedCheat extends Cheat {
	private static final String TIP_NAME = "Cheat.OperatorRevealed";
	public static final int PENALTY_TIME_IN_SECONDS = 30;

	public OperatorRevealedCheat(Context context) {
		super(createCheatParameters(context));
	}

	private static CheatParameters createCheatParameters(Context context) {
		return new CheatParameters()
				.setResources(context.getResources())
				.setPenaltyTimeInSeconds(PENALTY_TIME_IN_SECONDS)
				.setTipName(TIP_NAME)
				.setTipTitleResId(R.string.dialog_tip_cheat_reveal_operator_title)
				.setTipTextResId(R.string.dialog_tip_cheat_reveal_operator_text);
	}
}
