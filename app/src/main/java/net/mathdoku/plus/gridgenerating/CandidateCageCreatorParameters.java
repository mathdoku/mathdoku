package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.matrix.SquareMatrix;

import java.util.Random;

public class CandidateCageCreatorParameters {
    private GridGeneratingParameters gridGeneratingParameters;
    private Random random;
    private SquareMatrix<Integer> correctValueSquareMatrix;
    private SquareMatrix<Integer> cageIdSquareMatrix;
    private OverlappingSubsetChecker overlappingSubsetChecker;

    public CandidateCageCreatorParameters setGridGeneratingParameters(GridGeneratingParameters
                                                                              gridGeneratingParameters) {
        this.gridGeneratingParameters = gridGeneratingParameters;
        return this;
    }

    public CandidateCageCreatorParameters setRandom(Random random) {
        this.random = random;
        return this;
    }

    public CandidateCageCreatorParameters setCorrectValueSquareMatrix(SquareMatrix<Integer> correctValueSquareMatrix) {
        this.correctValueSquareMatrix = correctValueSquareMatrix;
        overlappingSubsetChecker = new OverlappingSubsetChecker(correctValueSquareMatrix);
        return this;
    }

    public CandidateCageCreatorParameters setCageIdSquareMatrix(SquareMatrix<Integer> cageIdSquareMatrix) {
        this.cageIdSquareMatrix = cageIdSquareMatrix;
        return this;
    }

    public GridGeneratingParameters getGridGeneratingParameters() {
        return gridGeneratingParameters;
    }

    public SquareMatrix<Integer> getCorrectValueSquareMatrix() {
        return correctValueSquareMatrix;
    }

    public SquareMatrix<Integer> getCageIdSquareMatrix() {
        return cageIdSquareMatrix;
    }

    public OverlappingSubsetChecker getOverlappingSubsetChecker() {
        return overlappingSubsetChecker;
    }

    public CageOperatorGenerator createCageOperatorGenerator(int... cellValues) {
        return new CageOperatorGenerator(gridGeneratingParameters, random, cellValues);
    }
}
