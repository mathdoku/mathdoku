package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FieldOperatorIntegerValueTest {
	@Test
	public void createFieldOperatorIntegerValue() throws Exception {
		assertThat(new FieldOperatorIntegerValue("FIELD",
				FieldOperatorValue.Operator.EQUALS, 123).toString(),
				is("`FIELD` = 123"));
	}
}
