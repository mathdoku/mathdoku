package net.mathdoku.plus.util.fileprovider;

public class CheatsChartFileType extends FileType {
    public static final String NAME = "cheats.png";

    public CheatsChartFileType() {
        super(NAME, pngImageMimeType());
    }
}
