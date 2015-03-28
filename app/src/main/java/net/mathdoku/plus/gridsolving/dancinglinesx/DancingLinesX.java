package net.mathdoku.plus.gridsolving.dancinglinesx;

import android.util.Log;

import net.mathdoku.plus.gridsolving.GridSolver;

import java.util.ArrayList;
import java.util.List;

public class DancingLinesX {
    @SuppressWarnings("unused")
    private static final String TAG = DancingLinesX.class.getName();

    public enum SolveType {
        ONE,
        MULTIPLE,
        ALL
    }

    private final ConstraintNode rootConstraintNode;
    private ConstraintNode[] constraintNodes;
    private PermutationNode[] permutationNodes;
    private int numberOfPermutationNodesAllocated;
    private PermutationNode lastPermutationNodeAdded;
    private List<Integer> selectedPermutationNodeIndexes;
    private List<Integer> lastSolutionFound;
    private int countSolutionsFound;
    private boolean isValid;
    private int previousPermutationIndex = -1;
    private SolveType solveType;

    private int complexity;

    public DancingLinesX() {
        rootConstraintNode = new ConstraintNode();
        selectedPermutationNodeIndexes = new ArrayList<Integer>();
        isValid = true;
    }

    public void init(int numberOfPermutations, int numberOfConstraints) {
        initPermutationNodes(numberOfPermutations);
        initConstraintNodes(numberOfConstraints);
    }

    private void initPermutationNodes(int numNodes) {
        permutationNodes = new PermutationNode[numNodes + 1];
        this.numberOfPermutationNodesAllocated = 0;
    }

    private void initConstraintNodes(int numCols) {
        constraintNodes = new ConstraintNode[numCols];
        for (int i = 0; i < constraintNodes.length; i++) {
            constraintNodes[i] = new ConstraintNode();
        }

        ConstraintNode prev = rootConstraintNode;
        for (ConstraintNode constraintNode : constraintNodes) {
            prev.setRight(constraintNode);
            constraintNode.setLeft(prev);
            prev = constraintNode;
        }
        rootConstraintNode.setLeft(constraintNodes[constraintNodes.length - 1]);
        constraintNodes[constraintNodes.length - 1].setRight(rootConstraintNode);
    }

    public int getRowsInSolution() {
        return lastSolutionFound.size();
    }

    public int getSolutionRow(int row) {
        return lastSolutionFound.get(row - 1);
    }

    private void coverCol(ConstraintNode coverCol) {
        Node i, j;
        coverCol.getRight()
                .setLeft(coverCol.getLeft());
        coverCol.getLeft()
                .setRight(coverCol.getRight());

        i = coverCol.getDown();
        while (i != coverCol) {
            j = i.getRight();
            while (j != i) {
                j.getDown()
                        .setUp(j.getUp());
                j.getUp()
                        .setDown(j.getDown());
                ((PermutationNode) j).getConstraintNode()
                        .decreaseNumberOfPermutations();
                j = j.getRight();
            }
            i = i.getDown();
        }
    }

    private void uncoverCol(ConstraintNode uncoverCol) {
        Node i, j;

        i = uncoverCol.getUp();
        while (i != uncoverCol) {
            j = i.getLeft();
            while (j != i) {
                ((PermutationNode) j).getConstraintNode()
                        .increaseNumberOfPermutations();
                j.getDown()
                        .setUp(j);
                j.getUp()
                        .setDown(j);
                j = j.getLeft();
            }
            i = i.getUp();
        }
        uncoverCol.getRight()
                .setLeft(uncoverCol);
        uncoverCol.getLeft()
                .setRight(uncoverCol);
    }

    private ConstraintNode chooseMinCol() {
        int minSize = Integer.MAX_VALUE;
        ConstraintNode search, minColumn;

        minColumn = search = (ConstraintNode) rootConstraintNode.getRight();

        while (search != rootConstraintNode) {
            if (search.getNumberOfPermutations() < minSize) {
                minColumn = search;
                minSize = minColumn.getNumberOfPermutations();
                if (minSize == 0) {
                    break;
                }
            }
            search = (ConstraintNode) search.getRight();
        }
        if (minSize == 0) {
            return null;
        } else {
            return minColumn;
        }
    }

    public void addPermutation(int permutationIndex, int constraintIndex) {
        permutationNodes[++numberOfPermutationNodesAllocated] = new PermutationNode(constraintNodes[constraintIndex], permutationIndex);
        if (previousPermutationIndex == permutationIndex) {
            permutationNodes[numberOfPermutationNodesAllocated].setLeft(lastPermutationNodeAdded);
            permutationNodes[numberOfPermutationNodesAllocated].setRight(lastPermutationNodeAdded.getRight());
            lastPermutationNodeAdded.setRight(permutationNodes[numberOfPermutationNodesAllocated]);
            permutationNodes[numberOfPermutationNodesAllocated].getRight()
                    .setLeft(permutationNodes[numberOfPermutationNodesAllocated]);
        } else {
            previousPermutationIndex = permutationIndex;
            permutationNodes[numberOfPermutationNodesAllocated].setLeft(permutationNodes[numberOfPermutationNodesAllocated]);
            permutationNodes[numberOfPermutationNodesAllocated].setRight(permutationNodes[numberOfPermutationNodesAllocated]);
        }
        lastPermutationNodeAdded = permutationNodes[numberOfPermutationNodesAllocated];
    }

    /**
     * Determines the number of solutions that can be found for this grid.
     *
     * @param solveType
     *         The solve type to be used to determine the number of solutions.
     * @return The number of solutions, given the solution type, that can be found for this grid.
     */
    @SuppressWarnings("SameParameterValue")
    public int solve(SolveType solveType) {
        if (!isValid) {
            return -1;
        }

        this.solveType = solveType;
        countSolutionsFound = 0;
        complexity = 0;
        search(selectedPermutationNodeIndexes.size());
        return countSolutionsFound;
    }

    private void search(int k) {
        ConstraintNode chosenCol;
        Node r, j;

        // A solution is found in case all columns are covered
        if (rootConstraintNode.getRight() == rootConstraintNode) {
            countSolutionsFound++;
            lastSolutionFound = new ArrayList<Integer>(selectedPermutationNodeIndexes);
            if (GridSolver.DEBUG) {
                Log.i(TAG,
                      "Solution " + countSolutionsFound + " found which consists of following moves: " + selectedPermutationNodeIndexes
                              .toString());
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
                if (k >= selectedPermutationNodeIndexes.size()) {
                    selectedPermutationNodeIndexes.add(((PermutationNode) r).getPermutationIndex());
                } else {
                    selectedPermutationNodeIndexes.set(k, ((PermutationNode) r).getPermutationIndex());
                }
                j = r.getRight();
                while (j != r) {
                    coverCol(((PermutationNode) j).getConstraintNode());
                    j = j.getRight();
                }
                search(k + 1);
                if (solveType == SolveType.ONE && countSolutionsFound > 0) {
                    // Stop as soon as we find 1 solution
                    return;
                }
                if (solveType == SolveType.MULTIPLE && countSolutionsFound > 1) {
                    // Stop as soon as we find multiple solutions
                    return;
                }
                j = r.getLeft();
                while (j != r) {
                    uncoverCol(((PermutationNode) j).getConstraintNode());
                    j = j.getLeft();
                }
                r = r.getDown();
            }
            uncoverCol(chosenCol);
        }
    }

    public int getComplexity() {
        return complexity;
    }
}
