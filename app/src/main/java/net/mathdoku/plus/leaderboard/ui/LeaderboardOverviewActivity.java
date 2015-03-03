package net.mathdoku.plus.leaderboard.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import net.mathdoku.plus.Preferences;
import net.mathdoku.plus.R;
import net.mathdoku.plus.leaderboard.LeaderboardConnector;
import net.mathdoku.plus.storage.databaseadapter.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.tip.TipLeaderboardCreateGame;
import net.mathdoku.plus.tip.TipLeaderboardViewDetails;
import net.mathdoku.plus.ui.GooglePlusSignInDialog;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;
import net.mathdoku.plus.ui.base.GooglePlayServiceFragmentActivity;
import net.mathdoku.plus.util.FeedbackEmail;

/**
 * This activity handles displaying the leaderboard overviews. Each leaderboard
 * overview displays a number of related leaderboards (e.g. of same size) which
 * additionally can be filtered on certain criteria.
 */
public class LeaderboardOverviewActivity extends
		GooglePlayServiceFragmentActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the leaderboard fragments. A
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative is used,
	 * which will keep every loaded fragment in memory.
	 */
	private LeaderboardOverviewPagerAdapter mLeaderboardOverviewPagerAdapter;

	/**
	 * The {@link ViewPager} that will display the leaderboard fragments , one
	 * at a time.
	 */
	private ViewPager mViewPager;

	private ActionBar mActionBar;

	public enum LeaderboardFilter {
		ALL_LEADERBOARDS, MY_LEADERBOARDS, HIDDEN_OPERATORS, VISIBLE_OPERATORS
	}

	private LeaderboardFilter mLeaderboardFilter;

	private LeaderboardRankUpdaterProgressDialog mLeaderboardRankUpdaterProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard_activity_fragment);

		// Create the adapter that will return the leaderboard fragments.
		mLeaderboardOverviewPagerAdapter = new LeaderboardOverviewPagerAdapter(
				this, getSupportFragmentManager());

		initializeViewPager();
		initializeActionBar();
		createActionBarTabPerFragment();
		selectTabLastDisplayed();
		setFilterSpinner(LeaderboardFilter.ALL_LEADERBOARDS);

		/*
		 * Styling of the pager tab strip is not possible from within code. See
		 * values-v14/styles.xml for styling of the action bar tab.
		 */
	}

	private void selectTabLastDisplayed() {
		// Show the same page as last time (or the last tab if leaderboard were
		// not displayed before.
		int tab = mMathDokuPreferences.getLeaderboardsTabLastDisplayed();
		mViewPager.setCurrentItem(tab >= 0 ? tab : mLeaderboardOverviewPagerAdapter.getCount() -
				1);
	}

	private void createActionBarTabPerFragment() {
		// For each of the leaderboard fragments, add a tab to the action bar.
		for (int i = 0; i < mLeaderboardOverviewPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the listener for when this tab is
			// selected.
			mActionBar.addTab(mActionBar
									  .newTab()
									  .setText(mLeaderboardOverviewPagerAdapter.getPageTitle(i))
									  .setTabListener(this));
		}
	}

	private void initializeViewPager() {
		mViewPager = (ViewPager) findViewById(R.id.pager);

		mViewPager.setAdapter(mLeaderboardOverviewPagerAdapter);
		mViewPager.setOnPageChangeListener(new PageChangeListener());
	}

	private void initializeActionBar() {
		// Set up the action bar.
		mActionBar = getActionBar();

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		mActionBar.setDisplayHomeAsUpEnabled(true);

		// Do not display a title as the leaderboard filter will act as title.
		mActionBar.setDisplayShowTitleEnabled(false);

		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.leaderboard_action_bar_custom);
	}

	private class PageChangeListener extends
			ViewPager.SimpleOnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			// Get the fragment on the selected position
			LeaderboardOverview leaderboardOverview = mLeaderboardOverviewPagerAdapter
					.getFragment(mViewPager, position,
							getSupportFragmentManager());

			// Inform the fragment about the current filter as it may be changed
			// since in case the fragment was displayed before.
			if (leaderboardOverview != null) {
				leaderboardOverview
						.setLeaderboardFilter(getLeaderboardFilter());
			}

			// When swiping between different leaderboard fragments, select the
			// corresponding tab.
			mActionBar.setSelectedNavigationItem(position);
		}
	}

	@Override
	protected void onResumeFragments() {
		setFilterSpinner(mMathDokuPreferences
				.getLeaderboardFilterLastValueUsed());
		super.onResumeFragments();
	}

	@Override
	protected void onPause() {
		mMathDokuPreferences
				.setLeaderboardFilterLastValueUsed(getLeaderboardFilter());
		mMathDokuPreferences.setLeaderboardsTabLastDisplayed(mViewPager
				.getCurrentItem());

		super.onPause();
	}

	@Override
	protected void onStop() {
		if (mLeaderboardRankUpdaterProgressDialog != null) {
			mLeaderboardRankUpdaterProgressDialog.dismiss();
		}
		super.onStop();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// The method is required because of implementing interface
		// ActionBar.TabListener. No specific action however is needed when
		// unselecting a tab.
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// The method is required because of implementing interface
		// ActionBar.TabListener. No specific action however is needed when
		// reselecting a tab.
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.leaderboard_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			navigateToParentActivity();
			return true;
		case R.id.action_refresh_leaderboards:
			refreshAllLeaderboards();
			return true;
		case R.id.action_send_feedback:
			new FeedbackEmail(this).show();
			return true;
		case R.id.action_help:
			openHelpDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void navigateToParentActivity() {
		// This is called when the Home (Up) button is pressed in the action
		// bar. Create a simple intent that starts the hierarchical parent
		// activity and use NavUtils in the Support Package to ensure proper
		// handling of Up.
		Intent upIntent = new Intent(this, PuzzleFragmentActivity.class);
		if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
			// This activity is not part of the application's task, so
			// create a new task with a synthesized back stack.
			// If there are ancestor activities, they should be added here.
			TaskStackBuilder
					.create(this)
					.addNextIntent(upIntent)
					.startActivities();
			finish();
		} else {
			// This activity is part of the application's task, so simply
			// navigate up to the hierarchical parent activity.
			NavUtils.navigateUpTo(this, upIntent);
		}
	}

	private void openHelpDialog() {
		new AlertDialog.Builder(this)
				.setTitle(
						getResources().getString(
								R.string.leaderboard_actionbar_title)
								+ " "
								+ getResources()
										.getString(R.string.action_help))
				.setIcon(R.drawable.icon)
				.setView(inflateLeaderboardHelpDialog())
				.setPositiveButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Do nothing
							}
						})
				.show();
	}

	private View inflateLeaderboardHelpDialog() {
		return LayoutInflater.from(this).inflate(R.layout.leaderboard_help_dialog, null);
	}

	@Override
	public void onSignInFailed() {
		GooglePlusSignInDialog googlePlusSignInDialog = new GooglePlusSignInDialog(
				this, new GooglePlusSignInDialog.Listener() {
					@Override
					public void onGooglePlusSignInStart() {
						beginUserInitiatedSignIn();
					}

					@Override
					public void onGooglePlusSignInCancelled() {
						// Leaderboards can not be viewed when not signed in.
						finish();
					}
				})
				.setMessage(R.string.google_plus_login_dialog_on_show_leaderboards);
		googlePlusSignInDialog.setCancelable(false);
		googlePlusSignInDialog.show();
	}

	/**
	 * After a successful sign in to Google+ the leaderboards are updated if
	 * needed. If so, a progress dialog is shown.
	 */
	private void onSignInSucceeded() {
		mLeaderboardRankUpdaterProgressDialog = new LeaderboardRankUpdaterProgressDialog(
				this, new LeaderboardConnector(this, getGamesClient()));
		mLeaderboardRankUpdaterProgressDialog
				.setMessage(getResources()
						.getString(
								R.string.dialog_leaderboard_rank_update_selected_leaderboards_message));
		mLeaderboardRankUpdaterProgressDialog.setCancelable(true);
		mLeaderboardRankUpdaterProgressDialog
				.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						mLeaderboardRankUpdaterProgressDialog = null;
						if (TipLeaderboardViewDetails
								.toBeDisplayed(mMathDokuPreferences)) {
							new TipLeaderboardViewDetails(
									LeaderboardOverviewActivity.this).show();
						}
						if (TipLeaderboardCreateGame
								.toBeDisplayed(mMathDokuPreferences)) {
							new TipLeaderboardCreateGame(
									LeaderboardOverviewActivity.this).show();
						}
					}
				});
		mLeaderboardRankUpdaterProgressDialog.show();
		if (mLeaderboardRankUpdaterProgressDialog.hasNoLeaderboardUpdated()) {
			// No dialog was shown and the onDismissListener is not called.
			if (TipLeaderboardViewDetails.toBeDisplayed(mMathDokuPreferences)) {
				new TipLeaderboardViewDetails(LeaderboardOverviewActivity.this)
						.show();
			}
			if (TipLeaderboardCreateGame.toBeDisplayed(mMathDokuPreferences)) {
				new TipLeaderboardCreateGame(LeaderboardOverviewActivity.this)
						.show();
			}
		}
	}

	@Override
	public void onAutoSignInSucceeded() {
		// Automatic sign-in on Google Play Services has succeeded.
		onSignInSucceeded();
	}

	@Override
	public void onUserInitiatedSignInSucceeded() {
		// User initiated sign to Google Play Services has succeeded.
		onSignInSucceeded();
	}

	/**
	 * Initializes/refreshes the filter spinner. Returns: True in case the
	 * filter spinner should be shown. False otherwise.
	 */
	void setFilterSpinner(LeaderboardFilter leaderboardFilter) {
		Spinner spinner = (Spinner) mActionBar.getCustomView().findViewById(
				R.id.leaderboard_filter_spinner);
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.leaderboard_filter));
		adapterStatus
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Build the spinner
		spinner.setAdapter(adapterStatus);

		mLeaderboardFilter = leaderboardFilter;
		spinner.setSelection(mLeaderboardFilter.ordinal());

		spinner
				.setOnItemSelectedListener(new CreateLeaderboardFilterOnItemSelectedListener());
	}

	private class CreateLeaderboardFilterOnItemSelectedListener implements
			OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			mLeaderboardFilter = LeaderboardFilter.values()[(int) id];

			// Get the fragment which is currently displayed
			LeaderboardOverview leaderboardOverview = mLeaderboardOverviewPagerAdapter
					.getFragment(mViewPager, mViewPager.getCurrentItem(),
							getSupportFragmentManager());

			// Inform the fragment about the change of filter.
			if (leaderboardOverview != null) {
				leaderboardOverview.setLeaderboardFilter(mLeaderboardFilter);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// This method is required by the interface. It has been left
			// empty intentionally.
		}
	}

	/**
	 * Get the currently selected value of the leaderboard filter.
	 * 
	 * @return The currently selected value of the leaderboard filter.
	 */
	public LeaderboardFilter getLeaderboardFilter() {
		return mLeaderboardFilter;
	}

	/**
	 * Refreshes all leaderboard data.
	 */
	private void refreshAllLeaderboards() {
		// Force update of all leaderboards
		new LeaderboardRankDatabaseAdapter().setAllRanksToBeUpdated();

		// Start the leaderboard updater
		mLeaderboardRankUpdaterProgressDialog = new LeaderboardRankUpdaterProgressDialog(
				this, new LeaderboardConnector(this, getGamesClient()));
		mLeaderboardRankUpdaterProgressDialog
				.setMessage(getResources()
						.getString(
								R.string.dialog_leaderboard_rank_update_all_leaderboards_message));
		mLeaderboardRankUpdaterProgressDialog.setCancelable(false);
		mLeaderboardRankUpdaterProgressDialog
				.setOnDismissListener(new LeaderBoardRankUpdaterProgressDialogOnDismissListener());
		mLeaderboardRankUpdaterProgressDialog.show();
	}

	private class LeaderBoardRankUpdaterProgressDialogOnDismissListener
			implements OnDismissListener {
		@Override
		public void onDismiss(DialogInterface dialog) {
			mLeaderboardRankUpdaterProgressDialog = null;

			// Get the first and last position of the fragments which are
			// currently loaded by the view pager.
			int minPosition = Math.max(0, mViewPager.getCurrentItem()
					- mViewPager.getOffscreenPageLimit());
			int maxPosition = Math.min(
					mLeaderboardOverviewPagerAdapter.getCount(),
					mViewPager.getCurrentItem()
							+ mViewPager.getOffscreenPageLimit());

			for (int i = minPosition; i <= maxPosition; i++) {
				// Get the fragment on the selected position
				LeaderboardOverview leaderboardOverview = mLeaderboardOverviewPagerAdapter
						.getFragment(mViewPager, i, getSupportFragmentManager());

				// Refresh content of the fragment as the leaderboard data may
				// be changed since in case the fragment created.
				if (leaderboardOverview != null) {
					leaderboardOverview.refresh();
				}
			}
		}
	}

	public void viewLeaderboardDetails(String mLeaderboardId) {
		// Connect to the games client of the activity to start the Google Play
		// Services leaderboard intent.
		if (getGamesClient() != null) {
			Intent intent = getGamesClient().getLeaderboardIntent(
					mLeaderboardId);
			if (intent != null) {
				// The OnActivityResult is handled by superclass
				// GooglePlayServiceFragmentActivity. Therefore the return code
				// of that class is used here.
				startActivityForResult(intent,
						GooglePlayServiceFragmentActivity.RC_UNUSED);

				Preferences.getInstance().increaseLeaderboardsDetailsViewed();
			}
		}
	}
}
