package net.mathdoku.plus.gridgenerating.cageresult;

public class NullCageResult extends CageResult {
	private static NullCageResult singletonNullCageResult = null;

	private NullCageResult() {
		super();
	}

	public static NullCageResult create() {
		if (singletonNullCageResult == null) {
			singletonNullCageResult = new NullCageResult();
		}
		return singletonNullCageResult;
	}

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public int getResult() {
		return 0;
	}
}
