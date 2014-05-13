package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class ConditionQueryHelperTest {
	ConditionQueryHelper conditionQueryHelper;

	@Before
	public void setUp() throws Exception {
		conditionQueryHelper = new ConditionQueryHelper();
	}

	@Test
	public void getFieldLessThanValue() throws Exception {
		assertThat(
				ConditionQueryHelper.getFieldLessThanValue("FIELD", "VALUE"),
				is("`FIELD` < 'VALUE'"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addBetweenOperand_FieldIsNull_ThrowsIllegalStateException()
			throws Exception {
		ConditionQueryHelper.getFieldBetweenValues(null, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addBetweenOperand_FieldIsEmpty_ThrowsIllegalStateException()
			throws Exception {
		ConditionQueryHelper.getFieldBetweenValues("", Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Test
	public void addBetweenOperand_OperandsNotNullOrEmpty_ThrowsIllegalStateException()
			throws Exception {
		assertThat(
				ConditionQueryHelper
						.getFieldBetweenValues("FIELD", 123, 456)
						.toString(), is("FIELD BETWEEN 123 AND 456"));
	}

	@Test(expected = IllegalStateException.class)
	public void setAndOperator_NoOperandsSet_ThrowsIllegalStateException()
			throws Exception {
		conditionQueryHelper.setAndOperator();
	}

	@Test(expected = IllegalStateException.class)
	public void setAndOperator_1OperandSet_ThrowsIllegalStateException()
			throws Exception {
		conditionQueryHelper.addOperand("OPERAND1");
		conditionQueryHelper.setAndOperator();
	}

	@Test
	public void setAndOperator_2OperandsSet_Success() throws Exception {
		conditionQueryHelper.addOperand("OPERAND1");
		conditionQueryHelper.addOperand("OPERAND2");
		conditionQueryHelper.setAndOperator();
		assertThat(conditionQueryHelper.toString(),
				is(" (OPERAND1 AND OPERAND2)"));
	}

	@Test
	public void setAndOperator_3OperandsSet_Success() throws Exception {
		conditionQueryHelper.addOperand("OPERAND1");
		conditionQueryHelper.addOperand("OPERAND2");
		conditionQueryHelper.addOperand("OPERAND3");
		conditionQueryHelper.setAndOperator();
		assertThat(conditionQueryHelper.toString(),
				is(" (OPERAND1 AND OPERAND2 AND OPERAND3)"));
	}

	@Test(expected = IllegalStateException.class)
	public void setOrOperator_NoOperandsSet_ThrowsIllegalStateException()
			throws Exception {
		conditionQueryHelper.setOrOperator();
	}

	@Test(expected = IllegalStateException.class)
	public void setOrOperator_1OperandSet_ThrowsIllegalStateException()
			throws Exception {
		conditionQueryHelper.addOperand("OPERAND1");
		conditionQueryHelper.setOrOperator();
	}

	@Test
	public void setOrOperator_2OperandsSet_Success() throws Exception {
		conditionQueryHelper.addOperand("OPERAND1");
		conditionQueryHelper.addOperand("OPERAND2");
		conditionQueryHelper.setOrOperator();
		assertThat(conditionQueryHelper.toString(),
				is(" (OPERAND1 OR OPERAND2)"));
	}

	@Test
	public void setOrOperator_3OperandsSet_Success() throws Exception {
		conditionQueryHelper.addOperand("OPERAND1");
		conditionQueryHelper.addOperand("OPERAND2");
		conditionQueryHelper.addOperand("OPERAND3");
		conditionQueryHelper.setOrOperator();
		assertThat(conditionQueryHelper.toString(),
				is(" (OPERAND1 OR OPERAND2 OR OPERAND3)"));
	}

	@Test(expected = IllegalStateException.class)
	public void toString_NoOperandsSet_ThrowsIllegalStateException()
			throws Exception {
		conditionQueryHelper.toString();
	}

	public void toString_1OperandSet_ThrowsIllegalStateException()
			throws Exception {
		assertThat(conditionQueryHelper.addOperand("OPERAND1").toString(),is("OPERAND1"));
	}
}
