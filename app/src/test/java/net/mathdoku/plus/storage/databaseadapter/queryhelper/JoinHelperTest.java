package net.mathdoku.plus.storage.databaseadapter.queryhelper;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JoinHelperTest {
	private JoinHelper joinHelper;
	private static final String LEFT_HAND_TABLE = "LeftHandTable";
	private static final String LEFT_HAND_COLUMN = "LeftHandColumn";
	private static final String RIGHT_HAND_TABLE = "RightHandTable";
	private static final String RIGHT_HAND_COLUMN = "RightHandColumn";

	@Test(expected = IllegalArgumentException.class)
	public void constructor_LeftHandTableIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper(null, LEFT_HAND_COLUMN);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_LeftHandTableIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper("", LEFT_HAND_COLUMN);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_LeftHandColumnIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper(LEFT_HAND_TABLE, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor_LeftHandColumnIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper(LEFT_HAND_TABLE, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void innerJoinWith_RightHandTableIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper(LEFT_HAND_TABLE, LEFT_HAND_COLUMN).innerJoinWith(null,
				RIGHT_HAND_COLUMN);
	}

	@Test(expected = IllegalArgumentException.class)
	public void innerJoinWith_RighyHandTableIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper(LEFT_HAND_TABLE, LEFT_HAND_COLUMN).innerJoinWith("",
				RIGHT_HAND_COLUMN);
	}

	@Test(expected = IllegalArgumentException.class)
	public void innerJoinWith_RightHandColumnIsNull_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper(LEFT_HAND_TABLE, LEFT_HAND_COLUMN).innerJoinWith(
				RIGHT_HAND_TABLE, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void innerJoinWith_RightHandColumnIsEmpty_ThrowsIllegalArgumentException()
			throws Exception {
		new JoinHelper(LEFT_HAND_TABLE, LEFT_HAND_COLUMN).innerJoinWith(
				RIGHT_HAND_TABLE, "");
	}

	@Test(expected = IllegalStateException.class)
	public void toString_JointTypeNotSet_ThrowsIllegalStateException()
			throws Exception {
		new JoinHelper(LEFT_HAND_TABLE, LEFT_HAND_COLUMN).toString();
	}

	@Test
	public void toString_AllParametersSet_JoinStringCreated() throws Exception {
		assertThat(
				new JoinHelper(LEFT_HAND_TABLE, LEFT_HAND_COLUMN)
						.innerJoinWith(RIGHT_HAND_TABLE, RIGHT_HAND_COLUMN)
						.toString(),
				is(" `LeftHandTable` INNER JOIN `RightHandTable` ON `LeftHandTable`.`LeftHandColumn` = `RightHandTable`.`RightHandColumn`"));

	}
}
