package robolectric;

import android.app.Activity;

import net.mathdoku.plus.storage.databaseadapter.DatabaseHelper;
import net.mathdoku.plus.util.Util;

public class TestRunnerHelper {
	private static TestRunnerHelper singletonTestRunnerHelper;
	private static String lastInstantiatedByClassName;
	private final Activity activity;
	private DatabaseHelper databaseHelper;

	private TestRunnerHelper(String className) {
		throwExceptionOnInvalidClassName(className);
		if (singletonTestRunnerHelper != null) {
			throw new IllegalStateException(String.format(
					"Test runner is not properly closed by class %s.",

					lastInstantiatedByClassName));
		}
		singletonTestRunnerHelper = this;
		lastInstantiatedByClassName = className;
		activity = new Activity();
		new Util(activity);
	}

	public static void setup(String className) {
		throwExceptionOnInvalidClassName(className);
		// if (singletonTestRunnerHelper != null &&
		// className.equals(lastInstantiatedByClassName)) {
		// return;
		// }
		singletonTestRunnerHelper = new TestRunnerHelper(className);
		singletonTestRunnerHelper.setupDatabase();
	}

	private void setupDatabase() {
		databaseHelper = DatabaseHelper.getInstance(activity);
	}

	public static void tearDown() {
		if (singletonTestRunnerHelper != null) {
			singletonTestRunnerHelper.tearDownDatabase();
		}
		singletonTestRunnerHelper = null;
		lastInstantiatedByClassName = null;
	}

	private void tearDownDatabase() {
		if (databaseHelper != null) {
			// Closing the database helper ensures that the next test will use a
			// new DatabaseHelper instance with a new SQLite database connection
			// and an empty database.
			databaseHelper.close();
			databaseHelper = null;
		}
	}

	private static boolean throwExceptionOnInvalidClassName(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(String.format(
					"Invalid class name %s.", className), e);
		}
	}

	public static Activity getActivity() {
		return singletonTestRunnerHelper.activity;
	}

	public static DatabaseHelper getDatabaseHelper() {
		return singletonTestRunnerHelper.databaseHelper;
	}

	public static void closeDatabaseHelper() {
		singletonTestRunnerHelper.tearDownDatabase();
	}
}
