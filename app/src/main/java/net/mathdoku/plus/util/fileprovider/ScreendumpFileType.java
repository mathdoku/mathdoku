package net.mathdoku.plus.util.fileprovider;

public class ScreendumpFileType extends FileType {
    public static final String NAME = "screendump.png";

    public ScreendumpFileType() {
        super(NAME, pngImageMimeType());
    }
}
