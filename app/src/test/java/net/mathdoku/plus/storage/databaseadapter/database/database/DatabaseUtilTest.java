package net.mathdoku.plus.storage.databaseadapter.database.database;

import org.junit.Test;

import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DatabaseUtilTest {
	@Test
	public void stringBetweenBackTicks() throws Exception {
		assertThat(DatabaseUtil.stringBetweenBackTicks("STRING"),
				is("`STRING`"));
	}

	@Test
	public void stringBetweenQuotes() throws Exception {
		assertThat(DatabaseUtil.stringBetweenQuotes("STRING"), is("'STRING'"));
	}

	@Test
	public void toSQLiteBoolean_true_true() throws Exception {
		assertThat(DatabaseUtil.toSQLiteBoolean(true), is("true"));
	}

	@Test
	public void toSQLiteBoolean_true_false() throws Exception {
		assertThat(DatabaseUtil.toSQLiteBoolean(false), is("false"));
	}

	@Test
	public void toQuotedSQLiteString_true_true() throws Exception {
		assertThat(DatabaseUtil.toQuotedSQLiteString(true), is("'true'"));
	}

	@Test
	public void toQuotedSQLiteString_false_false() throws Exception {
		assertThat(DatabaseUtil.toQuotedSQLiteString(false), is("'false'"));
	}

	@Test
	public void valueOfSQLiteBoolean_true_true() throws Exception {
		assertThat(DatabaseUtil.valueOfSQLiteBoolean("true"), is(true));
	}

	@Test
	public void valueOfSQLiteBoolean_false_false() throws Exception {
		assertThat(DatabaseUtil.valueOfSQLiteBoolean("false"), is(false));
	}

	@Test(expected = IllegalStateException.class)
	public void valueOfSQLiteBoolean_True_ThrowsIllegalStateException()
			throws Exception {
		DatabaseUtil.valueOfSQLiteBoolean("True");
	}

	@Test
	public void getCurrentSQLiteTimestamp() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.APRIL, 29);
		calendar.set(Calendar.HOUR_OF_DAY, 17);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 15);
		calendar.set(Calendar.MILLISECOND, 1);

		// Instantiate DatabaseUtil to feed it with a specific time which will
		// be returned as current time.
		assertThat(
				new DatabaseUtil(calendar.getTimeInMillis())
						.getCurrentSQLiteTimestamp(),
				is("2014-04-29 17:30:15.001"));
	}

	@Test
	public void getCurrentMinusOffsetSQLiteTimestamp() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2014, Calendar.MAY, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 2);
		calendar.set(Calendar.MILLISECOND, 3);
		long offsetToLastMillisecondOnPreviousDay = (60 + 2) * 1000 + 4;

		// Instantiate DatabaseUtil to feed it with a specific time which will
		// be returned as current time.
		assertThat(
				new DatabaseUtil(calendar.getTimeInMillis())
						.getCurrentMinusOffsetSQLiteTimestamp(offsetToLastMillisecondOnPreviousDay),
				is("2014-04-30 23:59:59.999"));
	}

	@Test
	public void toSQLiteTimestamp() throws Exception {
		assertThat(DatabaseUtil.toSQLiteTimestamp(1399042037851L),
				is("2014-05-02 16:47:17.851"));
	}

	@Test
	public void toSQLTimestamp_TimeIsNotNull() throws Exception {
		assertThat(DatabaseUtil.toSQLTimestamp("2014-05-02 16:47:17.851").getTime(), is(1399042037851L));
	}

	@Test
	public void toSQLTimestamp_TimeIsNull() throws Exception {
		assertThat(DatabaseUtil.toSQLTimestamp(null).getTime(), is(0L));
	}

	@Test
	public void valueOfSQLiteTimestamp_TimeIsNotNull() throws Exception {
		assertThat(DatabaseUtil.valueOfSQLiteTimestamp("2014-05-02 16:47:17.851"), is(1399042037851L));
	}

	@Test
	public void valueOfSQLiteTimestamp_TimeIsNull() throws Exception {
		assertThat(DatabaseUtil.valueOfSQLiteTimestamp(null), is(0L));
	}
}
