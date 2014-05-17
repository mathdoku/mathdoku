package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;

public class FieldOperatorBooleanValue extends FieldOperatorValue {
	public FieldOperatorBooleanValue(String field, Operator operator,
			boolean value) {
		super(field, operator, DatabaseUtil.toQuotedSQLiteString(value));
	}
}
