package net.mathdoku.plus.storage.databaseadapter.database;

public class DatabaseException extends RuntimeException {
    public DatabaseException() {
        super();
    }

    public DatabaseException(String errorMessage) {
        super(errorMessage);
    }

    public DatabaseException(String errorMessage, Throwable throwable) {
        super(errorMessage, throwable);
    }
}
