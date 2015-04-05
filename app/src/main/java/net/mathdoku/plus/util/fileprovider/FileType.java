package net.mathdoku.plus.util.fileprovider;

public class FileType {
    private final String filename;
    private final String mimeType;

    public FileType(String filename, String mimeType) {
        this.filename = filename;
        this.mimeType = mimeType;
    }

    public String getName() {
        return filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    /* package private */ static String plainTextMimeType() {
        return "text/plain";
    }

    /* package private */ static String pngImageMimeType() {
        return "image/png";
    }

    /* package private */ static String unknownMimeType() {
        return "*/*";
    }
}
