package net.mathdoku.plus.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import net.mathdoku.plus.R;
import net.mathdoku.plus.griddefinition.GridDefinition;
import net.mathdoku.plus.painter.Painter;
import net.mathdoku.plus.puzzle.InvalidGridException;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.ui.GridViewerView;
import net.mathdoku.plus.storage.databaseadapter.database.GridDatabaseAdapter;
import net.mathdoku.plus.storage.databaseadapter.database.GridRow;
import net.mathdoku.plus.ui.base.AppFragmentActivity;
import net.mathdoku.plus.util.FeedbackEmail;
import net.mathdoku.plus.util.SharedPuzzle;

public class SharedPuzzleActivity extends AppFragmentActivity {

	@SuppressWarnings("unused")
	private static final String TAG = SharedPuzzleActivity.class.getName();

	public static final String RESTART_LAST_GAME_SHARED_PUZZLE = "RestartLastGame";

	// The grid constructed from the share url
	private Grid mGrid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// First check whether the intent can be processed.
		if (!isValidIntent(getIntent())) {
			new AlertDialog.Builder(this)
					.setIcon(R.drawable.icon)
					.setTitle(R.string.dialog_invalid_share_url_title)
					.setMessage(R.string.dialog_invalid_share_url_body)
					.setCancelable(false)
					.setNeutralButton(R.string.dialog_general_button_close,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									finish();
								}
							})
					.show();
			return;
		}

		// The format of the uri in the intent has the required format. Load the
		// view with all data.
		setContentView(R.layout.shared_puzzle_fragment);

		// Setup the action bar
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			actionBar.setDisplayHomeAsUpEnabled(false);
			actionBar.setTitle(getResources().getString(
					R.string.shared_puzzle_action_bar_title));
		}

		// Initialize the painter with the correct theme
		Painter.getInstance().setTheme(mMathDokuPreferences.getTheme());

		// set color
		findViewById(R.id.shared_puzzle_play_button).setBackgroundColor(
				Painter.getInstance().getButtonBackgroundColor());

		// Load the grid into the grid view.
		((GridViewerView) findViewById(R.id.grid_viewer_view))
				.loadNewGrid(mGrid);

		// Display the difficulty rating.
		final VerticalRatingBar puzzleParameterDifficultyRatingBar = (VerticalRatingBar) findViewById(R.id.puzzleParameterDifficultyRatingBar);
		puzzleParameterDifficultyRatingBar.setEnabled(false);
		switch (mGrid.getPuzzleComplexity()) {
		case RANDOM:
			// Note: puzzles will never be created with this complexity.
			break;
		case VERY_EASY:
			puzzleParameterDifficultyRatingBar.setNumStars(1);
			break;
		case EASY:
			puzzleParameterDifficultyRatingBar.setNumStars(2);
			break;
		case NORMAL:
			puzzleParameterDifficultyRatingBar.setNumStars(3);
			break;
		case DIFFICULT:
			puzzleParameterDifficultyRatingBar.setNumStars(4);
			break;
		case VERY_DIFFICULT:
			puzzleParameterDifficultyRatingBar.setNumStars(5);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.shared_puzzle_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		int menuId = menuItem.getItemId();
		switch (menuId) {
		case R.id.action_send_feedback:
			new FeedbackEmail(this).show();
			return true;
		default:
			return super.onOptionsItemSelected(menuItem);
		}
	}

	/**
	 * Checks whether the specified intent is valid an can be processed by this
	 * activity.
	 * 
	 * @param intent
	 *            The intent to be checked.
	 * @return True in case the intent contains a valid grid which can be
	 *         processed.
	 */
	private boolean isValidIntent(Intent intent) {
		// The intent must exist.
		if (intent == null) {
			return false;
		}

		// The intent should contain data.
		Uri uri = intent.getData();
		if (uri == null) {
			return false;
		}

		String gridDefinition = new SharedPuzzle(this)
				.getGridDefinitionFromUrl(uri);
		try {
			mGrid = new GridDefinition(gridDefinition).createGrid();
		} catch (InvalidGridException e) {
			Log.d("Cannot create a grid for definition '%s'.", gridDefinition,
					e);
			return false;
		}

		// Disable the grid as the user should not be able to click
		// cells in the shared puzzle view
		mGrid.setActive(false);
		return true;
	}

	/**
	 * Load the shared puzzle as the active puzzle in MathDoku.
	 */
	public void onClickPlayGame(@SuppressWarnings("UnusedParameters") View view) {
		// First check if the puzzle already exists
		String gridDefinition = mGrid.getDefinition();
		GridRow gridRow = new GridDatabaseAdapter()
				.getByGridDefinition(gridDefinition);
		if (gridRow == null) {
			startPuzzleFragment();
		} else {
			new AlertDialog.Builder(this)
					.setTitle(R.string.shared_puzzle_exists_title)
					.setMessage(
							getResources().getString(
									R.string.shared_puzzle_exists_message,
									gridRow.mId))
					.setNegativeButton(
							R.string.shared_puzzle_exists_negative_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Finish the preview activity
									finish();
								}
							})
					.setPositiveButton(
							R.string.shared_puzzle_exists_positive_button,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									startPuzzleFragment();
								}
							})
					.show();
		}
	}

	/**
	 * Start the puzzle fragment for the grid which is displayed.
	 */
	private void startPuzzleFragment() {
		// Activate and store the newly created grid so it will be played when
		// the puzzle fragment activity is (re)started.
		mGrid.setActive(true);
		if (mGrid.save()) {
			// Stop the preview activity.
			finish();

			// Start the main activity of MathDoku
			Intent intent = new Intent(this, PuzzleFragmentActivity.class)
					.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							+ Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra(RESTART_LAST_GAME_SHARED_PUZZLE, true);
			startActivity(intent);
		}
	}
}
