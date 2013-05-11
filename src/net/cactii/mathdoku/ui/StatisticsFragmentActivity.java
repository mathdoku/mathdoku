package net.cactii.mathdoku.ui;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.util.UsageLog;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

public class StatisticsFragmentActivity extends FragmentActivity implements
		ActionBar.TabListener {
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the statistics fragments. A
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative is used,
	 * which will keep every loaded fragment in memory.
	 */
	StatisticsFragmentPagerAdapter mStatisticsFragmentPagerAdapter;

	/**
	 * The {@link ViewPager} that will display the statistics fragments , one at
	 * a time.
	 */
	ViewPager mViewPager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics_activity_fragment);

		// Determine if specific statistics for a grid have to be shown.
		Intent intent = getIntent();
		int mGridStatisticsId = -1;
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				mGridStatisticsId = extras.getInt(StatisticsGameFragment.BUNDLE_KEY_STATISTICS_ID, -1);
			}
		}

		// Create the adapter that will return the statistics fragments.
		mStatisticsFragmentPagerAdapter = new StatisticsFragmentPagerAdapter(
				getSupportFragmentManager(), mGridStatisticsId);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayHomeAsUpEnabled(true);

		// Set up the ViewPager, attaching the adapter and setting up a listener
		// for when the user swipes between the statistics fragments.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mStatisticsFragmentPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between different statistics fragments,
						// select the corresponding tab.
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the statistics fragments, add a tab to the action bar.
		for (int i = 0; i < mStatisticsFragmentPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the listener for when this tab is
			// selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mStatisticsFragmentPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
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
		getMenuInflater().inflate(R.menu.statistics_menu, menu);
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
				TaskStackBuilder.create(this)
						.addNextIntent(upIntent).startActivities();
				finish();
			} else {
				// This activity is part of the application's task, so simply
				// navigate up to the hierarchical parent activity.
				NavUtils.navigateUpTo(this, upIntent);
			}
			return true;
		case R.id.menu_statistics_options:
			UsageLog.getInstance().logFunction("Menu.ViewStatisticsOptions");
			startActivity(new Intent(this, StatisticsPreferenceActivity.class));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}