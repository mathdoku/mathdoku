package net.mathdoku.plus.leaderboard.ui;

import net.mathdoku.plus.R;
import net.mathdoku.plus.leaderboard.LeaderboardConnector;
import net.mathdoku.plus.storage.database.LeaderboardRankDatabaseAdapter;
import net.mathdoku.plus.ui.GooglePlusSignInDialog;
import net.mathdoku.plus.ui.PuzzleFragmentActivity;
import net.mathdoku.plus.ui.base.GooglePlayServiceFragmentActivity;
import net.mathdoku.plus.util.FeedbackEmail;

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

import com.google.android.gms.games.GamesClient;

public class LeaderboardFragmentActivity extends
		GooglePlayServiceFragmentActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the leaderboard fragments. A
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative is used,
	 * which will keep every loaded fragment in memory.
	 */
	private LeaderboardFragmentPagerAdapter mLeaderboardFragmentPagerAdapter;

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

	// Reference to dialog for updating the leaderboards
	private LeaderboardRankUpdaterProgressDialog mLeaderboardRankUpdaterProgressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboard_activity_fragment);

		// Create the adapter that will return the leaderboard fragments.
		mLeaderboardFragmentPagerAdapter = new LeaderboardFragmentPagerAdapter(
				this, getSupportFragmentManager());

		// Set up the action bar.
		mActionBar = getActionBar();
		if (mActionBar != null) {
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
			mActionBar.setDisplayHomeAsUpEnabled(true);

			// Do not display a title as the leaderboard filter will act as
			// title.
			mActionBar.setDisplayShowTitleEnabled(false);

			mActionBar.setDisplayShowCustomEnabled(true);
			mActionBar.setCustomView(R.layout.leaderboard_action_bar_custom);
		}

		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the user swipes between the leaderboard fragments.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mLeaderboardFragmentPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// Get the fragment on the selected position
						LeaderboardFragment leaderboardFragment = mLeaderboardFragmentPagerAdapter
								.getFragment(mViewPager, position,
										getSupportFragmentManager());

						// Inform the fragment about the current filter as it
						// may be changed since in case the fragment was
						// displayed before.
						if (leaderboardFragment != null) {
							leaderboardFragment
									.setLeaderboardFilter(getLeaderboardFilter());
						}

						// When swiping between different leaderboard fragments,
						// select the corresponding tab.
						mActionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the leaderboard fragments, add a tab to the action bar.
		for (int i = 0; i < mLeaderboardFragmentPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the listener for when this tab is
			// selected.
			mActionBar.addTab(mActionBar.newTab()
					.setText(mLeaderboardFragmentPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		// Show the same page as last time (or the last tab if leaderboard were
		// not displayed before.
		int tab = mMathDokuPreferences.getLeaderboardsTabLastDisplayed();
		mViewPager.setCurrentItem(tab >= 0 ? tab
				: mLeaderboardFragmentPagerAdapter.getCount() - 1);

		setFilterSpinner(LeaderboardFilter.ALL_LEADERBOARDS);

		/*
		 * Styling of the pager tab strip is not possible from within code. See
		 * values-v14/styles.xml for styling of the action bar tab.
		 */
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

		// Store tab which is currently displayed.
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
			// This is called when the Home (Up) button is pressed in the action
			// bar. Create a simple intent that starts the hierarchical parent
			// activity and use NavUtils in the Support Package to ensure proper
			// handling of Up.
			Intent upIntent = new Intent(this, PuzzleFragmentActivity.class);
			if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
				// This activity is not part of the application's task, so
				// create a new task with a synthesized back stack.
				// If there are ancestor activities, they should be added here.
				TaskStackBuilder.create(this).addNextIntent(upIntent)
						.startActivities();
				finish();
			} else {
				// This activity is part of the application's task, so simply
				// navigate up to the hierarchical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
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
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Displays the Help Dialog.
	 */
	private void openHelpDialog() {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.leaderboard_help_dialog, null);

		new AlertDialog.Builder(this)
				.setTitle(
						getResources().getString(
								R.string.leaderboard_actionbar_title)
								+ " "
								+ getResources()
										.getString(R.string.action_help))
				.setIcon(R.drawable.icon)
				.setView(view)
				.setPositiveButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// Do nothing
							}
						}).show();
	}

	@Override
	public void onSignInFailed() {
		// Can not sign in to Google Play Services. Show sign in dialog.
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
		mLeaderboardRankUpdaterProgressDialog.show();
		mLeaderboardRankUpdaterProgressDialog
				.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						mLeaderboardRankUpdaterProgressDialog = null;
					}
				});
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

	@Override
	protected GamesClient getGamesClient() {
		return super.getGamesClient();
	}

	/**
	 * Initializes/refreshes the filter spinner.
	 * Returns: True in case the filter spinner should be shown. False
	 * otherwise.
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

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mLeaderboardFilter = LeaderboardFilter.values()[(int) id];

				// Get the fragment which is currently displayed
				LeaderboardFragment leaderboardFragment = mLeaderboardFragmentPagerAdapter
						.getFragment(mViewPager, mViewPager.getCurrentItem(),
								getSupportFragmentManager());

				// Inform the fragment about the change of filter.
				if (leaderboardFragment != null) {
					leaderboardFragment
							.setLeaderboardFilter(mLeaderboardFilter);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});
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
				.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						mLeaderboardRankUpdaterProgressDialog = null;

						// Get the first and last position of the fragments
						// which are currently loaded by the view pager.
						int minPosition = Math.max(
								0,
								mViewPager.getCurrentItem()
										- mViewPager.getOffscreenPageLimit());
						int maxPosition = Math.min(
								mLeaderboardFragmentPagerAdapter.getCount(),
								mViewPager.getCurrentItem()
										+ mViewPager.getOffscreenPageLimit());

						for (int i = minPosition; i <= maxPosition; i++) {
							// Get the fragment on the selected position
							LeaderboardFragment leaderboardFragment = mLeaderboardFragmentPagerAdapter
									.getFragment(mViewPager, i,
											getSupportFragmentManager());

							// Refresh content of the fragment as the
							// leaderboard data may be changed since in case the
							// fragment created.
							if (leaderboardFragment != null) {
								leaderboardFragment.refresh();
							}
						}
					}
				});
		mLeaderboardRankUpdaterProgressDialog.show();
	}
}
