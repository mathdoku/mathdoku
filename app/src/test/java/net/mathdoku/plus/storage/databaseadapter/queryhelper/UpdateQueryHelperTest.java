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
public class UpdateQueryHelperTest {
	private static final String TABLE_NAME = "TABLE";
	private UpdateQueryHelper updateQueryHelper;

	@Before
	public void setUp() {
		updateQueryHelper = new UpdateQueryHelper(TABLE_NAME);
	}

	@Test
	public void setColumnTo_TableWithSingleColumn_success() throws Exception {
		updateQueryHelper.setColumnToValue("COLUMN1", "VALUE1");
		assertThat(updateQueryHelper.toString(),
				is("UPDATE TABLE SET `COLUMN1` = 'VALUE1'"));
	}

	@Test
	public void setColumnTo_TableWithMultipleColumns_success() throws Exception {
		updateQueryHelper.setColumnToValue("COLUMN1", "VALUE1");
		updateQueryHelper.setColumnToValue("COLUMN2", "VALUE2");
		assertThat(
				updateQueryHelper.toString(),
				is("UPDATE TABLE SET `COLUMN1` = 'VALUE1', `COLUMN2` = 'VALUE2'"));
	}

	@Test
	public void setColumnTo_TableWithSingleColumnIsNull_success()
			throws Exception {
		updateQueryHelper.setColumnToValue("COLUMN1", null);
		assertThat(updateQueryHelper.toString(),
				is("UPDATE TABLE SET `COLUMN1` = null"));
	}

	@Test
	public void setColumnToStatement_TableWithSingleColumn_success()
			throws Exception {
		updateQueryHelper.setColumnToStatement("COLUMN1", "SQL-STATEMENT");
		assertThat(updateQueryHelper.toString(),
				   is("UPDATE TABLE SET `COLUMN1` = SQL-STATEMENT"));
	}


	@Test
	public void setColumnToNull_SingleColumn_Success() throws Exception {
		updateQueryHelper.setColumnToNull("COLUMN1");
		assertThat(updateQueryHelper.toString(),
				is("UPDATE TABLE SET `COLUMN1` = null"));
	}

	@Test
	public void setColumnToNull_MultipleColumn_Success() throws Exception {
		updateQueryHelper.setColumnToNull("COLUMN1");
		updateQueryHelper.setColumnToNull("COLUMN2");
		assertThat(updateQueryHelper.toString(),
				is("UPDATE TABLE SET `COLUMN1` = null, `COLUMN2` = null"));
	}

	@Test
	public void setWhereCondition_TableWithSingleColumnIsSQL_success()
			throws Exception {
		updateQueryHelper.setColumnToValue("COLUMN1", "VALUE");
		ConditionQueryHelper conditionQueryHelperMock = mock(ConditionQueryHelper.class);
		when(conditionQueryHelperMock.toString()).thenReturn("CONDITION");
		updateQueryHelper.setWhereCondition(conditionQueryHelperMock);
		assertThat(updateQueryHelper.toString(),
				   is("UPDATE TABLE SET `COLUMN1` = 'VALUE' WHERE CONDITION"));
	}


	@Test(expected = IllegalStateException.class)
	public void toString_NoColumns_ThrowsIllegalStateException()
			throws Exception {
		updateQueryHelper.toString();
	}
}
