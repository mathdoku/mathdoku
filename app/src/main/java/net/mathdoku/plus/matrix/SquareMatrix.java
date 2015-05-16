package net.mathdoku.plus.matrix;

/**
 * This class implements a two dimensional matrix of the given type. The matrix is always a square (it contains the same
 * number as rows as columns).
 *
 * @param <T>
 */
public class SquareMatrix<T> extends Matrix<T> {
    private final int size;
    private final boolean transposedFromOriginal;

    private SquareMatrix(int size, T emptyValue, boolean transposed) {
        super(size, size, emptyValue);
        this.size = size;
        transposedFromOriginal = transposed;
    }

    public SquareMatrix(int size, T emptyValue) {
        this(size, emptyValue, false);
    }

    public SquareMatrix<T> createTransposedMatrix() {
        SquareMatrix<T> transposedSquareMatrix = new SquareMatrix<T>(size, getEmptyValue(), !isTransposed());
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                transposedSquareMatrix.setValueToRowColumn(get(row, column), column, row);
            }
        }
        return transposedSquareMatrix;
    }

    public int size() {
        return size;
    }

    public boolean isTransposed() {
        return transposedFromOriginal;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SquareMatrix{");
        sb.append("size=")
                .append(size);
        sb.append(", transposedFromOriginal=")
                .append(transposedFromOriginal);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SquareMatrix)) {
            return false;
        }

        SquareMatrix that = (SquareMatrix) o;

        if (size != that.size) {
            return false;
        }
        if (transposedFromOriginal != that.transposedFromOriginal) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = size;
        result = 31 * result + (transposedFromOriginal ? 1 : 0);
        return result;
    }
}