package net.mathdoku.plus.util;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import net.mathdoku.plus.config.Config;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * The file provider can only be used to get access to files which have to be send as email attachment. Beware that
 * files may not include personal data as all apps can access this provider.
 */
public class FileProvider extends ContentProvider {
    private static final String PROVIDER_NAME = FileProvider.class.getName();

    // Supported files.
    private static final int FEEDBACK_LOG_ID = 1;
    public static final String FEEDBACK_LOG_FILE_NAME = "device_info_and_settings.txt";

    private static final int SCREENDUMP_ID = 2;
    public static final String SCREENDUMP_FILE_NAME = "screendump.png";

    private static final int AVOIDABLE_MOVES_CHART_ID = 3;
    public static final String AVOIDABLE_MOVES_CHART_FILE_NAME = "avoidable_moves.png";

    private static final int CHEATS_CHART_ID = 4;
    public static final String CHEATS_CHART_FILE_NAME = "cheats.png";

    private static final int DATABASE_ID = 5;
    public static final String DATABASE_FILE_NAME = "MathDoku.sqlite";

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, FEEDBACK_LOG_FILE_NAME, FEEDBACK_LOG_ID);
        uriMatcher.addURI(PROVIDER_NAME, SCREENDUMP_FILE_NAME, SCREENDUMP_ID);
        uriMatcher.addURI(PROVIDER_NAME, AVOIDABLE_MOVES_CHART_FILE_NAME, AVOIDABLE_MOVES_CHART_ID);
        uriMatcher.addURI(PROVIDER_NAME, CHEATS_CHART_FILE_NAME, CHEATS_CHART_ID);
        if (Config.APP_MODE == Config.AppMode.DEVELOPMENT) {
            uriMatcher.addURI(PROVIDER_NAME, DATABASE_FILE_NAME, DATABASE_ID);
        }
    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {
        throwUnsupportedOperationException();
        return 0;
    }

    private void throwUnsupportedOperationException() {
        throw new UnsupportedOperationException(PROVIDER_NAME + " does not support this operation.");
    }

    @Override
    public String getType(Uri uri) {
        if (uriMatcher.match(uri) == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // For a URI which matched an allowed URI, the corresponding mime type is returned.
        switch (uriMatcher.match(uri)) {
            case FEEDBACK_LOG_ID:
                return "text/plain";
            case SCREENDUMP_ID:
                return "image/png";
            case AVOIDABLE_MOVES_CHART_ID:
                return "image/png";
            case CHEATS_CHART_ID:
                return "image/png";
            default:
                return "*/*";
        }
    }

    @Override
    public Uri insert(Uri arg0, ContentValues arg1) {
        throwUnsupportedOperationException();
        return null;
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3, String arg4) {
        if (uri == null || uriMatcher.match(uri) == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // For a URI which matched an allowed URI, a cursor containing the name
        // and size of the file is returned,
        MatrixCursor cursor = null;

        // Check if dir exists
        // noinspection ConstantConditions
        File dir = getContext().getFilesDir();
        if (dir == null || !dir.exists()) {
            return null;
        }

        File file = new File(dir, uri.getLastPathSegment());
        if (file.exists()) {
            cursor = new MatrixCursor(new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE});
            cursor.addRow(new Object[]{uri.getLastPathSegment(), file.length()});
        }

        return cursor;
    }

    @Override
    public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
        throwUnsupportedOperationException();
        return 0;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (uri == null || uriMatcher.match(uri) == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // Check if dir exists
        // noinspection ConstantConditions
        File dir = getContext().getFilesDir();
        if (dir == null || !dir.exists()) {
            return null;
        }

        // For a URI which matched an allowed URI, the file is provided as
        // read only.
        File file = new File(dir, uri.getLastPathSegment());
        if (file.exists()) {
            return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        }
        throw new FileNotFoundException(uri.getPath());
    }

    /**
     * Get the uri which can be used to retrieve the file via the content provider.
     *
     * @param filename
     *         The filename for which the uri has to be determined.
     * @return A uri in case the filename can be provided by this provider. Null otherwise.
     */
    public static Uri getUri(String filename) {
        Uri uri = Uri.parse("content://" + PROVIDER_NAME + "/" + filename);

        if (uriMatcher.match(uri) == UriMatcher.NO_MATCH) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        // For filename which matches an allowed URI, the uri is returned.
        return uri;
    }
}
