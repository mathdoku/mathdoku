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

	// The number of times "check progress" was used and the total number of
	// invalids values which have been found when using this option (a cheat)
	public int checkProgressUsed;
	public int checkProgressInvalidsFound;

	// Has the entire solution been revealed?
	public boolean solutionRevealed;

	// Has the grid been solved manually (i.e. not revealed)?
	public boolean solvedManually;

	// Has the grid been finished (either solved or revealed solution)?
	public boolean finished;

	// Counters available
	public enum StatisticsCounterType {
		MOVES, POSSIBLES, UNDOS, CELLS_REVEALED, OPERATORS_REVEALED, CHECK_PROGRESS_USED, CHECK_PROGRESS_INVALIDS_FOUND
	};

	public void show(Activity activity) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle("Grid statistics")
				.setMessage(
						"Current statistics for grid "
						// Line 1
								+ " * id: "
								+ _id
								+ " \n"
								// Line 2
								+ " * grid size: "
								+ gridSize
								+ " \n"
								// Line 3
								+ " * First move: "
								+ firstMove.toString()
								+ " \n"
								// Line 4
								+ " * Last move: "
								+ lastMove
								+ " \n"
								// Line 5
								+ " * Elapsed: "
								+ elapsedTime
								+ " \n"
								// Line 6
								+ " * Cheat penalty: "
								+ cheatPenaltyTime
								+ " (not yet implemented)\n"
								// Line 7
								+ " * Moves: "
								+ moves
								+ " \n"
								// Line 8
								+ " * Possibles: "
								+ possibles
								+ " \n"
								// Line 9
								+ " * Undos: "
								+ undos
								+ " \n"
								// Line 10
								+ " * Cells revealed: "
								+ cellsRevealed
								+ " \n"
								// Line 11
								+ " * Operators revealed: "
								+ operatorsRevevealed
								+ " \n"
								// Line 12
								+ " * Check progress used: "
								+ checkProgressUsed
								+ " \n"
								// Line 13
								+ " * Invalids found with check progress: "
								+ checkProgressInvalidsFound
								+ " \n"
								// Line 14
								+ " * solutionRevealed: "
								+ Boolean.toString(solutionRevealed) + " \n"
								// Line 15
								+ " * solved: "
								+ Boolean.toString(solvedManually) + " \n"
								// Line 16
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
		case CHECK_PROGRESS_USED:
			checkProgressUsed++;
			break;
		case CHECK_PROGRESS_INVALIDS_FOUND:
			checkProgressInvalidsFound++;
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