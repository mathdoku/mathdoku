package net.mathdoku.plus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

/**
 * This class contains unit tests for upgrading from version 594 to the current
 * development version.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class PreferencesUpgradeVersion594ToLatestVersionTest extends
		PreferencesUpgradeVersion595ToLatestVersionTest {
	private int APP_VERSION = 594;

	@Before
	public void setUp() throws Exception {
		super.setUp(this.getClass().getCanonicalName(), APP_VERSION);
		ImportPreferenceFile("user_preferences_version_594.csv");
	}

	@Test
	public void leaderboardsDetailsViewed_GetSetGet() throws Exception {
		assertThatPreferenceIsAdded("leaderboard_details_viewed_counter");
	}

	@Test
	public void leaderboardsGamesCreated_GetSetGet() throws Exception {
		assertThatPreferenceIsAdded("leaderboard_games_created_counter");
	}

	@Test
	public void leaderboardsOverviewViewed_GetSetGet() throws Exception {
		assertThatPreferenceIsAdded("leaderboard_overview_viewed");
	}
}
