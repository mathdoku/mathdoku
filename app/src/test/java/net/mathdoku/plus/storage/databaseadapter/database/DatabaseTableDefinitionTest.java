package net.mathdoku.plus.storage.databaseadapter.database;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DatabaseTableDefinitionTest {
	private DatabaseTableDefinition databaseTableDefinition;
	private static final String TABLE_NAME = "*** TEST TABLE ***";
	private static final String FOREIGN_KEY_DEFINITION = "*** FOREIGN KEY DEFINITION ***";
	private static DatabaseColumnDefinition databaseInitialColumnDefinitionMock = mock(DatabaseColumnDefinition.class);
	private static final String COLUMN_NAME_DEFINITION = "*** MOCK COLUMN DEFINITION ***";
	private static final String COLUMN_NAME_MOCK_NAME = "*** MOCK COLUMN ***";

	@Before
	public void setup() {
		databaseTableDefinition = new DatabaseTableDefinition(TABLE_NAME);
		when(databaseInitialColumnDefinitionMock.getColumnClause()).thenReturn(
				COLUMN_NAME_DEFINITION);
		when(databaseInitialColumnDefinitionMock.getName()).thenReturn(
				COLUMN_NAME_MOCK_NAME);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_TableNameIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new DatabaseTableDefinition(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_TableNameIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new DatabaseTableDefinition("");
	}

	@Test(expected = DatabaseException.class)
	public void addColumn_TableAlreadyComposed_ThrowsDatabaseException()
			throws Exception {
		databaseTableDefinition.addColumn(databaseInitialColumnDefinitionMock);
		databaseTableDefinition.build();

		databaseTableDefinition.addColumn(mock(DatabaseColumnDefinition.class));
	}

	@Test(expected = DatabaseException.class)
	public void setForeignKey_TableAlreadyComposed_ThrowsDatabaseException()
			throws Exception {
		databaseTableDefinition.addColumn(databaseInitialColumnDefinitionMock);
		databaseTableDefinition.build();

		addForeignKey();
	}

	private void addForeignKey() {
		DatabaseForeignKeyDefinition databaseForeignKeyDefinition = mock(DatabaseForeignKeyDefinition.class);
		databaseTableDefinition.setForeignKey(databaseForeignKeyDefinition);
		when(databaseForeignKeyDefinition.getForeignKeyClause()).thenReturn(
				FOREIGN_KEY_DEFINITION);
	}

	@Test(expected = DatabaseException.class)
	public void build_NoColumnsAdded_ThrowsDatabaseException() throws Exception {
		databaseTableDefinition.build();
	}

	@Test
	public void getTableName() throws Exception {
		assertThat(databaseTableDefinition.getTableName(), is(TABLE_NAME));
	}

	@Test
	public void getColumnNames_SingleColumnTable_ColumnsRetrieved()
			throws Exception {
		databaseTableDefinition.addColumn(databaseInitialColumnDefinitionMock);
		databaseTableDefinition.build();
		assertThat(databaseTableDefinition.getColumnNames().length, is(1));
		assertThat(databaseTableDefinition.getColumnNames()[0],
				is(COLUMN_NAME_MOCK_NAME));
	}

	@Test
	public void getCreateTableSQL_SingleColumn_Success() throws Exception {
		databaseTableDefinition.addColumn(databaseInitialColumnDefinitionMock);
		databaseTableDefinition.build();

		StringBuilder expectedDatabaseTableDefinitionStringBuilder = new StringBuilder();
		expectedDatabaseTableDefinitionStringBuilder.append("CREATE TABLE `");
		expectedDatabaseTableDefinitionStringBuilder.append(TABLE_NAME);
		expectedDatabaseTableDefinitionStringBuilder.append("` (");
		expectedDatabaseTableDefinitionStringBuilder
				.append(COLUMN_NAME_DEFINITION);
		expectedDatabaseTableDefinitionStringBuilder.append(")");

		assertThat(databaseTableDefinition.getCreateTableSQL(),
				is(expectedDatabaseTableDefinitionStringBuilder.toString()));
	}

	@Test
	public void getCreateTableSQL_MultipleColumns_Success() throws Exception {
		// Add same mock multiple times
		databaseTableDefinition.addColumn(databaseInitialColumnDefinitionMock);
		databaseTableDefinition.addColumn(databaseInitialColumnDefinitionMock);
		databaseTableDefinition.build();

		StringBuilder expectedDatabaseTableDefinitionStringBuilder = new StringBuilder();
		expectedDatabaseTableDefinitionStringBuilder.append("CREATE TABLE `");
		expectedDatabaseTableDefinitionStringBuilder.append(TABLE_NAME);
		expectedDatabaseTableDefinitionStringBuilder.append("` (");
		expectedDatabaseTableDefinitionStringBuilder
				.append(COLUMN_NAME_DEFINITION);
		expectedDatabaseTableDefinitionStringBuilder.append(", ");
		expectedDatabaseTableDefinitionStringBuilder
				.append(COLUMN_NAME_DEFINITION);
		expectedDatabaseTableDefinitionStringBuilder.append(")");

		assertThat(databaseTableDefinition.getCreateTableSQL(),
				is(expectedDatabaseTableDefinitionStringBuilder.toString()));
	}

	@Test
	public void getCreateTableSQL_SingleColumnAndForeignKey_Success()
			throws Exception {
		databaseTableDefinition.addColumn(databaseInitialColumnDefinitionMock);
		addForeignKey();
		databaseTableDefinition.build();

		StringBuilder expectedDatabaseTableDefinitionStringBuilder = new StringBuilder();
		expectedDatabaseTableDefinitionStringBuilder.append("CREATE TABLE `");
		expectedDatabaseTableDefinitionStringBuilder.append(TABLE_NAME);
		expectedDatabaseTableDefinitionStringBuilder.append("` (");
		expectedDatabaseTableDefinitionStringBuilder
				.append(COLUMN_NAME_DEFINITION);
		expectedDatabaseTableDefinitionStringBuilder.append(", ");
		expectedDatabaseTableDefinitionStringBuilder
				.append(FOREIGN_KEY_DEFINITION);
		expectedDatabaseTableDefinitionStringBuilder.append(")");

		assertThat(databaseTableDefinition.getCreateTableSQL(),
				is(expectedDatabaseTableDefinitionStringBuilder.toString()));
	}
}
