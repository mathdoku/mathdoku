package net.mathdoku.plus.sharedpuzzle;

import android.net.Uri;

import net.mathdoku.plus.util.Util;

import java.util.List;

public abstract class ShareUri {
    private static final int PATH_SEGMENT_PREFIX = 0;
    private static final int PATH_SEGMENT_VERSION = 1;
    private static final int PATH_SEGMENT_GRID_DEFINITION = 2;
    private static final int PATH_SEGMENT_HASH_CODE = 3;

    private final String scheme;
    private final String host;
    private String prefix;
    private String version;

    public ShareUri(String scheme, String host, String prefix, int version) {
        if (Util.isNullOrEmpty(scheme)) {
            throw new IllegalStateException("Scheme can not be null or empty");
        }
        this.scheme = scheme;

        if (Util.isNullOrEmpty(host)) {
            throw new IllegalStateException("Host can not be null or empty");
        }
        this.host = host;

        if (Util.isNullOrEmpty(prefix)) {
            throw new IllegalStateException("Prefix can not be null or empty");
        }
        this.prefix = prefix;

        this.version = Integer.toString(version);

        // Strip slash of start of prefix
        if (this.prefix.charAt(0) == '/') {
            this.prefix = this.prefix.substring(1);
        }
    }

    public String getShareUrl(String gridDefinition) {
        return scheme + "://" + host + "/" +
                prefix + "/" + version + "/" + gridDefinition +
                "/" + gridDefinition.hashCode();
    }

    public boolean matches(Uri uri) {
        if (uri == null) {
            return false;
        }

        if (!scheme.equals(uri.getScheme()) || !host.equals(uri.getHost())) {
            return false;
        }

        if (!prefix.equals(getPathSegmentFromUri(uri, PATH_SEGMENT_PREFIX))) {
            return false;
        }

        if (!version.equals(getPathSegmentFromUri(uri, PATH_SEGMENT_VERSION))) {
            return false;
        }

        return true;
    }

    private String getPathSegmentFromUri(Uri uri, int index) {
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments == null || index < 0 || index >= pathSegments.size()) {
            return null;
        }
        return pathSegments.get(index);
    }


    public String getGridDefinitionFromUri(Uri uri) {
        if (!matches(uri)) {
            throw new IllegalStateException(String.format("Method called with an invalid uri '%s'", uri));
        }

        return isValidHasCodeInUri(uri) ? getPathSegmentFromUri(uri, PATH_SEGMENT_GRID_DEFINITION) : null;
    }

    private boolean isValidHasCodeInUri(Uri uri) {
        // This is only a very simple measure to check if the uri is complete and not manually changed by an ordinary
        // user. It it still possible to manually manipulate the grid definition and the hash code but this can do no
        // harm as it is still checked whether a valid grid is specified.
        return getPathSegmentFromUri(uri, PATH_SEGMENT_GRID_DEFINITION).hashCode() == Integer.valueOf(
                getPathSegmentFromUri(uri, PATH_SEGMENT_HASH_CODE));
    }
}
