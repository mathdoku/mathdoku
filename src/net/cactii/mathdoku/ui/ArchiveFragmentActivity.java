/*
 * Copyright 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.Preferences;
import net.cactii.mathdoku.R;
import net.cactii.mathdoku.painter.PagerTabStripPainter;
import net.cactii.mathdoku.painter.Painter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter.SizeFilter;
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter.StatusFilter;
import net.cactii.mathdoku.util.FeedbackEmail;
import net.cactii.mathdoku.util.SharedPuzzle;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class ArchiveFragmentActivity extends AppFragmentActivity {

	public static final String BUNDLE_KEY_SOLVING_ATTEMPT_ID = "solvingAttemptId";

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments representing each object in a collection. We use a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter} derivative,
	 * which will destroy and re-create fragments as needed, saving and
	 * restoring their state in the process. This is important to conserve
	 * memory and is a best practice when allowing navigation between objects in
	 * a potentially large collection.
	 */
	ArchiveFragmentStatePagerAdapter mArchiveFragmentStatePagerAdapter;

	/**
	 * The {@link android.support.v4.view.ViewPager} that will display the
	 * object collection.
	 */
	ViewPager mViewPager;
	ActionBar mActionBar;

	// Should filters be shown?
	private boolean mShowStatusFilter;
	private boolean mShowSizeFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.archive_activity_fragment);

		// Create an adapter that when requested, will return a fragment
		// representing an object in the collection.
		// ViewPager and its adapters use support library fragments, so we must
		// use getSupportFragmentManager.
		mArchiveFragmentStatePagerAdapter = new ArchiveFragmentStatePagerAdapter(
				getSupportFragmentManager(), this);

		// Get preferences for displaying the filter.
		Preferences preferences = Preferences.getInstance(this);
		mShowStatusFilter = preferences.showArchiveStatusFilter();
		mShowSizeFilter = preferences.showArchiveSizeFilter();
		mArchiveFragmentStatePagerAdapter.setStatusFilter(preferences
				.getArchiveStatusFilterLastValueUsed());
		mArchiveFragmentStatePagerAdapter.setSizeFilter(preferences
				.getArchiveSizeFilterLastValueUsed());

		mActionBar = getActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			mActionBar.setTitle(getResources().getString(
					R.string.archive_action_bar_title));
			mActionBar.setDisplayShowCustomEnabled(true);

			mActionBar.setCustomView(R.layout.archive_action_bar_custom);

			setStatusSpinner();
			setSizeSpinner();
		}

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mArchiveFragmentStatePagerAdapter);

		// Set up the pager tab strip
		final PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
		pagerTabStrip.setDrawFullUnderline(false);

		// This pager contains a maximum of 3 visible items. The selected tab
		// will always be displayed in the middle. Hide the tab indicator by
		// setting color identical to background color.
		PagerTabStripPainter pagerTabStripPainter = Painter.getInstance()
				.getPagerTabStripPainter();
		pagerTabStrip.setTabIndicatorColor(pagerTabStripPainter
				.getBackgroundColor());
		pagerTabStrip.setBackgroundColor(pagerTabStripPainter
				.getBackgroundColor());
		pagerTabStrip.setTextColor(pagerTabStripPainter.getTextColor());

		// Non primary items are semi transparent.
		pagerTabStrip.setNonPrimaryAlpha(0.75f);

		// In case a solving attempt has been specified in the bundle, this
		// solving attempt will be showed as selected grid as long as it does
		// meet the selection criteria of the filters.
		Intent intent = getIntent();
		if (intent != null) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				int solvingAttemptId = bundle
						.getInt(BUNDLE_KEY_SOLVING_ATTEMPT_ID);
				if (solvingAttemptId >= 0
						&& mArchiveFragmentStatePagerAdapter
								.getPositionOfGridId(solvingAttemptId) >= 0) {
					preferences
							.setArchiveSelectedGridIdLastValueUsed(solvingAttemptId);
				}
			}
		}
	}

	@Override
	protected void onResumeFragments() {
		Preferences preferences = Preferences.getInstance(this);

		// Check for changes in visibility of status spinner. Reset the filters
		// for which the visibility changes.
		boolean showStatusFilterNew = preferences.showArchiveStatusFilter();
		boolean setSpinners = false;
		if (mShowStatusFilter != showStatusFilterNew) {
			mShowStatusFilter = showStatusFilterNew;
			mArchiveFragmentStatePagerAdapter.setStatusFilter(StatusFilter.ALL);
			setSpinners = true;
		}
		boolean showSizeFilterNew = preferences.showArchiveSizeFilter();
		if (mShowSizeFilter != showSizeFilterNew) {
			mShowSizeFilter = showSizeFilterNew;
			mArchiveFragmentStatePagerAdapter.setSizeFilter(SizeFilter.ALL);
			setSpinners = true;
		}

		// After all filters have been set to possible new values, the spinners
		// can set.
		if (setSpinners) {
			setStatusSpinner();
			setSizeSpinner();
		}

		// Select the same grid which was selected before. If not possible,
		// the last page will be shown.
		selectGridId(preferences.getArchiveSelectedGridIdLastValueUsed());

		super.onResumeFragments();
	}

	@Override
	protected void onPause() {
		// Save preferences
		Preferences preferences = Preferences.getInstance(this);
		preferences
				.setArchiveStatusFilterLastValueUsed(mArchiveFragmentStatePagerAdapter
						.getStatusFilter());
		preferences
				.setArchiveSizeFilterLastValueUsed(mArchiveFragmentStatePagerAdapter
						.getSizeFilter());
		preferences
				.setArchiveSelectedGridIdLastValueUsed(getCurrentSelectedGridId());

		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.archive_menu, menu);
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
		case R.id.action_share:
			new SharedPuzzle(this).addStatisticsChartsAsAttachments(
					this.getWindow().getDecorView()).share(
					getSolvingAttemptIdForCurrentSelectedGrid());
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, ArchivePreferenceActivity.class));
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
	 * Initializes/refreshes the status spinner.
	 * 
	 * Returns: True in case the status spinner should be shown. False
	 * otherwise.
	 */
	protected void setStatusSpinner() {
		Spinner spinner = (Spinner) mActionBar.getCustomView().findViewById(
				R.id.spinner_status);
		if (!mShowStatusFilter
				|| mArchiveFragmentStatePagerAdapter.getCount() == 0) {
			spinner.setVisibility(View.GONE);
			return;
		}

		// Determine which statuses are actually used for the currently
		// selected size filter.
		GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
		final StatusFilter[] usedStatuses = gridDatabaseAdapter
				.getUsedStatuses(mArchiveFragmentStatePagerAdapter
						.getSizeFilter());

		// Load the list of descriptions for statuses actually used into the
		// array adapter.
		String[] usedStatusesDescription = new String[usedStatuses.length];
		for (int i = 0; i < usedStatuses.length; i++) {
			usedStatusesDescription[i] = getResources().getStringArray(
					R.array.archive_status_filter)[usedStatuses[i].ordinal()];
		}
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, usedStatusesDescription);
		adapterStatus
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Build the spinner
		spinner.setAdapter(adapterStatus);

		// Restore selected status
		StatusFilter selectedStatusFilter = mArchiveFragmentStatePagerAdapter
				.getStatusFilter();
		for (int i = 0; i < usedStatuses.length; i++) {
			if (usedStatuses[i] == selectedStatusFilter) {
				spinner.setSelection(i);
				break;
			}
		}

		// Hide spinner if only two choices are available. As one of those
		// choices is always "ALL" the choices will result in an identical
		// selection.
		spinner.setVisibility((usedStatuses.length <= 2 ? View.GONE
				: View.VISIBLE));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// Get the selected status
				StatusFilter statusFilter = usedStatuses[(int) id];

				// Check if value for status spinner has changed.
				if (statusFilter != mArchiveFragmentStatePagerAdapter
						.getStatusFilter()) {
					// Remember currently displayed grid id.
					int gridId = getCurrentSelectedGridId();

					// Refresh pager adapter with new status.
					mArchiveFragmentStatePagerAdapter
							.setStatusFilter(statusFilter);

					// Refresh the size spinner as the content of the spinners
					// are related.
					setSizeSpinner();

					// If possible select the grid id which was selected before
					// changing the spinner(s). Otherwise select last page.
					selectGridId(gridId);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});
	}

	/**
	 * Initializes/refreshes the sizes spinner.
	 * 
	 * Returns: True in case the sizes spinner should be shown. False otherwise.
	 */
	protected void setSizeSpinner() {
		Spinner spinner = (Spinner) mActionBar.getCustomView().findViewById(
				R.id.spinner_size);
		if (!mShowSizeFilter
				|| mArchiveFragmentStatePagerAdapter.getCount() == 0) {
			spinner.setVisibility(View.GONE);
			return;
		}

		// Determine which sizes are actually used for the currently
		// selected status filter.
		GridDatabaseAdapter gridDatabaseAdapter = new GridDatabaseAdapter();
		final SizeFilter[] usedSizes = gridDatabaseAdapter
				.getUsedSizes(mArchiveFragmentStatePagerAdapter
						.getStatusFilter());

		// Load the list of descriptions for sizes actually used into the
		// array adapter.
		String[] usedSizesDescription = new String[usedSizes.length];
		for (int i = 0; i < usedSizes.length; i++) {
			usedSizesDescription[i] = getResources().getStringArray(
					R.array.archive_size_filter)[usedSizes[i].ordinal()];
		}
		ArrayAdapter<String> adapterStatus = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, usedSizesDescription);
		adapterStatus
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Build the spinner
		spinner.setAdapter(adapterStatus);

		// Restore selected size
		SizeFilter selectedSizeFilter = mArchiveFragmentStatePagerAdapter
				.getSizeFilter();
		for (int i = 0; i < usedSizes.length; i++) {
			if (usedSizes[i] == selectedSizeFilter) {
				spinner.setSelection(i);
				break;
			}
		}

		// Hide spinner if only two choices are available. As one of those
		// choices is always "ALL" the choices will result in an identical
		// selection.
		spinner.setVisibility((usedSizes.length <= 2 ? View.GONE : View.VISIBLE));

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// Remember currently displayed grid id.
				int gridId = getCurrentSelectedGridId();

				// Get the selected status
				SizeFilter sizeFilter = usedSizes[(int) id];

				// Check if value for status spinner has changed.
				if (sizeFilter != mArchiveFragmentStatePagerAdapter
						.getSizeFilter()) {
					// Refresh pager adapter with new status.
					mArchiveFragmentStatePagerAdapter.setSizeFilter(sizeFilter);

					// Refresh the status spinner as the content of the spinners
					// are related.
					setStatusSpinner();

					// If possible select the grid id which was selected before
					// changing the spinner(s). Otherwise select last page.
					selectGridId(gridId);
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});
	}

	/**
	 * Displays the help dialog for the archive activity.
	 */
	private void openHelpDialog() {
		// Get view and put relevant information into the view.
		LayoutInflater li = LayoutInflater.from(this);
		View view = li.inflate(R.layout.archive_help_dialog, null);

		new AlertDialog.Builder(this)
				.setTitle(
						getResources().getString(R.string.action_archive)
								+ " "
								+ getResources()
										.getString(R.string.action_help))
				.setIcon(R.drawable.icon)
				.setView(view)
				.setNegativeButton(R.string.dialog_general_button_close,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	/**
	 * Get the grid id which is currently displayed in the archive.
	 * 
	 * @return The grid id which is currently displayed in the archive.
	 */
	private int getCurrentSelectedGridId() {
		if (mArchiveFragmentStatePagerAdapter != null && mViewPager != null) {
			return mArchiveFragmentStatePagerAdapter.getGridId(mViewPager
					.getCurrentItem());
		} else {
			return -1;
		}
	}

	/**
	 * Get the solving attempt id of the grid which is currently displayed in
	 * the archive.
	 * 
	 * @return The solving attempt id of the grid which is currently displayed
	 *         in the archive.
	 */
	private int getSolvingAttemptIdForCurrentSelectedGrid() {
		if (mArchiveFragmentStatePagerAdapter != null && mViewPager != null) {
			return mArchiveFragmentStatePagerAdapter
					.getSolvingAttemptId(mViewPager.getCurrentItem());
		} else {
			return -1;
		}
	}

	/**
	 * Select the page with the given grid id. In case the specified grid id was
	 * not found, the last page is selected.
	 * 
	 * @param gridId
	 *            The grid id for which the corresponding page in the archive
	 *            has to be selected.
	 * @return True in case the grid id has been found and is selected. False
	 *         otherwise.
	 */
	private boolean selectGridId(int gridId) {
		// Get the position of the grid in the adapter.
		int position = mArchiveFragmentStatePagerAdapter
				.getPositionOfGridId(gridId);

		// In case the grid id is found in the adapter it is selected.
		if (position >= 0) {
			mViewPager.setCurrentItem(position);
			return true;
		} else {
			// Show last page
			mViewPager.setCurrentItem(mArchiveFragmentStatePagerAdapter
					.getCount() - 1);
			return false;
		}
	}

	/**
	 * Reload the game currently displayed in the archive.
	 * 
	 * @param view
	 */
	public void onClickReloadGame(View view) {
		Intent intent = new Intent();
		intent.putExtra(BUNDLE_KEY_SOLVING_ATTEMPT_ID,
				getSolvingAttemptIdForCurrentSelectedGrid());
		setResult(RESULT_OK, intent);

		// Finish the archive activity
		finish();
	}

	public int getViewPagerCurrentPosition() {
		return (mViewPager == null ? -1 : mViewPager.getCurrentItem());
	}
}