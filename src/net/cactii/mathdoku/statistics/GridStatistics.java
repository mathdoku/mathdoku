package net.cactii.mathdoku.statistics;

import java.sql.Timestamp;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.storage.database.DatabaseHelper;
import net.cactii.mathdoku.storage.database.StatisticsDatabaseAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * Statistics for a single grid.
 */
public class GridStatistics {
	// Unique row id in database
	public long _id;

	// Unique string representation of this grid
	public String gridSignature;

	// Size of the grid
	public int gridSize;

	// Timestamp of first and last move
	public java.sql.Timestamp firstMove;
	public java.sql.Timestamp lastMove;

	// Time elapsed while playing (e.d. displaying the game)
	public long elapsedTime;

	// Time added to the elapsed time because of using cheats
	public long cheatPenaltyTime; // TODO: implement cheat penalty time

	// The number of moves (e.g. number of user values entered) made
	public int moves;

	// The number of possible values used
	public int possibles;

	// THe number of moves reverse via undo
	public int undos;

	// The number of cells revealed (a cheat)
	public int cellsRevealed;

	// The number of cage operators revealed (a cheat)
	public int operatorsRevevealed;

	// Has the entire solution been revealed?
	public boolean solutionRevealed;

	// Has the grid been solved manually (i.e. not revealed)?
	public boolean solvedManually;

	// Has the grid been finished (either solved or revealed solution)?
	public boolean finished;

	// Counters available
	public enum StatisticsCounterType {
		MOVES, POSSIBLES, UNDOS, CELLS_REVEALED, OPERATORS_REVEALED
	};

	public void show(Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Grid statistics")
				.setMessage(
						"Current statistics for grid " + " * id: " + _id
								+ " \n" + " * grid size: " + gridSize + " \n"
								+ " * First move: " + firstMove.toString()
								+ " \n" + " * Last move: " + lastMove + " \n"
								+ " * Elapsed: " + elapsedTime + " \n"
								+ " * Cheat penalty: " + cheatPenaltyTime
								+ " (not yet implemented)\n" + " * Moves: "
								+ moves + " \n" + " * Possibles: " + possibles
								+ " \n" + " * Undos: " + undos + " \n"
								+ " * Cells revealed: " + cellsRevealed + " \n"
								+ " * Operators revealed "
								+ operatorsRevevealed + " \n"
								+ " * solutionRevealed: "
								+ Boolean.toString(solutionRevealed) + " \n"
								+ " * solved: "
								+ Boolean.toString(solvedManually) + " \n"
								+ " * finished: " + Boolean.toString(finished))
				.setCancelable(false)
				.setPositiveButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do nothing
							}
						});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	public boolean save() {
		DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
		StatisticsDatabaseAdapter statisticsDatabaseAdapter = new StatisticsDatabaseAdapter(
				databaseHelper);
		return statisticsDatabaseAdapter.update(this);
	}

	public void increaseCounter(StatisticsCounterType statisticsCounterType) {
		switch (statisticsCounterType) {
		case MOVES:
			moves++;
			break;
		case POSSIBLES:
			possibles++;
			break;
		case UNDOS:
			undos++;
			break;
		case CELLS_REVEALED:
			cellsRevealed++;
			break;
		case OPERATORS_REVEALED:
			operatorsRevevealed++;
			break;
		}
		setLastMoveToCurrentTime();
	}

	public void solved() {
		if (!solutionRevealed) {
			solvedManually = true;
		}
		finished = true;
		setLastMoveToCurrentTime();
	}

	public void solutionRevealed() {
		solutionRevealed = true;
		solvedManually = false;
		finished = true;
		setLastMoveToCurrentTime();
	}

	public void setLastMoveToCurrentTime() {
		lastMove = new java.sql.Timestamp(System.currentTimeMillis());
	}
}