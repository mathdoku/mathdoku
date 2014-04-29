package net.mathdoku.plus.storage.databaseadapter.database;

import net.mathdoku.plus.util.ParameterValidator;

import static net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil.stringBetweenBackTicks;
import static net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil.toSQLiteBoolean;

class DatabaseColumnDefinition {
	private final String columnName;
	private final DataType dataType;
	private boolean primaryKey;
	private boolean uniqueKey;
	private boolean notNull;
	private boolean hasDefaultValue;
	private String defaultValue;

	public DatabaseColumnDefinition(String columnName, DataType dataType) {
		ParameterValidator.validateNotNullOrEmpty(columnName);
		ParameterValidator.validateNotNull(dataType);
		this.columnName = columnName;
		this.dataType = dataType;
	}

	public DatabaseColumnDefinition setPrimaryKey() {
		primaryKey = true;
		return this;
	}

	public DatabaseColumnDefinition setUniqueKey() {
		uniqueKey = true;
		return this;
	}

	public DatabaseColumnDefinition setNotNull() {
		notNull = true;
		return this;
	}

	public DatabaseColumnDefinition setDefaultValue(String defaultValue) {
		hasDefaultValue = true;
		if (defaultValue == null || defaultValue.isEmpty()) {
			this.defaultValue = stringBetweenBackTicks("");
		} else {
			this.defaultValue = defaultValue;
		}
		return this;
	}

	public DatabaseColumnDefinition setDefaultValue(int integer) {
		return setDefaultValue(Integer.toString(integer));
	}

	public DatabaseColumnDefinition setDefaultValue(boolean booleanValue) {
		return setDefaultValue(stringBetweenBackTicks(toSQLiteBoolean(booleanValue)));
	}

	public String getColumnClause() {
		String space = " ";

		StringBuilder columnDefinition = new StringBuilder();
		columnDefinition.append(stringBetweenBackTicks(columnName.trim()));
		columnDefinition.append(space);
		columnDefinition.append(dataType.getSqliteDataType());
		if (primaryKey) {
			columnDefinition.append(" primary key autoincrement");
		}
		if (uniqueKey) {
			columnDefinition.append(" unique");
		}
		if (notNull) {
			columnDefinition.append(" not null");
		}
		if (hasDefaultValue) {
			columnDefinition.append(" default ");
			columnDefinition.append(defaultValue);
		}
		return columnDefinition.toString();
	}

	public String getName() {
		return columnName;
	}
}
