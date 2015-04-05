package net.mathdoku.plus.util.fileprovider;

public class DatabaseFileType extends FileType {
    public static final String NAME = "MathDoku.sqlite";

    public DatabaseFileType() {
        super(NAME, unknownMimeType());
    }
}
