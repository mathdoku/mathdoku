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
	 * Gets the singleton reference to the DatabaseHelper object. If it does not
	 * yet exist an exception will be thrown.
	 * 
	 * @return The singleton reference to the DatabaseHelper object.
	 */
	public static DatabaseHelper getInstance() {
		if (mDatabaseHelperSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mDatabaseHelperSingletonInstance;
	}

	/**
	 * Gets the (writeable) database connected to this DatabaseHelper object. As
	 * it is a shortcut for DatabaseHelper.getInstance().getWriteableDatabase()
	 * you have to be sure that the DatabaseHelper has been instantiated before.
	 * 
	 * @return The SQLiteDatabase. Null in case of an error.
	 */
	public static SQLiteDatabase getDatabase() {
		if (mDatabaseHelperSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		return mDatabaseHelperSingletonInstance.getWritableDatabase();
	}

	/**
	 * Begin a transaction for the database connected to this DatabaseHelper
	 * object. As it is a shortcut for
	 * DatabaseHelper.getInstance().getWriteableDatabase().beginTransaction()
	 * you have to be sure that the DatabaseHelper has been instantiated before.
	 * 
	 * @return The SQLiteDatabase. Null in case of an error.
	 */
	public static void beginTransaction() {
		if (mDatabaseHelperSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		SQLiteDatabase sqliteDatabase = mDatabaseHelperSingletonInstance
				.getWritableDatabase();
		if (sqliteDatabase != null) {
			sqliteDatabase.beginTransaction();
		}
	}

	/**
	 * End a transaction for the database connected to this DatabaseHelper
	 * object. As it is a shortcut for
	 * DatabaseHelper.getInstance().getWriteableDatabase().endTransaction() you
	 * have to be sure that the DatabaseHelper has been instantiated before.
	 * 
	 * @return The SQLiteDatabase. Null in case of an error.
	 */
	public static void endTransaction() {
		if (mDatabaseHelperSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		SQLiteDatabase sqliteDatabase = mDatabaseHelperSingletonInstance
				.getWritableDatabase();
		if (sqliteDatabase != null) {
			sqliteDatabase.endTransaction();
		}
	}

	/**
	 * Commit a transaction for the database connected to this DatabaseHelper
	 * object. As it is a shortcut for
	 * DatabaseHelper.getInstance().getWriteableDatabase
	 * ().setTransactionSuccessful() you have to be sure that the DatabaseHelper
	 * has been instantiated before.
	 * 
	 * @return The SQLiteDatabase. Null in case of an error.
	 */
	public static boolean setTransactionSuccessful() {
		if (mDatabaseHelperSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		SQLiteDatabase sqliteDatabase = mDatabaseHelperSingletonInstance
				.getWritableDatabase();
		if (sqliteDatabase != null) {
			sqliteDatabase.setTransactionSuccessful();
			return true;
		}

		return false;
	}

	@Override
	public synchronized void close() {
		super.close();
		mDatabaseHelperSingletonInstance = null;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		GridDatabaseAdapter.create(db);
		SolvingAttemptDatabaseAdapter.create(db);
		StatisticsDatabaseAdapter.create(db);

		// Enable foreign key constraints
		db.execSQL("PRAGMA foreign_keys=ON;");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		GridDatabaseAdapter.upgrade(db, oldVersion, newVersion);
		SolvingAttemptDatabaseAdapter.upgrade(db, oldVersion, newVersion);
		StatisticsDatabaseAdapter.upgrade(db, oldVersion, newVersion);
	}

	public static boolean hasChangedTableDefinitions() {
		return new GridDatabaseAdapter().isTableDefinitionChanged()
				|| new StatisticsDatabaseAdapter().isTableDefinitionChanged()
				|| new SolvingAttemptDatabaseAdapter()
						.isTableDefinitionChanged();
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