package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.gridgenerating.CellCoordinates.CellCoordinates;

import java.util.ArrayList;
import java.util.List;

/**
 * This class implements a two dimensional matrix of the given type. The matrix
 * is always a square (it contains the same number as rows as columns).
 * 
 * @param <T>
 */
public class Matrix<T> {
	private final T[][] cellValue;
	private final int size;
	private final T emptyValue;
	private final boolean transposedFromOriginal;

	private Matrix(int size, T emptyValue, boolean transposed) {
		this.size = size;
		this.emptyValue = emptyValue;
		transposedFromOriginal = transposed;
		// noinspection unchecked,unchecked
		cellValue = (T[][]) new Object[size][size];
	}

	public Matrix(int size, T emptyValue) {
		this(size, emptyValue, false);
		initializeMatrix();
	}

	private void initializeMatrix() {
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				cellValue[row][column] = emptyValue;
			}
		}
	}

	public Matrix<T> createTransposedMatrix() {
		Matrix<T> transposedMatrix = new Matrix<T>(size, emptyValue,
				!isTransposed());
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				transposedMatrix.cellValue[row][column] = this.cellValue[column][row];
			}
		}
		return transposedMatrix;
	}

	public T get(int row, int column) {
		if (isInvalidPosition(row, column)) {
			throw indexOutOfBoundsException(row, column);
		}
		return cellValue[row][column];
	}

	public boolean containsInvalidCellCoordinates(
			CellCoordinates... cellCoordinatesArray) {
		for (CellCoordinates cellCoordinates : cellCoordinatesArray) {
			if (cellCoordinates.isNull()
					|| isInvalidPosition(cellCoordinates.getRow(),
							cellCoordinates.getColumn())) {
				return true;
			}
		}
		return false;
	}

	private boolean isInvalidPosition(int row, int column) {
		return isInvalidIndex(row) || isInvalidIndex(column);
	}

	private boolean isInvalidIndex(int index) {
		return index < 0 || index >= size;
	}

	private IndexOutOfBoundsException indexOutOfBoundsException(int row,
			int column) {
		return new IndexOutOfBoundsException(String.format(
				"Invalid position (%d,%d), size of matrix is %d", row, column,
				size));
	}

	public void setValueToAllCellCoordinates(
			@SuppressWarnings("SameParameterValue") T value,
			CellCoordinates... cellCoordinatesArray) {
		for (CellCoordinates cellCoordinates : cellCoordinatesArray) {
			setValueToCellCoordinates(value, cellCoordinates);
		}
	}

	public void setValueToCellCoordinates(T value,
			CellCoordinates cellCoordinates) {
		setValueToRowColumn(value, cellCoordinates.getRow(),
				cellCoordinates.getColumn());
	}

	public void setValueToRowColumn(T value, int row, int column) {
		if (isInvalidPosition(row, column)) {
			throw indexOutOfBoundsException(row, column);
		}
		cellValue[row][column] = value;
	}

	public List<T> getColumn(int column) {
		List<T> valuesInColumn = new ArrayList<T>();
		for (int row = 0; row < size; row++) {
			valuesInColumn.add(cellValue[row][column]);
		}
		return valuesInColumn;
	}

	public int countValueInColumn(T value, int column) {
		int count = 0;
		for (int row = 0; row < size; row++) {
			if (cellValue[row][column] == value) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Clears the given value from all positions in the solution matrix.
	 */
	public void clearValue(T value) {
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				if (cellValue[row][column] == value) {
					cellValue[row][column] = emptyValue;
				}
			}
		}
	}

	public boolean containsNonEmptyCell(CellCoordinates... cellCoordinatesArray) {
		for (CellCoordinates cellCoordinates : cellCoordinatesArray) {
			if (!isEmpty(cellCoordinates.getRow(), cellCoordinates.getColumn())) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty(int row, int column) {
		if (isInvalidPosition(row, column)) {
			throw indexOutOfBoundsException(row, column);
		}
		return cellValue[row][column] == emptyValue;
	}

	public boolean isNotEmpty(int row, int column) {
		return !isEmpty(row, column);
	}

	public CellCoordinates getCellCoordinatesForFirstEmptyCell() {
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				if (isEmpty(row, column)) {
					return new CellCoordinates(row, column);
				}
			}
		}
		return CellCoordinates.EMPTY;
	}

	public int size() {
		return size;
	}

	public boolean isTransposed() {
		return transposedFromOriginal;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("Matrix{");
		sb.append("size=")
				.append(size);
		sb.append(", emptyValue=")
				.append(emptyValue);
		sb.append(", transposedFromOriginal=")
				.append(transposedFromOriginal);
		sb.append(", matrix=\n");
		for (int row = 0; row < size; row++) {
			sb.append("    ");
			for (int column = 0; column < size; column++) {
				sb.append(cellValue[row][column]).append(" ");
			}
			sb.append("\n");
		}
		sb.append('}');
		return sb.toString();
	}

	@Override
	@SuppressWarnings("all") // Needed to suppress sonar warning on cyclomatic complexity
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Matrix)) {
			return false;
		}

		Matrix matrix = (Matrix) o;

		if (size != matrix.size) {
			return false;
		}
		if (transposedFromOriginal != matrix.transposedFromOriginal) {
			return false;
		}
		if (emptyValue != null ? !emptyValue.equals(
				matrix.emptyValue) : matrix.emptyValue != null) {
			return false;
		}
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				if (!this.cellValue[row][column].equals(matrix.get(row,column))) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = size;
		result = 31 * result + (emptyValue != null ? emptyValue.hashCode() : 0);
		result = 31 * result + (transposedFromOriginal ? 1 : 0);
		for (int row = 0; row < size; row++) {
			result = 31 * result + cellValue[row].hashCode();
		}
		return result;
	}
}
