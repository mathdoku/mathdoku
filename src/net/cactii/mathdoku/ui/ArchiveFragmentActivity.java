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
import net.cactii.mathdoku.storage.database.GridDatabaseAdapter;
import net.cactii.mathdoku.ui.ArchiveFragmentStatePagerAdapter.SizeFilter;
import net.cactii.mathdoku.ui.ArchiveFragmentStatePagerAdapter.StatusFilter;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import android.widget.Toast;

public class ArchiveFragmentActivity extends FragmentActivity {

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.archive_activity_fragment);

		// Create an adapter that when requested, will return a fragment
		// representing an object in the collection.
		//
		// ViewPager and its adapters use support library fragments, so we must
		// use getSupportFragmentManager.
		mArchiveFragmentStatePagerAdapter = new ArchiveFragmentStatePagerAdapter(
				getSupportFragmentManager(), this);

		mShowStatusFilter = Preferences.getInstance(this)
				.showArchiveStatusFilter();
		mShowSizeFilter = Preferences.getInstance(this).showArchiveSizeFilter();

		mActionBar = getActionBar();
		if (mActionBar != null) {
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
			mActionBar.setSubtitle(getResources().getString(
					R.string.action_bar_subtitle_archive_fragment));
			mActionBar.setDisplayShowCustomEnabled(true);

			mActionBar.setCustomView(R.layout.archive_action_bar_custom);

			// Display spinners only if the archive is not empty.
			if (mArchiveFragmentStatePagerAdapter.getCount() > 0) {
				if (mShowStatusFilter) {
					setStatusSpinner();
				}
				if (mShowSizeFilter) {
					setSizeSpinner();
				}
			}
		}

		// Set up the ViewPager, attaching the adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mArchiveFragmentStatePagerAdapter);

		if (mArchiveFragmentStatePagerAdapter.getCount() == 0) {
			Toast.makeText(ArchiveFragmentActivity.this,
					"Archive is still empty. Create and play a game first.", // TODO:
																				// i18n
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResumeFragments() {
		boolean showStatusFilter = Preferences.getInstance(this)
				.showArchiveStatusFilter();
		boolean showSizeFilter = Preferences.getInstance(this)
				.showArchiveSizeFilter();
		if (mShowStatusFilter != showStatusFilter
				|| mShowSizeFilter != showSizeFilter) {
			mShowStatusFilter = showStatusFilter;
			mShowSizeFilter = showSizeFilter;

			// Reset all filters in case the settings for displaying a filter
			// have been changed.
			mArchiveFragmentStatePagerAdapter.setStatusFilter(StatusFilter.ALL);
			mArchiveFragmentStatePagerAdapter.setSizeFilter(SizeFilter.ALL);

			// Refresh the spinners
			setStatusSpinner();
			setSizeSpinner();
		}
		super.onResumeFragments();
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
		case R.id.action_settings:
			startActivity(new Intent(this, ArchivePreferenceActivity.class));
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
		if (!mShowStatusFilter) {
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
					R.array.archiveStatusFilter)[usedStatuses[i].ordinal()];
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
				// Check if value for status spinner has changed.
				if (usedStatuses[(int) id] != mArchiveFragmentStatePagerAdapter
						.getStatusFilter()) {
					mArchiveFragmentStatePagerAdapter
							.setStatusFilter(usedStatuses[(int) id]);
					setSizeSpinner();
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
		if (!mShowSizeFilter) {
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
					R.array.archiveSizeFilter)[usedSizes[i].ordinal()];
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
				// Check if value for size spinner has changed.
				if (usedSizes[(int) id] != mArchiveFragmentStatePagerAdapter
						.getSizeFilter()) {
					mArchiveFragmentStatePagerAdapter
							.setSizeFilter(usedSizes[(int) id]);
					if (mShowStatusFilter) {
						setStatusSpinner();
					}
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
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}
}