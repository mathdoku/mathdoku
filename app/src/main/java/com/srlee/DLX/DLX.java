package com.srlee.DLX;

import java.util.ArrayList;

import android.util.Log;

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
	private ArrayList<Integer> trySolution;
	private ArrayList<Integer> foundSolution;
	private int countSolutions;
	private boolean isValid;
	private int previousRowIndex = -1;
	private SolveType solveType;
	int complexity;

	DLX() {
		trySolution = new ArrayList<Integer>();
		isValid = true;
	}

	void Init(int numCols, int numNodes) {
		colHeaders = new DLXColumn[numCols + 1];
		for (int c = 1; c <= numCols; c++)
			colHeaders[c] = new DLXColumn();

		nodes = new DLXNode[numNodes + 1];
		this.numNodes = 0; // None allocated

		DLXColumn prev = root;
		for (int i = 1; i <= numCols; i++) {
			prev.SetRight(colHeaders[i]);
			colHeaders[i].SetLeft(prev);
			prev = colHeaders[i];
		}
		root.SetLeft(colHeaders[numCols]);
		colHeaders[numCols].SetRight(root);
	}

	int GetRowsInSolution() {
		return foundSolution.size();
	}

	int GetSolutionRow(int row) {
		return foundSolution.get(row - 1);
	}

	private void CoverCol(DLXColumn coverCol) {
		LL2DNode i, j;
		coverCol.GetRight().SetLeft(coverCol.GetLeft());
		coverCol.GetLeft().SetRight(coverCol.GetRight());

		i = coverCol.GetDown();
		while (i != coverCol) {
			j = i.GetRight();
			while (j != i) {
				j.GetDown().SetUp(j.GetUp());
				j.GetUp().SetDown(j.GetDown());
				((DLXNode) j).GetColumn().DecSize();
				j = j.GetRight();
			}
			i = i.GetDown();
		}
	}

	private void UncoverCol(DLXColumn uncoverCol) {
		LL2DNode i, j;

		i = uncoverCol.GetUp();
		while (i != uncoverCol) {
			j = i.GetLeft();
			while (j != i) {
				((DLXNode) j).GetColumn().IncSize();
				j.GetDown().SetUp(j);
				j.GetUp().SetDown(j);
				j = j.GetLeft();
			}
			i = i.GetUp();
		}
		uncoverCol.GetRight().SetLeft(uncoverCol);
		uncoverCol.GetLeft().SetRight(uncoverCol);
	}

	private DLXColumn ChooseMinCol() {
		int minSize = Integer.MAX_VALUE;
		DLXColumn search, minColumn;

		minColumn = search = (DLXColumn) root.GetRight();

		while (search != root) {
			if (search.GetSize() < minSize) {
				minColumn = search;
				minSize = minColumn.GetSize();
				if (minSize == 0) {
					break;
				}
			}
			search = (DLXColumn) search.GetRight();
		}
		if (minSize == 0)
			return null;
		else
			return minColumn;
	}

	void AddNode(int columnIndex, int rowIndex) {
		nodes[++numNodes] = new DLXNode(colHeaders[columnIndex], rowIndex);
		if (previousRowIndex == rowIndex) {
			nodes[numNodes].SetLeft(lastNodeAdded);
			nodes[numNodes].SetRight(lastNodeAdded.GetRight());
			lastNodeAdded.SetRight(nodes[numNodes]);
			nodes[numNodes].GetRight().SetLeft(nodes[numNodes]);
		} else {
			previousRowIndex = rowIndex;
			nodes[numNodes].SetLeft(nodes[numNodes]);
			nodes[numNodes].SetRight(nodes[numNodes]);
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
	int Solve(SolveType solveType) {
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
		if (root.GetRight() == root) {
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

		chosenCol = ChooseMinCol();
		if (chosenCol != null) {

			CoverCol(chosenCol);
			r = chosenCol.GetDown();

			while (r != chosenCol) {
				if (k >= trySolution.size())
					trySolution.add(((DLXNode) r).GetRowIdx());
				else
					trySolution.set(k, ((DLXNode) r).GetRowIdx());
				j = r.GetRight();
				while (j != r) {
					CoverCol(((DLXNode) j).GetColumn());
					j = j.GetRight();
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
				j = r.GetLeft();
				while (j != r) {
					UncoverCol(((DLXNode) j).GetColumn());
					j = j.GetLeft();
				}
				r = r.GetDown();
			}
			UncoverCol(chosenCol);
		}
	}
}
