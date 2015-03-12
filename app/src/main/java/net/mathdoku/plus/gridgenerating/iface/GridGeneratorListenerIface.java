package net.mathdoku.plus.gridgenerating.iface;

public interface GridGeneratorListenerIface {
    // Requests the listener whether the generating of this grid has to be
    // cancelled.
    boolean isCancelled();

    // Send a high level progress update to the listener.
    void updateProgressHighLevel(String text);

    // Send a detail level progress update to the listener.
    void updateProgressDetailLevel(String text);

    // Send signal about slow grid generating
    void signalSlowGridGeneration();
}
