package com.srlee.DLX;

import java.util.ArrayList;

import android.util.Log;

public class DLX extends Object {
	private static final String TAG = "MathDoku.DLX";

	public enum SolveType {
		ONE, MULTIPLE, ALL
	};

	private final DLXColumn root = new DLXColumn();
	private DLXColumn[] ColHdrs;
	private DLXNode[] Nodes;
	private DLXRow[] Rows;
	private int numcols, numrows, numnodes;
	private DLXNode lastnodeadded;
	private ArrayList<Integer> trysolution;
	private ArrayList<Integer> foundsolution;
	private int NumSolns;
	protected boolean isValid;
	private int prev_rowidx = -1;
	private SolveType solvetype;
	protected int complexity;

	public DLX() {
		trysolution = new ArrayList<Integer>();
		isValid = true;
	}

	public DLX(int nc, int nr, int nn) {
		Init(nc, nr, nn);
	}

	protected void Init(int nc, int nr, int nn) {
		numcols = nc;
		ColHdrs = new DLXColumn[numcols + 1];
		for (int c = 1; c <= numcols; c++)
			ColHdrs[c] = new DLXColumn();

		Nodes = new DLXNode[nn + 1];
		numnodes = 0; // None allocated

		Rows = new DLXRow[nr + 1];
		numrows = 0; // None allocated

		DLXColumn prev = root;
		for (int i = 1; i <= numcols; i++) {
			prev.SetRight(ColHdrs[i]);
			ColHdrs[i].SetLeft(prev);
			prev = ColHdrs[i];
		}
		root.SetLeft(ColHdrs[numcols]);
		ColHdrs[numcols].SetRight(root);
	}

	public int GetRowsInSolution() {
		return foundsolution.size();
	}

	public int GetSolutionRow(int row) {
		return foundsolution.get(row - 1);
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
		int minsize = Integer.MAX_VALUE;
		DLXColumn search, mincol;

		mincol = search = (DLXColumn) root.GetRight();

		while (search != root) {
			if (search.GetSize() < minsize) {
				mincol = search;
				minsize = mincol.GetSize();
				if (minsize == 0) {
					break;
				}
			}
			search = (DLXColumn) search.GetRight();
		}
		if (minsize == 0)
			return null;
		else
			return mincol;
	}

	public void AddNode(int colidx, int rowidx) {
		Nodes[++numnodes] = new DLXNode(ColHdrs[colidx], rowidx);
		if (prev_rowidx == rowidx) {
			Nodes[numnodes].SetLeft(lastnodeadded);
			Nodes[numnodes].SetRight(lastnodeadded.GetRight());
			lastnodeadded.SetRight(Nodes[numnodes]);
			Nodes[numnodes].GetRight().SetLeft(Nodes[numnodes]);
		} else {
			prev_rowidx = rowidx;
			Rows[++numrows] = new DLXRow(Nodes[numnodes]);
			Nodes[numnodes].SetLeft(Nodes[numnodes]);
			Nodes[numnodes].SetRight(Nodes[numnodes]);
		}
		lastnodeadded = Nodes[numnodes];
	}

	/**
	 * Determines the number of solutions that can be found for this grid.
	 * 
	 * @param gridGenerator
	 *            The GridGenerator start started this process and to which the
	 *            progress updates will be sent.
	 * @param solveType
	 *            The solve type to be used to determine the number of
	 *            solutions.
	 * @return The number of solutions, given the solution type, that can be
	 *         found for this grid.
	 */
	protected int Solve(SolveType solveType) {
		if (!isValid)
			return -1;

		solvetype = solveType;
		NumSolns = 0;
		complexity = 0;
		search(trysolution.size());
		return NumSolns;
	}

	private void search(int k) {
		DLXColumn chosenCol;
		LL2DNode r, j;

		// A solution is found in case all columns are covered
		if (root.GetRight() == root) {
			NumSolns++;
			foundsolution = new ArrayList<Integer>(trysolution);
			if (MathDokuDLX.DEBUG_DLX) {
				Log.i(TAG, "Solution " + NumSolns
						+ " found which consists of following moves: "
						+ trysolution.toString());
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
				if (k >= trysolution.size())
					trysolution.add(((DLXNode) r).GetRowIdx());
				else
					trysolution.set(k, ((DLXNode) r).GetRowIdx());
				j = r.GetRight();
				while (j != r) {
					CoverCol(((DLXNode) j).GetColumn());
					j = j.GetRight();
				}
				search(k + 1);
				if (solvetype == SolveType.ONE && NumSolns > 0) {
					// Stop as soon as we find 1 solution
					return;
				}
				if (solvetype == SolveType.MULTIPLE && NumSolns > 1) {
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
		return;
	}
}
