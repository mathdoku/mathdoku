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
    private int previousPermutationIndex = -1;
    private SolveType solveType;

    private int complexity;

    public DancingLinesX(int numberOfPermutations, int numberOfConstraints) {
        rootConstraintNode = new ConstraintNode();
        selectedPermutationNodeIndexes = new ArrayList<Integer>();
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
        return lastSolutionFound.get(row);
    }

    public List<Integer> getLastSolutionFound() {
        return lastSolutionFound;
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
        this.solveType = solveType;
        countSolutionsFound = 0;
        complexity = 0;
        searchForSolution(selectedPermutationNodeIndexes.size());
        return countSolutionsFound;
    }

    private void searchForSolution(int step) {
        if (isSolutionFound()) {
            storeSolution();
            return;
        }

        // In case no solution is yet found, select the next column to be covered. Now two things can happen. Either
        // such a column can be found, and the puzzle solving will be taken one level deeper. Or such a column can
        // not be found in which case a backtrack will be done. The more often a permutation is tried,
        // the harder to solve the puzzle is.
        complexity++;

        ConstraintNode constraintNode = getConstraintWithSmallestNumberOfPermutations();
        if (constraintNode != null) {
            coverConstraintNode(constraintNode);
            Node node = constraintNode.getDown();
            while (node != constraintNode) {
                if (step >= selectedPermutationNodeIndexes.size()) {
                    selectedPermutationNodeIndexes.add(((PermutationNode) node).getPermutationIndex());
                } else {
                    selectedPermutationNodeIndexes.set(step, ((PermutationNode) node).getPermutationIndex());
                }
                coverConstraintsForPermutationNode(node);
                searchForSolution(step + 1);
                if (stopSearchingForSolution()) {
                    return;
                }
                uncoverConstraintsForPermutationNode(node);
                node = node.getDown();
            }
            uncoverConstraintNode(constraintNode);
        }
    }

    private boolean isSolutionFound() {
        // A solution is found in case all constraints are covered.
        return rootConstraintNode.getRight() == rootConstraintNode;
    }

    private void storeSolution() {
        countSolutionsFound++;
        lastSolutionFound = new ArrayList<Integer>(selectedPermutationNodeIndexes);
        if (GridSolver.DEBUG) {
            Log.i(TAG,
                  "Solution " + countSolutionsFound + " found which consists of following moves: " +
                          selectedPermutationNodeIndexes.toString());
        }
    }

    private ConstraintNode getConstraintWithSmallestNumberOfPermutations() {
        ConstraintNode constraintWithSmallestNumberOfPermutations = null;
        int smallestNumberOfPermutations = Integer.MAX_VALUE;

        ConstraintNode constraintNode = (ConstraintNode) rootConstraintNode.getRight();
        while (constraintNode != rootConstraintNode) {
            if (constraintNode.getNumberOfPermutations() < smallestNumberOfPermutations) {
                constraintWithSmallestNumberOfPermutations = constraintNode;
                smallestNumberOfPermutations = constraintWithSmallestNumberOfPermutations.getNumberOfPermutations();
                if (smallestNumberOfPermutations == 0) {
                    return null;
                }
            }
            constraintNode = (ConstraintNode) constraintNode.getRight();
        }
        return constraintWithSmallestNumberOfPermutations;
    }

    private void coverConstraintsForPermutationNode(Node firstNode) {
        Node node = firstNode.getRight();
        while (node != firstNode) {
            coverConstraintNode(((PermutationNode) node).getConstraintNode());
            node = node.getRight();
        }
    }

    private void coverConstraintNode(ConstraintNode constraintNode) {
        constraintNode.getRight()
                .setLeft(constraintNode.getLeft());
        constraintNode.getLeft()
                .setRight(constraintNode.getRight());

        Node i = constraintNode.getDown();
        Node j;
        while (i != constraintNode) {
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

    private boolean stopSearchingForSolution() {
        if (solveType == SolveType.ONE && countSolutionsFound > 0) {
            // Stop as soon as we find the first solution
            return true;
        }
        if (solveType == SolveType.MULTIPLE && countSolutionsFound > 1) {
            // Stop as soon as we find multiple solutions
            return true;
        }
        return false;
    }

    private void uncoverConstraintsForPermutationNode(Node firstNode) {
        Node node = firstNode.getLeft();
        while (node != firstNode) {
            uncoverConstraintNode(((PermutationNode) node).getConstraintNode());
            node = node.getLeft();
        }
    }

    private void uncoverConstraintNode(ConstraintNode constraintNode) {
        Node i = constraintNode.getUp();
        Node j;
        while (i != constraintNode) {
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
        constraintNode.getRight()
                .setLeft(constraintNode);
        constraintNode.getLeft()
                .setRight(constraintNode);
    }

    public int getComplexity() {
        return complexity;
    }
}
