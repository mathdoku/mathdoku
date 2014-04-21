package net.mathdoku.plus.gridgenerating.cageresult;

import net.mathdoku.plus.enums.CageOperator;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class CageResultTest {
	private final int value0 = 1;
	private final int value1 = 5;
	CageResult cageResult;

	@Before
	public void setup() {
		cageResult = CageResult.create(CageOperator.ADD, value0, value1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void create_NullValue_ThrowsIllegalArgumentException() throws Exception {
		CageResult.create(CageOperator.ADD, null);
	}

	@Test
	public void create_OperatorNoneWithCorrectNumberOfArguments_CageResultCreated() throws Exception {
		cageResult = CageResult.create(CageOperator.NONE, value0);
		assertThat((SingeCellCageResult) cageResult, is(not(nullValue(SingeCellCageResult.class))));
	}

	@Test
	public void create_OperatorAddWithCorrectNumberOfArguments_CageResultCreated() throws Exception {
		cageResult = CageResult.create(CageOperator.ADD, value0, value1);
		assertThat((AdditionCageResult) cageResult, is(not(nullValue(AdditionCageResult.class))));
	}

	@Test
	public void create_SubtractWithCorrectNumberOfArguments_CageResultCreated() throws Exception {
		cageResult = CageResult.create(CageOperator.SUBTRACT, value0, value1);
		assertThat((SubtractionCageResult) cageResult, is(not(nullValue(SubtractionCageResult.class))));
	}

	@Test
	public void create_MultiplyWithCorrectNumberOfArguments_CageResultCreated() throws Exception {
		cageResult = CageResult.create(CageOperator.MULTIPLY, value0, value1);
		assertThat((MultiplicationCageResult) cageResult, is(not(nullValue(MultiplicationCageResult.class))));
	}

	@Test
	public void create_DivideWithCorrectNumberOfArguments_CageResultCreated() throws Exception {
		cageResult = CageResult.create(CageOperator.DIVIDE, value0, value1);
		assertThat((DivisionCageResult) cageResult, is(not(nullValue(DivisionCageResult.class))));
	}

	@Test
	public void getCageOperator() throws Exception {
		assertThat(cageResult.getCageOperator(), is(CageOperator.ADD));
	}

	@Test
	public void getCellValue() throws Exception {
		assertThat(cageResult.getCellValue(0), is(value0));
		assertThat(cageResult.getCellValue(1), is(value1));
	}

	@Test
	public void getCellValues() throws Exception {
		assertThat(cageResult.getCellValues(), is(new int[] {value0, value1}));
	}

	@Test
	public void isNull_NormalCageResult_False() throws Exception {
		assertThat(cageResult.isNull(), is(false));
	}

	@Test
	public void isNull_NullCageResult_False() throws Exception {
		cageResult = NullCageResult.create();
		assertThat(cageResult.isNull(), is(true));
	}

	@Test
	public void isValid_NullCageResult_True() throws Exception {
		cageResult = NullCageResult.create();
		assertThat(cageResult.isValid(), is(false));
	}
}
