package net.mathdoku.plus.enums;

import java.util.ArrayList;
import java.util.List;

// Allowed values for the status filters
public enum StatusFilter {
    ALL,
    UNFINISHED,
    SOLVED,
    REVEALED;

    public List<SolvingAttemptStatus> getAllAttachedSolvingAttemptStatuses() {
        List<SolvingAttemptStatus> solvingAttemptStatuses = new ArrayList<SolvingAttemptStatus>();
        for (SolvingAttemptStatus solvingAttemptStatus : SolvingAttemptStatus.values()) {
            if (solvingAttemptStatus.getAttachedToStatusFilter() == this) {
                solvingAttemptStatuses.add(solvingAttemptStatus);
            }
        }
        return solvingAttemptStatuses;
    }
}
