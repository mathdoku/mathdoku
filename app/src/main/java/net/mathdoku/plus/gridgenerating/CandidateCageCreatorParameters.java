package net.mathdoku.plus.gridgenerating;

import net.mathdoku.plus.matrix.Matrix;

import java.util.Random;

public class CandidateCageCreatorParameters {
    private GridGeneratingParameters gridGeneratingParameters;
    private Random random;
    private Matrix<Integer> correctValueMatrix;
    private Matrix<Integer> cageIdMatrix;
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

    public CandidateCageCreatorParameters setCorrectValueMatrix(Matrix<Integer> correctValueMatrix) {
        this.correctValueMatrix = correctValueMatrix;
        overlappingSubsetChecker = new OverlappingSubsetChecker(correctValueMatrix);
        return this;
    }

    public CandidateCageCreatorParameters setCageIdMatrix(Matrix<Integer> cageIdMatrix) {
        this.cageIdMatrix = cageIdMatrix;
        return this;
    }

    public GridGeneratingParameters getGridGeneratingParameters() {
        return gridGeneratingParameters;
    }

    public Matrix<Integer> getCorrectValueMatrix() {
        return correctValueMatrix;
    }

    public Matrix<Integer> getCageIdMatrix() {
        return cageIdMatrix;
    }

    public OverlappingSubsetChecker getOverlappingSubsetChecker() {
        return overlappingSubsetChecker;
    }

    public CageOperatorGenerator createCageOperatorGenerator(int... cellValues) {
        return new CageOperatorGenerator(gridGeneratingParameters, random, cellValues);
    }
}
