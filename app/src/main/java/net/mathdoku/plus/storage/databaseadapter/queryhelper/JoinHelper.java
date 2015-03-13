package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.ParameterValidator;

public class JoinHelper {
    private static final String EQUALS_OPERATOR = "=";
    private final String leftHandTableName;
    private final String leftHandColumnName;
    private String joinType;
    private String rightHandTableName;
    private String rightHandColumnName;

    public JoinHelper(String leftHandTableName, String leftHandColumnName) {
        ParameterValidator.validateNotNullOrEmpty(leftHandTableName);
        ParameterValidator.validateNotNullOrEmpty(leftHandColumnName);
        this.leftHandTableName = leftHandTableName;
        this.leftHandColumnName = leftHandColumnName;
    }

    public JoinHelper innerJoinWith(String rightHandTableName, String rightHandColumnName) {
        ParameterValidator.validateNotNullOrEmpty(rightHandTableName);
        ParameterValidator.validateNotNullOrEmpty(rightHandColumnName);
        this.joinType = "INNER JOIN";
        this.rightHandTableName = rightHandTableName;
        this.rightHandColumnName = rightHandColumnName;
        return this;
    }

    @Override
    public String toString() {
        if (joinType == null || joinType.isEmpty()) {
            throw new IllegalStateException("Join type and right hand side parameters have not been set.");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(QueryHelper.SPACE);
        stringBuilder.append(DatabaseUtil.stringBetweenBackTicks(leftHandTableName));
        stringBuilder.append(QueryHelper.SPACE);
        stringBuilder.append(joinType);
        stringBuilder.append(QueryHelper.SPACE);
        stringBuilder.append(DatabaseUtil.stringBetweenBackTicks(rightHandTableName));
        stringBuilder.append(" ON ");
        stringBuilder.append(DatabaseUtil.tableAndColumnBetweenBackTicks(leftHandTableName, leftHandColumnName));
        stringBuilder.append(QueryHelper.SPACE);
        stringBuilder.append(EQUALS_OPERATOR);
        stringBuilder.append(QueryHelper.SPACE);
        stringBuilder.append(DatabaseUtil.tableAndColumnBetweenBackTicks(rightHandTableName, rightHandColumnName));
        return stringBuilder.toString();
    }

    @Override
    @SuppressWarnings("all")
    // Needed to suppress sonar warning on cyclomatic complexity
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JoinHelper)) {
            return false;
        }

        JoinHelper that = (JoinHelper) o;

        if (!joinType.equals(that.joinType)) {
            return false;
        }
        if (!leftHandColumnName.equals(that.leftHandColumnName)) {
            return false;
        }
        if (!leftHandTableName.equals(that.leftHandTableName)) {
            return false;
        }
        if (!rightHandColumnName.equals(that.rightHandColumnName)) {
            return false;
        }
        if (!rightHandTableName.equals(that.rightHandTableName)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = leftHandTableName.hashCode();
        result = 31 * result + leftHandColumnName.hashCode();
        result = 31 * result + joinType.hashCode();
        result = 31 * result + rightHandTableName.hashCode();
        result = 31 * result + rightHandColumnName.hashCode();
        return result;
    }
}
