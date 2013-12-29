package net.mathdoku.plus.storage.database;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.mathdoku.plus.util.SingletonInstanceNotInstantiated;

/**
 * The DatabaseHelper is a generic access point for this application to
 * communicate with one SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "MathDoku.DatabaseHelper";

	public static final String DATABASE_NAME = "MathDoku.sqlite";

	private static DatabaseHelper mDatabaseHelperSingletonInstance = null;
	private static Context currentRenamingDelegatingContext = null;

	// The Objects Creator is responsible for creating all new objects needed by
	// this class. For unit testing purposes the default create methods can be
	// overridden if needed.
	public static class ObjectsCreator {
		public DatabaseHelper createDatabaseHelperSingletonInstance(
				Context context) {
			return new DatabaseHelper(context);
		}
	}

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
	 * Gets the singleton reference of
	 * {@link net.mathdoku.plus.storage.database.DatabaseHelper}. If it does not
	 * yet exist then it will be created.
	 * 
	 * @param context
	 *            The context in which the GridPainter is created.
	 * @return The context for which the preferences have to be determined.
	 */
	public static DatabaseHelper getInstance(Context context) {
		if (mDatabaseHelperSingletonInstance == null) {
			// Only the first time this method is called, the object will be
			// created.
			//
			// The application context is used to instantiate the SQLite
			// database in
			// order to avoid locking problems and leaking an Activity's
			// context.
			mDatabaseHelperSingletonInstance = new ObjectsCreator()
					.createDatabaseHelperSingletonInstance(context
							.getApplicationContext());
		}
		return mDatabaseHelperSingletonInstance;
	}

	/**
	 * Creates new instance of
	 * {@link net.mathdoku.plus.storage.database.DatabaseHelper}. All objects in
	 * this class will be created with the given ObjectsCreator. This method is
	 * intended for unit testing.
	 * 
	 * @param context
	 *            The context in which the Preference object is created.
	 * @param objectsCreator
	 *            The ObjectsCreator to be used by this class. Only create
	 *            methods for which the default implementation does not suffice,
	 *            should be overridden.
	 * @return The singleton instance for the Preferences.
	 */
	public static DatabaseHelper getInstance(Context context,
			ObjectsCreator objectsCreator) {
		if (objectsCreator != null) {
			mDatabaseHelperSingletonInstance = objectsCreator
					.createDatabaseHelperSingletonInstance(context
							.getApplicationContext());
		}
		return getInstance(context);
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
	 */
	public void beginTransaction() {
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
	 */
	public void endTransaction() {
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
	 */
	public void setTransactionSuccessful() {
		if (mDatabaseHelperSingletonInstance == null) {
			throw new SingletonInstanceNotInstantiated();
		}
		SQLiteDatabase sqliteDatabase = mDatabaseHelperSingletonInstance
				.getWritableDatabase();
		if (sqliteDatabase != null) {
			sqliteDatabase.setTransactionSuccessful();
		}
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
		LeaderboardRankDatabaseAdapter.create(db);

		// Enable foreign key constraints
		db.execSQL("PRAGMA foreign_keys=ON;");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		GridDatabaseAdapter.upgrade(db, oldVersion, newVersion);
		SolvingAttemptDatabaseAdapter.upgrade(db, oldVersion, newVersion);
		StatisticsDatabaseAdapter.upgrade(db, oldVersion, newVersion);
		LeaderboardRankDatabaseAdapter.upgrade(db, oldVersion, newVersion);
	}

	public static boolean hasChangedTableDefinitions() {
		return new GridDatabaseAdapter().isTableDefinitionChanged()
				|| new StatisticsDatabaseAdapter().isTableDefinitionChanged()
				|| new SolvingAttemptDatabaseAdapter()
						.isTableDefinitionChanged()
				|| new LeaderboardRankDatabaseAdapter()
						.isTableDefinitionChanged();
	}

	/**
	 * Get the version (revision) number of the app.
	 * 
	 * @param context
	 *            Context from which the version has to eb determined.
	 */
	@SuppressWarnings("ConstantConditions")
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