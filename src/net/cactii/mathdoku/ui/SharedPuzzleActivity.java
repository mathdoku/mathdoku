package net.cactii.mathdoku.ui;

import java.util.List;

import net.cactii.mathdoku.Grid;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridRow;
import net.cactii.mathdoku.util.FeedbackEmail;
import android.annotation.TargetApi;
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

public class SharedPuzzleActivity extends AppFragmentActivity {

	@SuppressWarnings("unused")
	private static final String TAG = "MathDoku.SharedPuzzleFragmentActivity";

	public static final String RESTART_LAST_GAME_SHARED_PUZZLE = "RestartLastGame";

	// Elements of the share url
	private static final String SHARE_URI_SCHEME = "http";
	private static final String SHARE_URI_HOST = "mathdoku.net";
	private static final String SHARE_URI_PUZZLE = "puzzle";
	private static final String SHARE_URI_VERSION = "1";

	// The grid constructed from the share url
	Grid mGrid;

	@Override
	@TargetApi(14)
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shared_puzzle_fragment);

		if (isValidIntent(getIntent()) == false) {
			// The intent does not contain a url in the required format.
			finish();
			return;
		}

		// The format of the uri in the intent has the required format.

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
				.setBackgroundColor(0xFF33B5E5); // TODO: retrieve from painter

		// Load the grid into the grid view.
		((GridView) findViewById(R.id.gridView)).loadNewGrid(mGrid);
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
	 * Get the share url for the given grid definition.
	 * 
	 * @param gridDefinition
	 *            The grid definition for which the share url has to be made.
	 * @return The share url for the given grid definition.
	 */
	public static String getShareUrl(String gridDefinition) {
		return SHARE_URI_SCHEME + "://" + SHARE_URI_HOST + "/"
				+ SHARE_URI_PUZZLE + "/" + SHARE_URI_VERSION + "/"
				+ gridDefinition + "/" + gridDefinition.hashCode();
	}

	/**
	 * Checks whether the specified intent is valid an can be processed by this
	 * activity.
	 * 
	 * @param intent
	 *            THe intent to be checked.
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

		// The data should contain exactly 4 segments
		List<String> pathSegments = uri.getPathSegments();
		if (pathSegments == null || pathSegments.size() != 4) {
			return false;
		}
		if (pathSegments.get(0).equals(SHARE_URI_PUZZLE) == false) {
			return false;
		}
		if (pathSegments.get(1).equals(SHARE_URI_VERSION) == false) {
			return false;
		}
		// Check if grid definition (part 3) matches with the hashcode (part 4).
		// This is a simple measure to check if the uri is complete and not
		// manually changed by an ordinary user. It it still possible to
		// manually manipulate the grid definition and the hashcode but this can
		// do no harm as it is still checked whether a valid grid is specified.
		String gridDefinition = pathSegments.get(2);
		if (gridDefinition.hashCode() != Integer.valueOf(pathSegments.get(3))) {
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
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// Finish the preview activity
									finish();
								}
							})
					.setPositiveButton(
							R.string.shared_puzzle_exists_positive_button,
							new DialogInterface.OnClickListener() {
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