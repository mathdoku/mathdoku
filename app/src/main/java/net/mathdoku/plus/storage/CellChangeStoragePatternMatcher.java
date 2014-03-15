package net.mathdoku.plus.storage;

import net.mathdoku.plus.storage.database.SolvingAttemptDatabaseAdapter;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CellChangeStoragePatternMatcher {
	private static final String TAG = CellChangeStoragePatternMatcher.class
			.getName();

	public static final String SAVE_GAME_CELL_CHANGE_LINE = "CELL_CHANGE";

	private Matcher matcherOuter;
	private Matcher matcherInner;

	// The outer regular expression is used to validate whether the string
	// starts with correct line identifier. Further checking is done with the
	// inner regular expression which can be used recursively.
	private static final String REGEXP_OUTER = "^" + SAVE_GAME_CELL_CHANGE_LINE
			+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + "(.*)$";
	private static final int GROUP_CELL_CHANGE = 1;

	// Regexp and groups inside. Groups 4 - 6 are helper groups which are
	// needed to ensure the validity of the cell information but are not
	// used programmatic.
	private static final String REGEXP_INNER = "^\\[(\\d+)"
			+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + "(\\d*)"
			+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1
			+ "((\\d*)|((\\d*"
			+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2 + ")+))"
			+ SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL1 + "(.*)\\]$";
	final int GROUP_CELL_NUMBER = 1;
	final int GROUP_PREVIOUS_USER_VALUE = 2;
	final int GROUP_PREVIOUS_POSSIBLE_VALUES = 3;
	final int GROUP_RELATED_CELL_CHANGED = 7;

	private static final Pattern patternOuter = Pattern.compile(REGEXP_OUTER);
	private static final Pattern patternInner = Pattern.compile(REGEXP_INNER);

	public boolean matchesOuter(String line) {
		matcherOuter = patternOuter.matcher(line);
		return matcherOuter.matches();
	}

	public String getDataOuter() {
		return matcherOuter.group(GROUP_CELL_CHANGE);
	}

	public int getCellNumber() {
		return Integer.valueOf(matcherInner.group(GROUP_CELL_NUMBER));
	}

	public int getPreviousEnteredValue() {
		return Integer.valueOf(matcherInner.group(GROUP_PREVIOUS_USER_VALUE));
	}

	public ArrayList<Integer> getPreviousPossibleValues() {
		ArrayList<Integer> previousPossibleValues = new ArrayList<Integer>();
		if (!matcherInner.group(GROUP_PREVIOUS_POSSIBLE_VALUES).equals("")) {
			for (String possible : matcherInner.group(
					GROUP_PREVIOUS_POSSIBLE_VALUES).split(
					SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
				previousPossibleValues.add(Integer.valueOf(possible));
			}
		}
		return previousPossibleValues;
	}

	public ArrayList<String> getRelatedCellChanges() {
		return parseRelatedCellChanges(matcherInner
				.group(GROUP_RELATED_CELL_CHANGED));
	}

	private ArrayList<String> parseRelatedCellChanges(
			String concatenatedStringOfCellChanges) {

		// The related cell changes can not be matched using a regular
		// expression because they recursively can contain other related
		// cell changes.
		ArrayList<String> relatedCellChanges = new ArrayList<String>();
		int levelNestedGroup = 0;
		int startPosGroup = 0;
		int index = 0;
		for (char c : concatenatedStringOfCellChanges.toCharArray()) {
			switch (c) {
			case '[': // Start of new group
				if (levelNestedGroup == 0) {
					// Remember starting position of outer group only
					startPosGroup = index;
				}
				levelNestedGroup++;
				break;
			case ']':
				levelNestedGroup--;
				if (levelNestedGroup == 0) {
					// Just completed a group.
					String group = concatenatedStringOfCellChanges.substring(
							startPosGroup, index + 1);
					relatedCellChanges.add(group);
				}
				break;
			default:
				if (levelNestedGroup == 0
						&& !Character
								.toString(c)
								.equals(SolvingAttemptDatabaseAdapter.FIELD_DELIMITER_LEVEL2)) {
					throw new IllegalStateException(
							String
									.format("Unexpected character '%c' at position %d in group '%s'.",
											c, index,
											concatenatedStringOfCellChanges));
				}
				break;
			}
			index++;
		}

		return relatedCellChanges;
	}

	public boolean matchesInner(String line) {
		matcherInner = patternInner.matcher(line);
		return matcherInner.matches();
	}

	public String debugLogOuter() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append("Start index: ")
				.append(matcherOuter.start())
				.append("\n");
		stringBuilder
				.append("End index: ")
				.append(matcherOuter.end())
				.append("\n");
		stringBuilder
				.append("#groups: ")
				.append(matcherOuter.groupCount())
				.append("\n");
		stringBuilder.append("Cell change: ").append(
				matcherOuter.group(GROUP_CELL_CHANGE));
		return stringBuilder.toString();
	}

	public String debugLogInner(String indent) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder
				.append(indent)
				.append("Number of groups found: ")
				.append(matcherInner.groupCount())
				.append("\n");
		stringBuilder
				.append(indent)
				.append("Cell number: ")
				.append(matcherInner.group(GROUP_CELL_NUMBER))
				.append("\n");
		stringBuilder
				.append(indent)
				.append("Previous user value: ")
				.append(matcherInner.group(GROUP_PREVIOUS_USER_VALUE))
				.append("\n");
		stringBuilder
				.append(indent)
				.append("Previous possible values: ")
				.append(matcherInner.group(GROUP_PREVIOUS_POSSIBLE_VALUES))
				.append("\n");
		stringBuilder
				.append(indent)
				.append("Related cell changes: ")
				.append(matcherInner.group(GROUP_RELATED_CELL_CHANGED))
				.append("\n");

		return stringBuilder.toString();
	}
}
