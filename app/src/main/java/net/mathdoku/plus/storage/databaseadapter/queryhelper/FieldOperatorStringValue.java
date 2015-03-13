package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import net.mathdoku.plus.storage.databaseadapter.database.DatabaseUtil;

public class FieldOperatorStringValue extends FieldOperatorValue {
    public FieldOperatorStringValue(String field, Operator operator, String value) {
        super(field, operator, value == null ? NULL_VALUE : DatabaseUtil.stringBetweenQuotes(value));
    }
}
