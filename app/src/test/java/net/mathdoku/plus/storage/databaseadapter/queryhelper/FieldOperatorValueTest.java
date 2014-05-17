package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FieldOperatorValueTest {
	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorNotEqualToBetween_FieldIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue(null, FieldOperatorValue.Operator.EQUALS,
				"VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorNotEqualToBetween_FieldIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("", FieldOperatorValue.Operator.EQUALS, "VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorNotEqualToBetween_OperatorIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("FIELD", (FieldOperatorValue.Operator) null,
				"VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorNotEqualToBetween_ValueIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("FIELD", FieldOperatorValue.Operator.EQUALS,
				null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorNotEqualToBetween_ValueIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("FIELD", FieldOperatorValue.Operator.EQUALS, "");
	}

	@Test
	public void constructorOperatorNotEqualToBetween_ValueIsNotNullOrEmpty_Success()
			throws Exception {
		assertThat(new FieldOperatorValue("FIELD",
				FieldOperatorValue.Operator.EQUALS, "VALUE").toString(),
				is("`FIELD` = VALUE"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorBetween_FieldIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue(null, "LOW_VALUE", "HIGH_VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorBetween_FieldIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("", "LOW_VALUE", "HIGH_VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorBetween_LowValueIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("FIELD", (String) null, "HIGH_VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorBetween_LowValueIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("FIELD", "", "HIGH_VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorBetween_HighValueIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("FIELD", "LOW_VALUE", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructorOperatorBetween_HighValueIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new FieldOperatorValue("FIELD", "LOW_VALUE", "");
	}

	@Test
	public void constructorOperatorBetween_FieldAndValuesNotNullOrEmpty_Success()
			throws Exception {
		assertThat(
				new FieldOperatorValue("FIELD", "LOW_VALUE", "HIGH_VALUE")
						.toString(),
				is("`FIELD` BETWEEN LOW_VALUE AND HIGH_VALUE"));
	}
}
