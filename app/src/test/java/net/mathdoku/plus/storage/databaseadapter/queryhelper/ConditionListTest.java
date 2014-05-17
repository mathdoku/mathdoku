package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
public class ConditionListTest {
	ConditionList conditionList;

	@Before
	public void setUp() throws Exception {
		conditionList = new ConditionList();
	}

	@Test
	public void getFieldLessThanValue() throws Exception {
		assertThat(ConditionList.getFieldLessThanValue("FIELD", "VALUE"),
				is("`FIELD` < 'VALUE'"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addBetweenOperand_FieldIsNull_ThrowsIllegalStateException()
			throws Exception {
		new FieldBetweenIntegerValues(null, Integer.MIN_VALUE,
				Integer.MAX_VALUE);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addBetweenOperand_FieldIsEmpty_ThrowsIllegalStateException()
			throws Exception {
		new FieldBetweenIntegerValues("", Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	@Test
	public void addBetweenOperand_OperandsNotNullOrEmpty_ThrowsIllegalStateException()
			throws Exception {
		assertThat(new FieldBetweenIntegerValues("FIELD", 123, 456).toString(),
				is("`FIELD` BETWEEN 123 AND 456"));
	}

	@Test(expected = IllegalStateException.class)
	public void setAndOperator_NoOperandsSet_ThrowsIllegalStateException()
			throws Exception {
		conditionList.setAndOperator();
	}

	@Test(expected = IllegalStateException.class)
	public void setAndOperator_1OperandSet_ThrowsIllegalStateException()
			throws Exception {
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND1"));
		conditionList.setAndOperator();
	}

	private FieldOperatorValue getFieldOperatorValueMock(String operand) {
		FieldOperatorValue fieldOperatorValue = mock(FieldOperatorValue.class);
		when(fieldOperatorValue.toString()).thenReturn(operand);

		return fieldOperatorValue;
	}

	@Test
	public void setAndOperator_2OperandsSet_Success() throws Exception {
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND1"));
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND2"));
		conditionList.setAndOperator();
		assertThat(conditionList.toString(), is(" (OPERAND1 AND OPERAND2)"));
	}

	@Test
	public void setAndOperator_3OperandsSet_Success() throws Exception {
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND1"));
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND2"));
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND3"));
		conditionList.setAndOperator();
		assertThat(conditionList.toString(),
				is(" (OPERAND1 AND OPERAND2 AND OPERAND3)"));
	}

	@Test(expected = IllegalStateException.class)
	public void setOrOperator_NoOperandsSet_ThrowsIllegalStateException()
			throws Exception {
		conditionList.setOrOperator();
	}

	@Test(expected = IllegalStateException.class)
	public void setOrOperator_1OperandSet_ThrowsIllegalStateException()
			throws Exception {
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND1"));
		conditionList.setOrOperator();
	}

	@Test
	public void setOrOperator_2OperandsSet_Success() throws Exception {
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND1"));
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND2"));
		conditionList.setOrOperator();
		assertThat(conditionList.toString(), is(" (OPERAND1 OR OPERAND2)"));
	}

	@Test
	public void setOrOperator_3OperandsSet_Success() throws Exception {
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND1"));
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND2"));
		conditionList.addOperand(getFieldOperatorValueMock("OPERAND3"));
		conditionList.setOrOperator();
		assertThat(conditionList.toString(),
				is(" (OPERAND1 OR OPERAND2 OR OPERAND3)"));
	}

	@Test(expected = IllegalStateException.class)
	public void toString_NoOperandsSet_ThrowsIllegalStateException()
			throws Exception {
		conditionList.toString();
	}

	public void toString_1OperandSet_ThrowsIllegalStateException()
			throws Exception {
		assertThat(
				conditionList
						.addOperand(getFieldOperatorValueMock("OPERAND1"))
						.toString(), is("OPERAND1"));
	}
}
