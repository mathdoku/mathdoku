package net.mathdoku.plus.puzzle.cheat;

import android.content.res.Resources;

public class CheatParameters {
	@SuppressWarnings("unused")
	private static final String TAG = CheatParameters.class.getName();

	private Resources resources;

	private long penaltyTimeInSeconds;
	private int tipTitleResId;
	private int tipTextResId;
	private String tipName;

	public Resources getResources() {
		return resources;
	}

	public CheatParameters setResources(Resources resources) {
		this.resources = resources;
		return this;
	}

	public long getPenaltyTimeInSeconds() {
		return penaltyTimeInSeconds;
	}

	public CheatParameters setPenaltyTimeInSeconds(long penaltyTimeInSeconds) {
		this.penaltyTimeInSeconds = penaltyTimeInSeconds;
		return this;
	}

	public int getTipTitleResId() {
		return tipTitleResId;
	}

	public CheatParameters setTipTitleResId(int tipTitleResId) {
		this.tipTitleResId = tipTitleResId;
		return this;
	}

	public int getTipTextResId() {
		return tipTextResId;
	}

	public CheatParameters setTipTextResId(int tipTextResId) {
		this.tipTextResId = tipTextResId;
		return this;
	}

	public String getTipName() {
		return tipName;
	}

	public CheatParameters setTipName(String tipName) {
		this.tipName = tipName;
		return this;
	}
}
