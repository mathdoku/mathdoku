package net.mathdoku.plus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

/**
 * This class contains unit tests for upgrading from version 594 to the current development version.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class PreferencesUpgradeVersion595ToLatestVersionTest extends PreferencesUpgradeVersion598ToLatestVersionTest {
    private int APP_VERSION = 595;

    @Before
    public void setUp() throws Exception {
        super.setUp(this.getClass()
                            .getCanonicalName(), APP_VERSION);
        ImportPreferenceFile("user_preferences_version_595.csv");
    }

    @Test
    public void puzzleParameterSize_IsRemoved() throws Exception {
        assertThatPreferenceIsRemoved("puzzle_parameter_size");
    }

    @Test
    public void archiveSizeFilterLastValue_IsRemoved() throws Exception {
        assertThatPreferenceIsRemoved("archive_size_filter_last_value");
    }

    @Test
    public void archiveSettingSizeFilterSizeVisible_IsRemoved() throws Exception {
        assertThatPreferenceIsRemoved("archive_setting_size_filter_size_visible");
    }

    @Test
    public void archiveGridTypeFilterLastValue_IsAdded() throws Exception {
        assertThatPreferenceIsAdded("archive_grid_type_filter_last_value");
    }

    @Test
    public void archiveSettingGridTypeFilterVisible_IsAdded() throws Exception {
        assertThatPreferenceIsAdded("archive_setting_grid_type_filter_visible");
    }

    @Test
    public void puzzleParameterGridSize_IsAdded() throws Exception {
        assertThatPreferenceIsAdded("puzzle_parameter_grid_size");
    }
}
