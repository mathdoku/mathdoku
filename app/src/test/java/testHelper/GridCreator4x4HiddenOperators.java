package testHelper;

public class GridCreator4x4HiddenOperators extends GridCreator4x4 {
	private int mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator = 5;

	public static GridCreator4x4HiddenOperators create() {
		return new GridCreator4x4HiddenOperators();
	}

	public static GridCreator4x4HiddenOperators createEmpty() {
		GridCreator4x4HiddenOperators gridCreator = new GridCreator4x4HiddenOperators();
		gridCreator.setEmptyGrid();
		return gridCreator;
	}

	public static GridCreator4x4HiddenOperators createEmptyWithSelectedCell(
			int idSelectedCell) {
		GridCreator4x4HiddenOperators gridCreator = new GridCreator4x4HiddenOperators();
		gridCreator.setSelectedCell(idSelectedCell);
		gridCreator.setEmptyGrid();
		return gridCreator;
	}

	protected GridCreator4x4HiddenOperators() {
		super(true);
	}

	@Override
	protected long getGameSeed() {
		return 2365799532943725794L;
	}

	public int getIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator() {
		return mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator;
	}
}
