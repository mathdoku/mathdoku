package net.cactii.mathdoku.storage.database;

import net.cactii.mathdoku.SingletonInstanceNotInstantiated;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * The DatabaseHelper is a generic access point for this application to
 * communicate with one SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	static final String TAG = "MathDoku.DatabaseHelper";

	public static final String DATABASE_NAME = "MathDoku.sqlite";

	private static DatabaseHelper mDatabaseHelperSingletonInstance = null;
	private static Context currentRenamingDelegatingContext = null;

	/**
	 * Constructor should be private to prevent direct instantiation. Call the
	 * static factory method "getInstance()" instead.
	 * 
	 * @param context
	 *            : The context in which the database helper is needed.
	 */
	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, getVersion(context));
	}

	/**
	 * Gets a (singular) instance of this SQLite database. The entire
	 * application context will use the same database helper in order to avoid
	 * locking problems.
	 * 
	 * @param context
	 *            The context in which the database helper is needed. It is not
	 *            necessarily needed to pass the application context itself.
	 * @return The (singular) instance to the SQLite database.
	 */
	public static DatabaseHelper getInstance(Context context) {
		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		if (mDatabaseHelperSingletonInstance == null) {
			mDatabaseHelperSingletonInstance = new DatabaseHelper(
					context.getApplicationContext());
		}

		if (context.getClass().getSimpleName()
				.equals("RenamingDelegatingContext")) {
			// While running as JUnit test we don't want to return the
			// application context of the JUnit test in case a
			// RenamingDelegatingContext is used as parameter for the
			// DatabaseHelper. We can however not test with
			// "context instanceof RenamingDelegatingContext" as this results in
			// an error when not running as JUnit test because this class does
			// not exist in normal running mode.
			if (mDatabaseHelperSingletonInstance == null
					|| context != currentRenamingDelegatingContext) {
				// When switching from RenamingDelegatingContext, i.e. a
				// new JUnit test-case is started, return a new Database
				// Helper.
				mDatabaseHelperSingletonInstance = new DatabaseHelper(context);
				currentRenamingDelegatingContext = context;
			}
		}

		return mDatabaseHelperSingletonInstance;
	}

	/**
	 * Gets the singleton reference to the GridPainter object. If it does not
	 * yet exist an exception will be thrown.
	 * 
	 * @return The singleton reference to the GridPainter object.
	 */
	public static DatabaseHelper getInstance() {
		if (mDatabaseHelperSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mDatabaseHelperSingletonInstance;
	}
	
	@Override
	public synchronized void close() {
		super.close();
		mDatabaseHelperSingletonInstance = null;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		GridDatabaseAdapter.create(db);
		StatisticsDatabaseAdapter.create(db);

		// Enable foreign key constraints
		db.execSQL("PRAGMA foreign_keys=ON;");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		GridDatabaseAdapter.upgrade(db, oldVersion, newVersion);
		StatisticsDatabaseAdapter.upgrade(db, oldVersion, newVersion);
	}

	/**
	 * Get the version (revision) number of the app.
	 * 
	 * @param context
	 *            Context from which the version has to eb determined.
	 */
	private static int getVersion(Context context) {
		int version = -1;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			version = pi.versionCode;
		} catch (Exception e) {
			Log.e(TAG, "Package name '" + context.getPackageName()
					+ "' not found", e);
		}
		return version;
	}
}