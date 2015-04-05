package net.mathdoku.plus.util.fileprovider;

public class AvoidableMovesChartFileType extends FileType {
    public static final String NAME = "avoidable_moves.png";

    public AvoidableMovesChartFileType() {
        super(NAME, pngImageMimeType());
    }
}
