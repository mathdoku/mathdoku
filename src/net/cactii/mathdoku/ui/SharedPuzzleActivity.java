package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper;
import net.cactii.mathdoku.developmentHelper.DevelopmentHelper.Mode;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.grid.ui.GridViewerView;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridRow;
import net.cactii.mathdoku.util.FeedbackEmail;
import net.cactii.mathdoku.util.SharedPuzzle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.bugsense.trace.BugSenseHandler;

public class SharedPuzzleActivity extends AppFragmentActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.SharedPuzzleFragmentActivity";

	public static final String RESTART_LAST_GAME_SHARED_PUZZLE = "RestartLastGame";

	// The grid constructed from the share url
	Grid mGrid;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// In BugSense mode the bug sense handler is initiated and started. In
		// case an exception occurs in this mode, it will be reported via the
		// BugSense web site. In this way exceptions which occurs while testing
		// the app can be monitored more closely. Note: the internet permission
		// needs to activated for this.
		if (DevelopmentHelper.mMode == Mode.BUG_SENSE) {
			BugSenseHandler.initAndStartSession(this,
					DevelopmentHelper.BUG_SENSE_API_KEY);
		}

		// First check whether the intent can be processed.
		if (isValidIntent(getIntent()) == false) {
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
							}).show();
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
		((Button) findViewById(R.id.shared_puzzle_play_button))
				.setBackgroundColor(Painter.getInstance()
						.getButtonBackgroundColor());

		// Load the grid into the grid view.
		((GridViewerView) findViewById(R.id.grid_viewer_view))
				.loadNewGrid(mGrid);

		// Display the difficulty rating.
		final VerticalRatingBar puzzleParameterDifficultyRatingBar = (VerticalRatingBar) findViewById(R.id.puzzleParameterDifficultyRatingBar);
		puzzleParameterDifficultyRatingBar.setEnabled(false);
		switch (mGrid.getPuzzleComplexity()) {
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

		// Get the grid definition form the uri.
		String gridDefinition = SharedPuzzle.getGridDefinitionFromUrl(uri);
		if (gridDefinition == null) {
			return false;
		}

		// Disable the grid as the user should not be able to click
		// cells in the shared puzzle view
		mGrid = new Grid();
		if (mGrid.load(gridDefinition)) {
			mGrid.setActive(false);
			return true;
		}

		return false;
	}

	/**
	 * Load the shared puzzle as the active puzzle in MathDoku.
	 */
	public void onClickPlayGame(View view) {
		// First check if the puzzle already exists
		GridRow gridRow = new GridDatabaseAdapter().getByGridDefinition(mGrid
				.toGridDefinitionString());
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
							}).show();
		}
	}

	/**
	 * Start the puzzle fragment for the grid which is displayed.
	 */
	private void startPuzzleFragment() {
		// Activate and store the newly created grid so it will be played when
		// the puzzle fragment activity is (re)started.
		mGrid.setActive(true);
		if (mGrid.insertInDatabase() && mGrid.save()) {
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