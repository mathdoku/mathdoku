package net.mathdoku.plus;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Test;

import csvimporter.PreferenceCsvImporter;
import robolectric.TestRunnerHelper;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PreferencesUpgradeToLatestVersionTest {
	protected Preferences preferences;
	private String fileNameImportedPreferencesCSV;
	private int appVersion;
	private static int testCounter = 0;
	private String preferencesBeforeUpgrade;
	private boolean instantiatedBySubclass = false;

	protected void setUp(String subclassName, int appVersion) {
		this.appVersion = appVersion;
		TestRunnerHelper.setup(subclassName);
		preferences = Preferences.getInstance(TestRunnerHelper.getActivity());
		testCounter++;
		instantiatedBySubclass = true;
	}

	protected void ImportPreferenceFile(String fileNamePreferencesCSV) {
		clearAllPreferences();
		new PreferenceCsvImporter(getPathToFile(fileNamePreferencesCSV),
				preferences.mSharedPreferences).importFile();
		fileNameImportedPreferencesCSV = fileNamePreferencesCSV;
		preferencesBeforeUpgrade = preferences.toString();
	}

	/**
	 * Removes all existing preferences (a possible remainder of a previous unit test).
 	 */
	private void clearAllPreferences() {
		preferences.mSharedPreferences.edit().clear().commit();
	}

	private String getPathToFile(String filename) {
		return "net/mathdoku/plus/preferences/" + filename;
	}

	@After
	public void tearDown() throws Exception {
		TestRunnerHelper.tearDown();
	}

	protected void upgradePreferences() {
		preferences.upgrade(appVersion, Integer.MAX_VALUE);
	}

	@Test
	public void upgrade_AppVersion_VersionIsSetCorrectlyInPreferences()
			throws Exception {
		// To prevent an Initialization error, the class needs at least one
		// test. But this test should only be executed in case this class was
		// instantiated by a subclass as it will fail when nog preference file
		// was imported.
		if (instantiatedBySubclass) {
			assertThat(preferences.getCurrentInstalledVersion(), is(appVersion));
		}
	}

	protected void assertThatLongPreferenceIsNotChanged(Long actualValue,
			Matcher<Long> matcher) {
		assertThat(printTestCounter(), actualValue, matcher);
		upgradePreferences();
		assertThat(printTestCounter(), actualValue, matcher);
	}

	protected void assertThatPreferenceIsRenamed(String oldPreferenceName, String newPreferenceName) {
		assertThatPreferenceExists(oldPreferenceName);
		assertThatPreferenceDoesNotExists(newPreferenceName);
		upgradePreferences();
		assertThatPreferenceDoesNotExists(oldPreferenceName);
		assertThatPreferenceExists(newPreferenceName);
	}

	protected void assertThatPreferenceIsRemoved(String preferenceName) {
		assertThatPreferenceExists(preferenceName);
		upgradePreferences();
		assertThatPreferenceDoesNotExists(preferenceName);
	}

	private void assertThatPreferenceExists(String preferenceName) {
		assertThat(printTestCounter(), containsPreferenceWithName(preferenceName), is(true));
	}

	private boolean containsPreferenceWithName(String preferenceName) {
		return preferences.mSharedPreferences.contains(preferenceName);
	}

	private void assertThatPreferenceDoesNotExists(String preferenceName) {
		assertThat(printTestCounter(), containsPreferenceWithName(preferenceName),
				is(false));
	}

	protected void assertThatPreferenceIsAdded(String preferenceName) {
		assertThatPreferenceDoesNotExists(preferenceName);
		upgradePreferences();
		assertThatPreferenceExists(preferenceName);
	}

	private String printTestCounter() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Test sequence number: ");
		stringBuilder.append(testCounter);
		stringBuilder.append(" failed.\n");
		stringBuilder
				.append("All preferences before upgrade as imported from file '");
		stringBuilder.append(fileNameImportedPreferencesCSV);
		stringBuilder.append("'\n");
		stringBuilder.append(preferencesBeforeUpgrade);
		stringBuilder.append("\n");
		stringBuilder.append("All preferences after upgrade:");
		stringBuilder.append(preferences.toString());
		stringBuilder.append("\n");
		return stringBuilder.toString();
	}
}
