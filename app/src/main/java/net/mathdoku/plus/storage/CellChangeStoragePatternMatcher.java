package net.mathdoku.plus.storage;

import net.mathdoku.plus.storage.selector.StorageDelimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CellChangeStoragePatternMatcher {
	@SuppressWarnings("unused")
	private static final String TAG = CellChangeStoragePatternMatcher.class
			.getName();

	public static final String SAVE_GAME_CELL_CHANGE_LINE = "CELL_CHANGE";

	private Matcher matcherOuter;
	private Matcher matcherInner;

	// The outer regular expression is used to validate whether the string
	// starts with correct line identifier. Further checking is done with the
	// inner regular expression which can be used recursively.
	private static final String REGEXP_OUTER = "^" + SAVE_GAME_CELL_CHANGE_LINE
			+ StorageDelimiter.FIELD_DELIMITER_LEVEL1 + "(.*)$";
	private static final int GROUP_CELL_CHANGE = 1;
	private static final Pattern PATTERN_OUTER = Pattern.compile(REGEXP_OUTER);

	// Regexp and groups inside. Groups 4 - 6 are helper groups which are
	// needed to ensure the validity of the cell information but are not
	// used programmatic.
	private static final String REGEXP_INNER = "^\\[(\\d+)"
			+ StorageDelimiter.FIELD_DELIMITER_LEVEL1 + "(\\d*)"
			+ StorageDelimiter.FIELD_DELIMITER_LEVEL1 + "((\\d*)|((\\d*"
			+ StorageDelimiter.FIELD_DELIMITER_LEVEL2 + ")+))"
			+ StorageDelimiter.FIELD_DELIMITER_LEVEL1 + "(.*)\\]$";
	private static final int GROUP_CELL_NUMBER = 1;
	private static final int GROUP_PREVIOUS_USER_VALUE = 2;
	private static final int GROUP_PREVIOUS_POSSIBLE_VALUES = 3;
	private static final int GROUP_RELATED_CELL_CHANGED = 7;
	private static final Pattern PATTERN_INNER = Pattern.compile(REGEXP_INNER);

	public boolean matchesOuter(String line) {
		matcherOuter = PATTERN_OUTER.matcher(line);
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

	public List<Integer> getPreviousPossibleValues() {
		List<Integer> previousPossibleValues = new ArrayList<Integer>();
		if (!matcherInner.group(GROUP_PREVIOUS_POSSIBLE_VALUES).isEmpty()) {
			for (String possible : matcherInner.group(
					GROUP_PREVIOUS_POSSIBLE_VALUES).split(
					StorageDelimiter.FIELD_DELIMITER_LEVEL2)) {
				previousPossibleValues.add(Integer.valueOf(possible));
			}
		}
		return previousPossibleValues;
	}

	public List<String> getRelatedCellChanges() {
		return new RelatedCellChangeSplitter(
				matcherInner.group(GROUP_RELATED_CELL_CHANGED)).parse();
	}

	public boolean matchesInner(String line) {
		matcherInner = PATTERN_INNER.matcher(line);
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

	// The related cell changes can not be matched using a regular expression
	// because they recursively can contain other related cell changes.
	private class RelatedCellChangeSplitter {
		private String concatenatedStringOfCellChanges;
		private int levelNestedGroup;
		private int startPosGroup;
		private int index;
		private List<String> relatedCellChanges;

		public RelatedCellChangeSplitter(String concatenatedStringOfCellChanges) {
			this.concatenatedStringOfCellChanges = concatenatedStringOfCellChanges;
			relatedCellChanges = new ArrayList<String>();
		}

		public List<String> parse() {
			levelNestedGroup = 0;
			startPosGroup = 0;
			index = 0;
			for (char c : concatenatedStringOfCellChanges.toCharArray()) {
				switch (c) {
				case '[':
					startGroup();
					break;
				case ']':
					endGroup();
					break;
				default:
					validateFieldDelimiter(c);
					break;
				}
				index++;
			}

			return relatedCellChanges;
		}

		private void startGroup() {
			if (levelNestedGroup == 0) {
				// Remember starting position of outer group only
				startPosGroup = index;
			}
			levelNestedGroup++;
		}

		private void endGroup() {
			levelNestedGroup--;
			if (levelNestedGroup == 0) {
				// Just completed an outer group.
				String group = concatenatedStringOfCellChanges.substring(
						startPosGroup, index + 1);
				relatedCellChanges.add(group);
			}

		}

		private void validateFieldDelimiter(char c) {
			if (levelNestedGroup == 0
					&& !Character.toString(c).equals(
							StorageDelimiter.FIELD_DELIMITER_LEVEL2)) {
				throw new IllegalStateException(String.format(
						"Unexpected character '%c' at position %d in group"
								+ " '%s'.", c, index,
						concatenatedStringOfCellChanges));
			}
		}
	}
}
