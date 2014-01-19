package net.mathdoku.plus.storage;

// The Objects Creator is responsible for creating all new objects needed by
// this class. For unit testing purposes the default create methods can be
// overridden if needed.
public class GridStorageObjectsCreator {
	public GridCageStorage createGridCageStorage() {
		return new GridCageStorage();
	}
}