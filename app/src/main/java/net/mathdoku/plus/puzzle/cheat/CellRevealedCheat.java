package net.mathdoku.plus.puzzle.cheat;

import android.content.Context;

import net.mathdoku.plus.R;

public class CellRevealedCheat extends Cheat {
	private static final String TIP_NAME = "Cheat.CellRevealed";
	public static final int PENALTY_TIME_IN_SECONDS = 60;

	public CellRevealedCheat(Context context) {
		super(createCheatParameters(context));
	}

	private static CheatParameters createCheatParameters(Context context) {
		return new CheatParameters()
				.setResources(context.getResources())
				.setPenaltyTimeInSeconds(PENALTY_TIME_IN_SECONDS)
				.setTipName(TIP_NAME)
				.setTipTitleResId(R.string.dialog_tip_cheat_reveal_value_title)
				.setTipTextResId(R.string.dialog_tip_cheat_reveal_value_text);
	}
}
