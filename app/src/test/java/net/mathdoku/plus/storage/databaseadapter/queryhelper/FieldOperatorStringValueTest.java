package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FieldOperatorStringValueTest {
	@Test
	public void constructorOperatorNotEqualToBetween_ValueIsNull_Success()
			throws Exception {
		assertThat(new FieldOperatorStringValue("FIELD",
				FieldOperatorValue.Operator.EQUALS, null).toString(),
				is("`FIELD` = null"));
	}

	@Test
	public void createFieldOperatorStringValue_ValueIsEmpty_Success()
			throws Exception {
		assertThat(new FieldOperatorStringValue("FIELD",
				FieldOperatorValue.Operator.EQUALS, "").toString(),
				is("`FIELD` = ''"));
	}

	@Test
	public void createFieldOperatorStringValue_ValueIsNotNullOrEmpty_Success()
			throws Exception {
		assertThat(new FieldOperatorStringValue("FIELD",
				FieldOperatorValue.Operator.EQUALS, "VALUE").toString(),
				is("`FIELD` = 'VALUE'"));
	}
}
