package net.mathdoku.plus.storage.databaseadapter.queryhelper;

public class FieldOperatorIntegerValue extends FieldOperatorValue {
    public FieldOperatorIntegerValue(String field, Operator operator, int value) {
        super(field, operator, String.valueOf(value));
    }
}
