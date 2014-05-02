package net.mathdoku.plus.storage.databaseadapter.database;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DatabaseColumnDefinitionTest {
	private DatabaseColumnDefinition databaseColumnDefinition;
	private static final String COLUMN_NAME = "*** TEST COLUMN ***";
	private static final DataType COLUMN_DATA_TYPE = DataType.STRING;
	private StringBuilder expectedColumnDefinitionStringBuilder;

	@Before
	public void setup() {
		databaseColumnDefinition = new DatabaseColumnDefinition(COLUMN_NAME,
				COLUMN_DATA_TYPE);
		expectedColumnDefinitionStringBuilder = new StringBuilder();
		expectedColumnDefinitionStringBuilder.append("`");
		expectedColumnDefinitionStringBuilder.append(COLUMN_NAME);
		expectedColumnDefinitionStringBuilder.append("` ");
		expectedColumnDefinitionStringBuilder.append(COLUMN_DATA_TYPE
				.getSqliteDataType());
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_ColumnNameIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		databaseColumnDefinition = new DatabaseColumnDefinition(null,
				DataType.STRING);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_ColumnNameIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		databaseColumnDefinition = new DatabaseColumnDefinition("",
				DataType.STRING);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_DataTypeIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		databaseColumnDefinition = new DatabaseColumnDefinition(COLUMN_NAME,
				null);
	}

	@Test
	public void getColumnClause_SimpleColumn_Success() throws Exception {
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	private void assertThatDatabaseColumnDefinitionMatches(
			String expectedColumnDefinition) {
		assertThat(databaseColumnDefinition.getColumnClause(),
				is(expectedColumnDefinition));
	}

	@Test
	public void getColumnClause_ColumnWithPrimaryKey_Success() throws Exception {
		databaseColumnDefinition.setPrimaryKey();
		expectedColumnDefinitionStringBuilder
				.append(" primary key autoincrement");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getColumnClause_ColumnWithUniqueKey_Success() throws Exception {
		databaseColumnDefinition.setUniqueKey();
		expectedColumnDefinitionStringBuilder.append(" unique");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getColumnClause_ColumnNotNull_Success() throws Exception {
		databaseColumnDefinition.setNotNull();
		expectedColumnDefinitionStringBuilder.append(" not null");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getColumnClause_ColumnWithDefaultStringValue_Success()
			throws Exception {
		String defaultValue = "*** DEFAULT VALUE ***";
		databaseColumnDefinition.setDefaultValue(defaultValue);
		expectedColumnDefinitionStringBuilder.append(" default `");
		expectedColumnDefinitionStringBuilder.append(defaultValue);
		expectedColumnDefinitionStringBuilder.append("`");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getColumnClause_ColumnWithDefaultEmptyStringValue_Success()
			throws Exception {
		databaseColumnDefinition.setDefaultValue("");
		expectedColumnDefinitionStringBuilder.append(" default ``");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getColumnClause_ColumnWithDefaultBooleanValue_Success()
			throws Exception {
		databaseColumnDefinition.setDefaultValue(true);
		expectedColumnDefinitionStringBuilder.append(" default `true`");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getColumnClause_ColumnWithDefaultIntegerValue_Success()
			throws Exception {
		databaseColumnDefinition.setDefaultValue(67283);
		expectedColumnDefinitionStringBuilder.append(" default 67283");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getColumnClause_ColumnNotNullWithDefaultValueUniqueKeyAndPrimaryKeyDefaultIntegerValue_Success()
			throws Exception {
		// This test does not intend to create a use able column definition in a
		// sqlite database. It merely test whether the elements of the clause
		// are ordered correctly independent of the order in which the setters
		// were called.
		databaseColumnDefinition.setNotNull();
		databaseColumnDefinition.setDefaultValue(67283);
		databaseColumnDefinition.setUniqueKey();
		databaseColumnDefinition.setPrimaryKey();
		expectedColumnDefinitionStringBuilder
				.append(" primary key autoincrement not null unique default 67283");
		assertThatDatabaseColumnDefinitionMatches(expectedColumnDefinitionStringBuilder
				.toString());
	}

	@Test
	public void getName() throws Exception {
		assertThat(databaseColumnDefinition.getName(), is(COLUMN_NAME));
	}
}
