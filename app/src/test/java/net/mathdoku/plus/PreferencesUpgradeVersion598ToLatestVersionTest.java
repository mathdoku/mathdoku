package net.mathdoku.plus;

import net.mathdoku.plus.tip.TipDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import robolectric.RobolectricGradleTestRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * This class contains unit tests for upgrading from version 598 to the current development version.
 */
@RunWith(RobolectricGradleTestRunner.class)
public class PreferencesUpgradeVersion598ToLatestVersionTest extends PreferencesUpgradeToLatestVersionTest {
    private int APP_VERSION = 598;

    @Before
    public void setUp() throws Exception {
        super.setUp(this.getClass()
                            .getCanonicalName(), APP_VERSION, "user_preferences_version_598.csv");
    }

    @Test
    public void upgrade_ArchiveAvailable_PreferenceNotRenamed() throws Exception {
        assertThatLongPreferenceIsNotChanged(preferences.getTipLastDisplayTime("ArchiveAvailable"), is(1000000000001L));
    }

    @Test
    public void upgrade_BadCageMath_PreferenceNotRenamed() throws Exception {
        assertThatLongPreferenceIsNotChanged(preferences.getTipLastDisplayTime("BadCageMath"), is(1000000000002L));
    }

    @Test
    public void upgrade_CheatCheckProgress_PreferenceNotRenamed() throws Exception {
        assertThatLongPreferenceIsNotChanged(preferences.getTipLastDisplayTime("Cheat.CheckProgress"),
                                             is(1000000000003L));
    }

    @Test
    public void upgrade_TipCopyCellValuesDisplayAgain_PreferenceTipLastDisplayTimeIsRenamed() throws Exception {
        String oldTipName = "Tip.CopyCellValues.DisplayAgain";
        String newTipName = "CopyCellValues";

        assertThatGetTipLastDisplayTimeIsRenamed(oldTipName, newTipName);
    }

    private void assertThatGetTipLastDisplayTimeIsRenamed(String oldTipName, String newTipName) {
        long oldValue = preferences.getTipLastDisplayTime(oldTipName);
        String oldName = TipDialog.getPreferenceStringLastDisplayTime(oldTipName);
        String newName = TipDialog.getPreferenceStringLastDisplayTime(newTipName);
        assertThatPreferenceIsRenamed(oldName, newName);
        assertThat(preferences.getTipLastDisplayTime(newTipName), is(oldValue));
    }

    @Test
    public void upgrade_TipOrderOfValuesInCageDisplayAgain_PreferenceTipLastDisplayTimeIsRenamed() throws Exception {
        String oldTipName = "Tip.OrderOfValuesInCage.DisplayAgain";
        String newTipName = "OrderOfValuesInCage";

        assertThatGetTipLastDisplayTimeIsRenamed(oldTipName, newTipName);
    }

    @Test
    public void upgrade_TipTipDuplicateValueDisplayAgain_PreferenceTipLastDisplayTimeIsRenamed() throws Exception {
        String oldTipName = "Tip.TipDuplicateValue.DisplayAgain";
        String newTipName = "DuplicateValue";

        assertThatGetTipLastDisplayTimeIsRenamed(oldTipName, newTipName);

    }

    @Test
    public void upgrade_TipTipIncorrectValueDisplayAgain_PreferenceTipLastDisplayTimeIsRenamed() throws Exception {
        String oldTipName = "Tip.TipIncorrectValue.DisplayAgain";
        String newTipName = "IncorrectValue";

        assertThatGetTipLastDisplayTimeIsRenamed(oldTipName, newTipName);
    }

    @Test
    public void upgrade_TipTipStatisticsDisplayAgain_PreferenceTipLastDisplayTimeIsRenamed() throws Exception {
        String oldTipName = "Tip.TipStatistics.DisplayAgain";
        String newTipName = "Statistics";

        assertThatGetTipLastDisplayTimeIsRenamed(oldTipName, newTipName);
    }

    @Test
    public void upgrade_TipCopyCellValuesDisplayAgain_PreferenceTipLastDisplayAgainIsRenamed() throws Exception {
        String oldTipName = "Tip.CopyCellValues.DisplayAgain";
        String newTipName = "CopyCellValues";

        assertThatGetTipDisplayAgainIsRenamed(oldTipName, newTipName);
    }

    private void assertThatGetTipDisplayAgainIsRenamed(String oldTipName, String newTipName) {
        boolean oldValue = preferences.getTipDisplayAgain(oldTipName);
        String oldName = TipDialog.getPreferenceStringDisplayTipAgain(oldTipName);
        String newName = TipDialog.getPreferenceStringDisplayTipAgain(newTipName);
        assertThatPreferenceIsRenamed(oldName, newName);
        assertThat(preferences.getTipDisplayAgain(newTipName), is(oldValue));
    }

    @Test
    public void upgrade_TipOrderOfValuesInCageDisplayAgain_PreferenceTipLastDisplayAgainIsRenamed() throws Exception {
        String oldTipName = "Tip.OrderOfValuesInCage.DisplayAgain";
        String newTipName = "OrderOfValuesInCage";

        assertThatGetTipDisplayAgainIsRenamed(oldTipName, newTipName);
    }

    @Test
    public void upgrade_TipTipDuplicateValueDisplayAgain_PreferenceTipLastDisplayAgainIsRenamed() throws Exception {
        String oldTipName = "Tip.TipDuplicateValue.DisplayAgain";
        String newTipName = "DuplicateValue";

        assertThatGetTipDisplayAgainIsRenamed(oldTipName, newTipName);
    }

    @Test
    public void upgrade_TipTipIncorrectValueDisplayAgain_PreferenceTipLastDisplayAgainIsRenamed() throws Exception {
        String oldTipName = "Tip.TipIncorrectValue.DisplayAgain";
        String newTipName = "IncorrectValue";

        assertThatGetTipDisplayAgainIsRenamed(oldTipName, newTipName);
    }

    @Test
    public void upgrade_TipTipStatisticsDisplayAgain_PreferenceTipLastDisplayAgainIsRenamed() throws Exception {
        String oldTipName = "Tip.TipStatistics.DisplayAgain";
        String newTipName = "Statistics";

        assertThatGetTipDisplayAgainIsRenamed(oldTipName, newTipName);
    }
}
