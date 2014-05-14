package net.mathdoku.plus.storage.databaseadapter.database;

import net.mathdoku.plus.util.ParameterValidator;
import net.mathdoku.plus.util.Util;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTableDefinition {
	private final String tableName;
	private final List<DatabaseColumnDefinition> databaseColumnDefinitions;
	private DatabaseForeignKeyDefinition foreignKey;
	private String[] columnNames;

	public DatabaseTableDefinition(String tableName) {
		ParameterValidator.validateNotNullOrEmpty(tableName);
		this.tableName = tableName;
		databaseColumnDefinitions = new ArrayList<DatabaseColumnDefinition>();
	}

	public void addColumn(DatabaseColumnDefinition databaseColumnDefinition) {
		if (isComposed()) {
			throwAlterDatabaseTableNotAllowedAfterCompose();
		}
		databaseColumnDefinitions.add(databaseColumnDefinition);
	}

	private boolean isComposed() {
		return columnNames != null && columnNames.length > 0;
	}

	private void throwAlterDatabaseTableNotAllowedAfterCompose() {
		throw new DatabaseException(
				"Cannot alter database table after it is composed.");
	}

	public void setForeignKey(DatabaseForeignKeyDefinition foreignKey) {
		if (isComposed()) {
			throwAlterDatabaseTableNotAllowedAfterCompose();
		}
		this.foreignKey = foreignKey;
	}

	public void build() {
		if (Util.isListNullOrEmpty(databaseColumnDefinitions)) {
			throw new DatabaseException(
					"At least one column has to be specified.");
		}

		// Array with column names is pre filled as method getColumnNames is
		// called often.
		columnNames = new String[databaseColumnDefinitions.size()];
		int index = 0;
		for (DatabaseColumnDefinition databaseColumnDefinition : databaseColumnDefinitions) {
			columnNames[index++] = databaseColumnDefinition.getName();
		}
	}

	public String getTableName() {
		return tableName;
	}

	public String[] getColumnNames() {
		throwIllegalStateExceptionIfNotYetComposed();
		return columnNames;
	}

	private void throwIllegalStateExceptionIfNotYetComposed() {
		if (!isComposed()) {
			new IllegalStateException(
					"Cannot be called until database table has been composed.");
		}
	}

	public DataType[] getColumnTypes() {
		throwIllegalStateExceptionIfNotYetComposed();

		// Array with column types is not pre filled as method getColumnTypes is
		// only called a few times when running the unit tests.
		DataType[] columnTypes = new DataType[databaseColumnDefinitions.size()];
		int index = 0;
		for (DatabaseColumnDefinition databaseColumnDefinition : databaseColumnDefinitions) {
			columnTypes[index++] = databaseColumnDefinition.getDataType();
		}
		return columnTypes;
	}

	public String getCreateTableSQL() {
		StringBuilder query = new StringBuilder();
		query.append("CREATE TABLE ");
		query.append(DatabaseUtil.stringBetweenBackTicks(tableName));
		query.append(" (");
		int remainingColumns = databaseColumnDefinitions.size();
		for (DatabaseColumnDefinition databaseColumnDefinition : databaseColumnDefinitions) {
			query.append(databaseColumnDefinition.getColumnClause());
			if (--remainingColumns > 0) {
				query.append(", ");
			}
		}
		if (foreignKey != null) {
			query.append(", ");
			query.append(foreignKey.getForeignKeyClause());
		}
		query.append(")");
		return query.toString();
	}
}
