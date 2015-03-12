package net.mathdoku.plus.storage.databaseadapter;

import net.mathdoku.plus.gridgenerating.GridGeneratingParameters;

/**
 * Mapping for records in database table Grid
 */
public class GridRow {
    private final int gridId;
    private final String gridDefinition;
    private final int gridSize;
    private final long gridDateCreated;
    private final GridGeneratingParameters gridGeneratingParameters;

    public GridRow(int gridId, String gridDefinition, int gridSize, long gridDateCreated,
                   GridGeneratingParameters gridGeneratingParameters) {
        this.gridId = gridId;
        this.gridDefinition = gridDefinition;
        this.gridSize = gridSize;
        this.gridDateCreated = gridDateCreated;
        this.gridGeneratingParameters = gridGeneratingParameters;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("GridRow{");
        sb.append("gridId=")
                .append(getGridId());
        sb.append(", gridDefinition='")
                .append(getGridDefinition())
                .append('\'');
        sb.append(", gridSize=")
                .append(getGridSize());
        sb.append(", gridDateCreated=")
                .append(getGridDateCreated());
        sb.append(", gridGeneratingParameters=")
                .append(getGridGeneratingParameters().toString());
        sb.append('}');
        return sb.toString();
    }

    @Override
    @SuppressWarnings("all")
    // Needed to suppress sonar warning on cyclomatic complexity
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GridRow)) {
            return false;
        }

        GridRow gridRow = (GridRow) o;

        if (getGridDateCreated() != gridRow.getGridDateCreated()) {
            return false;
        }
        if (getGridSize() != gridRow.getGridSize()) {
            return false;
        }
        if (getGridId() != gridRow.getGridId()) {
            return false;
        }
        if (getGridDefinition() != null ? !getGridDefinition().equals(
                gridRow.getGridDefinition()) : gridRow.getGridDefinition() != null) {
            return false;
        }
        if (getGridGeneratingParameters() != null ? !getGridGeneratingParameters().equals(
                gridRow.getGridGeneratingParameters()) : gridRow.getGridGeneratingParameters() !=
                null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = getGridId();
        result = 31 * result + (getGridDefinition() != null ? getGridDefinition().hashCode() : 0);
        result = 31 * result + getGridSize();
        result = 31 * result + (int) (getGridDateCreated() ^ getGridDateCreated() >>> 32);
        result = 31 * result + (getGridGeneratingParameters() != null ? getGridGeneratingParameters().hashCode() : 0);
        return result;
    }

    public int getGridId() {
        return gridId;
    }

    public String getGridDefinition() {
        return gridDefinition;
    }

    public int getGridSize() {
        return gridSize;
    }

    public long getGridDateCreated() {
        return gridDateCreated;
    }

    public GridGeneratingParameters getGridGeneratingParameters() {
        return gridGeneratingParameters;
    }
}
