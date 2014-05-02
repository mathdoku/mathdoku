package net.mathdoku.plus.storage.databaseadapter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import net.mathdoku.plus.config.Config;
import net.mathdoku.plus.util.SingletonInstanceNotInstantiated;

/**
 * The DatabaseHelper is a generic access point for this application to
 * communicate with one SQLite database.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	@SuppressWarnings("unused")
	private static final String TAG = DatabaseHelper.class.getName();

	private static final String DATABASE_NAME = "MathDoku.sqlite";

	private static DatabaseHelper mDatabaseHelperSingletonInstance = null;

	// The Objects Creator is responsible for creating all new objects needed by
	// this class. For unit testing purposes the default create methods can be
	// overridden if needed.
	public static class ObjectsCreator {
		public DatabaseHelper createDatabaseHelper(Context context) {
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
	 * Gets the singleton reference of {@link DatabaseHelper}. If it does not
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
					.createDatabaseHelper(context.getApplicationContext());
		}
		return mDatabaseHelperSingletonInstance;
	}

	/**
	 * Creates new instance of {@link DatabaseHelper}. All objects in this class
	 * will be created with the given DatabaseHelper.ObjectsCreator. This method
	 * is intended for unit testing.
	 * 
	 * @param context
	 *            The context in which the Preference object is created.
	 * @param objectsCreator
	 *            The DatabaseHelper.ObjectsCreator to be used by this class.
	 *            Only create methods for which the default implementation does
	 *            not suffice, should be overridden.
	 * @return The singleton instance for the Preferences.
	 */
	public static DatabaseHelper getInstance(Context context,
			DatabaseHelper.ObjectsCreator objectsCreator) {
		if (objectsCreator != null) {
			mDatabaseHelperSingletonInstance = objectsCreator
					.createDatabaseHelper(context.getApplicationContext());
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
		for (DatabaseAdapter databaseAdapter : DatabaseAdapter
				.getAllDatabaseAdapters(db)) {
			databaseAdapter.createTable();
		}

		// Enable foreign key constraints
		db.execSQL("PRAGMA foreign_keys=ON;");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		for (DatabaseAdapter databaseAdapter : DatabaseAdapter
				.getAllDatabaseAdapters(db)) {
			databaseAdapter.upgradeTable(oldVersion, newVersion);
		}
	}

	public static boolean hasChangedTableDefinitions() {
		for (DatabaseAdapter databaseAdapter : DatabaseAdapter
				.getAllDatabaseAdapters(mDatabaseHelperSingletonInstance
						.getReadableDatabase())) {
			if (databaseAdapter.isTableDefinitionChanged()) {
				return true;
			}
		}
		return false;
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

	public void deleteDatabase(Context context) {
		if (Config.mAppMode == Config.AppMode.DEVELOPMENT && context != null) {
			context.deleteDatabase(DATABASE_NAME);
		}
	}
}
