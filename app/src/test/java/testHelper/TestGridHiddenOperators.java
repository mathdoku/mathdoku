package testHelper;

public class TestGridHiddenOperators extends TestGrid {
	private int mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator = 2;

	TestGridHiddenOperators() {
		super(true);
	}

	public int getIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator() {
		return mIdOfUpperLeftCellOfCageWithAnUnrevealedCageOperator;
	}
}
