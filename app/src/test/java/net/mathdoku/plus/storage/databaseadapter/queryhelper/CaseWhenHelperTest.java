package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CaseWhenHelperTest {
	private CaseWhenHelper caseWhenHelper;

	@Before
	public void setUp() {
		caseWhenHelper = new CaseWhenHelper();
	}

	@Test(expected = IllegalArgumentException.class)
	public void addOperand_WhenThenConditionIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		caseWhenHelper.addOperand(null, "VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void addOperand_WhenThenValueIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		caseWhenHelper.addOperand(mock(FieldOperatorValue.class), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void addOperand_WhenThenValueIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		caseWhenHelper.addOperand(mock(FieldOperatorValue.class), "");
	}

	@Test
	public void addOperand_SingleWhenThen_success() throws Exception {
		caseWhenHelper.addOperand(getFieldOperatorValueMock("CONDITION"),
				"VALUE");

		assertThat(caseWhenHelper.toString(),
				is(" CASE WHEN CONDITION THEN 'VALUE' END"));
	}

	private FieldOperatorValue getFieldOperatorValueMock(String condition) {
		FieldOperatorValue fieldOperatorValueMock = mock(FieldOperatorValue.class);
		when(fieldOperatorValueMock.toString()).thenReturn(condition);

		return fieldOperatorValueMock;
	}

	@Test
	public void addOperand_MultipleWhenThen_success() throws Exception {
		caseWhenHelper.addOperand(getFieldOperatorValueMock("CONDITION1"),
				"VALUE1");
		caseWhenHelper.addOperand(getFieldOperatorValueMock("CONDITION2"),
				"VALUE2");

		assertThat(
				caseWhenHelper.toString(),
				is(" CASE WHEN CONDITION1 THEN 'VALUE1' WHEN CONDITION2 THEN 'VALUE2' END"));
	}

	@Test
	public void setElse_SingleWhenThen_success() throws Exception {
		caseWhenHelper.addOperand(getFieldOperatorValueMock("CONDITION"),
				"VALUE");
		caseWhenHelper.setElseStringValue("OTHER-VALUE");

		assertThat(caseWhenHelper.toString(),
				is(" CASE WHEN CONDITION THEN 'VALUE' ELSE 'OTHER-VALUE' END"));
	}

	@Test(expected = IllegalStateException.class)
	public void toString_NoOperandSet() throws Exception {
		caseWhenHelper.toString();
	}
}
