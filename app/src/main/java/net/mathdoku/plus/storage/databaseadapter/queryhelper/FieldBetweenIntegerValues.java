package net.mathdoku.plus.storage.databaseadapter.queryhelper;

public class FieldBetweenIntegerValues extends FieldOperatorValue {
	public FieldBetweenIntegerValues(String field, int lowValue, int highValue) {
		super(field, String.valueOf(lowValue), String.valueOf(highValue));
	}
}
