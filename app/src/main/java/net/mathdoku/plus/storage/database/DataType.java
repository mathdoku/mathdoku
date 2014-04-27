package net.mathdoku.plus.storage.database;

public enum DataType {
	INTEGER("integer"), LONG("long"), BOOLEAN("text"), STRING("text"), TIMESTAMP("datetime");

	private final String sqliteDataType;

	private DataType(String sqliteDataType) {
		this.sqliteDataType = sqliteDataType;
	}

	public String getSqliteDataType() {
		return sqliteDataType;
	}
}
