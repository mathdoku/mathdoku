package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;
import net.mathdoku.plus.util.ParameterValidator;

public class JoinHelper extends QueryHelper {
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
		query.append(SPACE);
		query.append(DatabaseUtil.stringBetweenBackTicks(leftHandTableName));
		query.append(SPACE);
		query.append(joinType);
		query.append(SPACE);
		query.append(DatabaseUtil.stringBetweenBackTicks(rightHandTableName));
		query.append(" ON ");
		query.append(DatabaseUtil.tableAndColumnBetweenBackTicks(leftHandTableName, leftHandColumnName));
		query.append(SPACE);
		query.append(EQUALS_OPERATOR);
		query.append(SPACE);
		query.append(DatabaseUtil.tableAndColumnBetweenBackTicks(rightHandTableName, rightHandColumnName));

		return super.toString();
	}
}
