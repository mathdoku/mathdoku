package net.mathdoku.plus.leaderboard.ui;

import android.content.Intent;
import android.os.Bundle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

import robolectric.RobolectricGradleTestRunner;

@RunWith(RobolectricGradleTestRunner.class)
public class LeaderboardOverviewActivityTest {
	private LeaderboardOverviewActivity leaderboardOverviewActivity;
	private ActivityController<LeaderboardOverviewActivity> controller;

	@Before
	public void setUp() throws Exception {
		controller = Robolectric
				.buildActivity(LeaderboardOverviewActivity.class);
	}

	@After
	public void tearDown() throws Exception {
		controller.destroy();
	}

	private void createWithIntent(String myExtra) {
		Intent intent = new Intent(Robolectric.application,
				LeaderboardOverviewActivity.class);
		Bundle extras = new Bundle();
		extras.putString("myExtra", myExtra);
		intent.putExtras(extras);
		leaderboardOverviewActivity = controller
				.withIntent(intent)
				.create()
				.start()
				.visible()
				.get();
	}

	@Test
	public void createsAndDestroysActivity() {
		createWithIntent("foo");
		// Assertions go here
	}

	@Test
	public void pausesAndResumesActivity() {
		createWithIntent("foo");
		controller.pause().resume();
		// Assertions go here
	}

	@Test
	public void recreatesActivity() {
		createWithIntent("foo");
		leaderboardOverviewActivity.recreate();
		// Assertions go here
	}

	@Test
	public void onCreate() throws Exception {

	}

	@Test
	public void onResumeFragments() throws Exception {

	}

	@Test
	public void onPause() throws Exception {

	}

	@Test
	public void onStop() throws Exception {

	}

	@Test
	public void onTabUnselected() throws Exception {

	}

	@Test
	public void onTabSelected() throws Exception {

	}

	@Test
	public void onTabReselected() throws Exception {

	}

	@Test
	public void onCreateOptionsMenu() throws Exception {

	}

	@Test
	public void onOptionsItemSelected() throws Exception {

	}

	@Test
	public void onSignInFailed() throws Exception {

	}

	@Test
	public void onAutoSignInSucceeded() throws Exception {

	}

	@Test
	public void onUserInitiatedSignInSucceeded() throws Exception {

	}

	@Test
	public void getGamesClient() throws Exception {

	}

	@Test
	public void setFilterSpinner() throws Exception {

	}

	@Test
	public void getLeaderboardFilter() throws Exception {

	}
}
