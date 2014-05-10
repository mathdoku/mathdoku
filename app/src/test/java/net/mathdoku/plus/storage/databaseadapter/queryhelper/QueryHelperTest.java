package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricGradleTestRunner.class)
public class QueryHelperTest {
	@Test(expected = IllegalArgumentException.class)
	public void getFieldOperatorValue_FieldIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		QueryHelper.getFieldOperatorValue(null, "OPERATOR", "VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFieldOperatorValue_FieldIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		QueryHelper.getFieldOperatorValue("", "OPERATOR", "VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFieldOperatorValue_OperatorIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		QueryHelper.getFieldOperatorValue("FIELD", null, "VALUE");
	}

	@Test(expected = IllegalArgumentException.class)
	public void getFieldOperatorValue_OperatorIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		QueryHelper.getFieldOperatorValue("FIELD", "", "VALUE");
	}

	@Test
	public void getFieldOperatorValue_ValueIsNotNull_Success() throws Exception {
		assertThat(
				QueryHelper.getFieldOperatorValue("FIELD", "OPERATOR", "VALUE"),
				is("`FIELD` OPERATOR 'VALUE'"));
	}

	@Test
	public void getFieldOperatorValue_StringValueIsNull_Success()
			throws Exception {
		assertThat(
				QueryHelper.getFieldOperatorValue("FIELD", "OPERATOR", null),
				is("`FIELD` OPERATOR null"));
	}

	@Test
	public void getFieldOperatorValue_StringValueIsEmpty_Success()
			throws Exception {
		assertThat(QueryHelper.getFieldOperatorValue("FIELD", "OPERATOR", ""),
				is("`FIELD` OPERATOR ''"));
	}

	@Test
	public void getFieldEqualsValue_StringValueNotNullOrEmpty_Success()
			throws Exception {
		assertThat(QueryHelper.getFieldEqualsValue("FIELD", "VALUE"),
				is("`FIELD` = 'VALUE'"));
	}

	@Test
	public void getFieldEqualsValue_IntegerValue_Success() throws Exception {
		assertThat(QueryHelper.getFieldEqualsValue("FIELD", 23),
				is("`FIELD` = 23"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void joinStringsSeparatedWith_StringIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		QueryHelper.joinStringsSeparatedWith(null, "+");
	}

	@Test(expected = IllegalArgumentException.class)
	public void joinStringsSeparatedWith_StringIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		QueryHelper.joinStringsSeparatedWith(new ArrayList<String>(), "+");
	}

	@Test
	public void joinStringsSeparatedWith_1Operand_Success() throws Exception {
		assertThat(QueryHelper.joinStringsSeparatedWith(
				Arrays.asList("operand1"), "+"), is("operand1"));
	}

	@Test
	public void joinStringsSeparatedWith_2Operands_Success() throws Exception {
		assertThat(
				QueryHelper.joinStringsSeparatedWith(
						Arrays.asList("operand1", "operand2"), "+"),
				is("operand1 + operand2"));
	}

	@Test
	public void joinStringsSeparatedWith_3Operands_Success() throws Exception {
		assertThat(
				QueryHelper.joinStringsSeparatedWith(
						Arrays.asList("operand1", "operand2", "operand3"), "+"),
				is("operand1 + operand2 + operand3"));
	}
}
