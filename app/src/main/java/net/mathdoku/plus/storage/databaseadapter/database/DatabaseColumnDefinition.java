package net.mathdoku.plus.storage.databaseadapter.database;

import net.mathdoku.plus.util.ParameterValidator;

public class DatabaseColumnDefinition {
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
		setDefaultValueAsString(DatabaseUtil
				.stringBetweenBackTicks(defaultValue == null ? ""
						: defaultValue));
		return this;
	}

	private void setDefaultValueAsString(String defaultValue) {
		hasDefaultValue = true;
		this.defaultValue = defaultValue;
	}

	public DatabaseColumnDefinition setDefaultValue(int integer) {
		setDefaultValueAsString(Integer.toString(integer));
		return this;
	}

	public DatabaseColumnDefinition setDefaultValue(boolean booleanValue) {
		setDefaultValueAsString(DatabaseUtil
				.stringBetweenBackTicks(DatabaseUtil
						.toSQLiteBoolean(booleanValue)));
		return this;
	}

	public String getColumnClause() {
		StringBuilder columnDefinition = new StringBuilder();
		columnDefinition.append(DatabaseUtil.stringBetweenBackTicks(columnName
				.trim()));
		columnDefinition.append(" ");
		columnDefinition.append(dataType.getSqliteDataType());
		if (primaryKey) {
			columnDefinition.append(" primary key autoincrement");
		}
		if (notNull) {
			columnDefinition.append(" not null");
		}
		if (uniqueKey) {
			columnDefinition.append(" unique");
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

	public DataType getDataType() {
		return dataType;
	}
}
