package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FieldBetweenIntegerValuesTest {
	@Test
	public void constructorOperatorBetween_FieldAndValuesNotNullOrEmpty_Success()
			throws Exception {
		assertThat(new FieldBetweenIntegerValues("FIELD", 123, 789).toString(),
				is("`FIELD` BETWEEN 123 AND 789"));
	}
}
