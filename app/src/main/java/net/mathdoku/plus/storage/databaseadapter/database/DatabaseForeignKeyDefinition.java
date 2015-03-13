package net.mathdoku.plus.storage.databaseadapter.database;

import net.mathdoku.plus.util.ParameterValidator;

public class DatabaseForeignKeyDefinition {
    private final String clause;

    public DatabaseForeignKeyDefinition(String column, String refersToTable, String refersToColumn) {
        ParameterValidator.validateNotNullOrEmpty(column);
        ParameterValidator.validateNotNullOrEmpty(refersToTable);
        ParameterValidator.validateNotNullOrEmpty(refersToColumn);

        StringBuilder foreignKey = new StringBuilder();
        foreignKey.append("FOREIGN KEY(");
        foreignKey.append(DatabaseUtil.stringBetweenBackTicks(column.trim()));
        foreignKey.append(") REFERENCES ");
        foreignKey.append(refersToTable.trim());
        foreignKey.append("(");
        foreignKey.append(refersToColumn.trim());
        foreignKey.append(")");
        clause = foreignKey.toString();
    }

    public String getForeignKeyClause() {
        return clause;
    }

}
