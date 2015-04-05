package net.mathdoku.plus.util.fileprovider;

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
import java.util.ArrayList;
import java.util.List;

/**
 * The file provider can only be used to get access to files which have to be send as email attachment. Beware that
 * files may not include personal data as all apps can access this provider.
 */
public class FileProvider extends ContentProvider {
    private static final String PROVIDER_NAME = FileProvider.class.getName();

    private static final List<FileType> fileTypes = getSupportedFileTypes();
    private static final UriMatcher uriMatcher = initUriMatcher(fileTypes);

    private static List<FileType> getSupportedFileTypes() {
        List<FileType> fileTypeList = new ArrayList<FileType>();

        fileTypeList.add(new LogFileType());
        fileTypeList.add(new ScreendumpFileType());
        fileTypeList.add(new AvoidableMovesChartFileType());
        fileTypeList.add(new CheatsChartFileType());
        if (Config.APP_MODE == Config.AppMode.DEVELOPMENT) {
            fileTypeList.add(new DatabaseFileType());
        }

        return fileTypeList;
    }

    private static UriMatcher initUriMatcher(List<FileType> fileTypes) {
        UriMatcher uriMatcherResult = new UriMatcher(UriMatcher.NO_MATCH);

        int index = 0;
        for (FileType fileType : fileTypes) {
            uriMatcherResult.addURI(PROVIDER_NAME, fileType.getName(), index);
            index++;
        }

        return uriMatcherResult;
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
            return unsupportedUri(uri);
        }

        // For a URI which matched an allowed URI, the corresponding mime type is returned.
        return fileTypes.get(uriMatcher.match(uri)).getMimeType();
    }

    private static String unsupportedUri(Uri uri) {
        throw new IllegalArgumentException("Unsupported URI: " + uri);
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
            unsupportedUri(uri);
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
            unsupportedUri(uri);
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
            unsupportedUri(uri);
        }

        // For filename which matches an allowed URI, the uri is returned.
        return uri;
    }
}
