package com.srlee.dlx;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DLX {
	private static final String TAG = "MathDoku.DLX";

	public enum SolveType {
		ONE, MULTIPLE, ALL
	}

	private final DLXColumn root = new DLXColumn();
	private DLXColumn[] colHeaders;
	private DLXNode[] nodes;
	private int numNodes;
	private DLXNode lastNodeAdded;
	private List<Integer> trySolution;
	private List<Integer> foundSolution;
	private int countSolutions;
	private boolean isValid;
	private int previousRowIndex = -1;
	private SolveType solveType;
	int complexity;

	DLX() {
		trySolution = new ArrayList<Integer>();
		isValid = true;
	}

	void init(int numCols, int numNodes) {
		colHeaders = new DLXColumn[numCols + 1];
		for (int c = 1; c <= numCols; c++)
			colHeaders[c] = new DLXColumn();

		nodes = new DLXNode[numNodes + 1];
		this.numNodes = 0; // None allocated

		DLXColumn prev = root;
		for (int i = 1; i <= numCols; i++) {
			prev.setRight(colHeaders[i]);
			colHeaders[i].setLeft(prev);
			prev = colHeaders[i];
		}
		root.setLeft(colHeaders[numCols]);
		colHeaders[numCols].setRight(root);
	}

	int getRowsInSolution() {
		return foundSolution.size();
	}

	int getSolutionRow(int row) {
		return foundSolution.get(row - 1);
	}

	private void coverCol(DLXColumn coverCol) {
		LL2DNode i, j;
		coverCol.getRight().setLeft(coverCol.getLeft());
		coverCol.getLeft().setRight(coverCol.getRight());

		i = coverCol.getDown();
		while (i != coverCol) {
			j = i.getRight();
			while (j != i) {
				j.getDown().setUp(j.getUp());
				j.getUp().setDown(j.getDown());
				((DLXNode) j).getColumn().decreaseSize();
				j = j.getRight();
			}
			i = i.getDown();
		}
	}

	private void uncoverCol(DLXColumn uncoverCol) {
		LL2DNode i, j;

		i = uncoverCol.getUp();
		while (i != uncoverCol) {
			j = i.getLeft();
			while (j != i) {
				((DLXNode) j).getColumn().increaseSize();
				j.getDown().setUp(j);
				j.getUp().setDown(j);
				j = j.getLeft();
			}
			i = i.getUp();
		}
		uncoverCol.getRight().setLeft(uncoverCol);
		uncoverCol.getLeft().setRight(uncoverCol);
	}

	private DLXColumn chooseMinCol() {
		int minSize = Integer.MAX_VALUE;
		DLXColumn search, minColumn;

		minColumn = search = (DLXColumn) root.getRight();

		while (search != root) {
			if (search.getSize() < minSize) {
				minColumn = search;
				minSize = minColumn.getSize();
				if (minSize == 0) {
					break;
				}
			}
			search = (DLXColumn) search.getRight();
		}
		if (minSize == 0)
			return null;
		else
			return minColumn;
	}

	void addNode(int columnIndex, int rowIndex) {
		nodes[++numNodes] = new DLXNode(colHeaders[columnIndex], rowIndex);
		if (previousRowIndex == rowIndex) {
			nodes[numNodes].setLeft(lastNodeAdded);
			nodes[numNodes].setRight(lastNodeAdded.getRight());
			lastNodeAdded.setRight(nodes[numNodes]);
			nodes[numNodes].getRight().setLeft(nodes[numNodes]);
		} else {
			previousRowIndex = rowIndex;
			nodes[numNodes].setLeft(nodes[numNodes]);
			nodes[numNodes].setRight(nodes[numNodes]);
		}
		lastNodeAdded = nodes[numNodes];
	}

	/**
	 * Determines the number of solutions that can be found for this grid.
	 * 
	 * @param solveType
	 *            The solve type to be used to determine the number of
	 *            solutions.
	 * @return The number of solutions, given the solution type, that can be
	 *         found for this grid.
	 */
	@SuppressWarnings("SameParameterValue")
	int solve(SolveType solveType) {
		if (!isValid)
			return -1;

		this.solveType = solveType;
		countSolutions = 0;
		complexity = 0;
		search(trySolution.size());
		return countSolutions;
	}

	private void search(int k) {
		DLXColumn chosenCol;
		LL2DNode r, j;

		// A solution is found in case all columns are covered
		if (root.getRight() == root) {
			countSolutions++;
			foundSolution = new ArrayList<Integer>(trySolution);
			if (MathDokuDLX.DEBUG_DLX) {
				Log.i(TAG, "Solution " + countSolutions
						+ " found which consists of following moves: "
						+ trySolution.toString());
			}
			return;
		}

		// In case no solution is yet found, select the next column to be
		// covered. Now two things can happen. Either such a column can be
		// found, and the puzzle solving will be taken one level deeper. Or such
		// a column can not be found in which case a backtrack will be done. The
		// more often a permutation is tried, the harder to solve the puzzle is.
		complexity++;

		chosenCol = chooseMinCol();
		if (chosenCol != null) {

			coverCol(chosenCol);
			r = chosenCol.getDown();

			while (r != chosenCol) {
				if (k >= trySolution.size())
					trySolution.add(((DLXNode) r).getRowIdx());
				else
					trySolution.set(k, ((DLXNode) r).getRowIdx());
				j = r.getRight();
				while (j != r) {
					coverCol(((DLXNode) j).getColumn());
					j = j.getRight();
				}
				search(k + 1);
				if (solveType == SolveType.ONE && countSolutions > 0) {
					// Stop as soon as we find 1 solution
					return;
				}
				if (solveType == SolveType.MULTIPLE && countSolutions > 1) {
					// Stop as soon as we find multiple solutions
					return;
				}
				j = r.getLeft();
				while (j != r) {
					uncoverCol(((DLXNode) j).getColumn());
					j = j.getLeft();
				}
				r = r.getDown();
			}
			uncoverCol(chosenCol);
		}
	}
}
