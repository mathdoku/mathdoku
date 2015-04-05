package net.mathdoku.plus.util.fileprovider;

public class LogFileType extends FileType {
    public static final String NAME = "device_info_and_settings.txt";

    public LogFileType() {
        super(NAME, plainTextMimeType());
    }
}
