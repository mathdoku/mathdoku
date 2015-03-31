package net.mathdoku.plus.puzzle.ui.theme;

import net.mathdoku.plus.R;

public class DarkTheme extends Theme {
    private static DarkTheme singletonDarkTheme;

    private DarkTheme() {
        // Prevent accidental instantiation
    }

    public static DarkTheme getInstance() {
        if (singletonDarkTheme == null) {
            singletonDarkTheme = new DarkTheme();
        }
        return singletonDarkTheme;
    }

    @Override
    public int getNameResId() {
        return R.string.theme_dark;
    }

    @Override
    public int getGridBackgroundColor() {
        return 0x00000000;
    }

    @Override
    public int getDefaultTextColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getDefaultCageBorderColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public boolean getBorderAntiAlias() {
        return true;
    }

    @Override
    public int getBadMathCageBorderColor() {
        return 0xFFBB0000;
    }

    @Override
    public int getSelectedCageBorderColor() {
        return 0xFFA0A030;
    }

    @Override
    public int getSelectedBadMathCageBorderColor() {
        return 0xFFBB0000;
    }

    @Override
    public int getInnerCageCellBorderColor() {
        return 0xff7f7f7f;
    }

    @Override
    public int getSelectedCellBorderColor() {
        return getSelectedCellBackgroundColor();
    }

    @Override
    public int getSelectedCellBackgroundColor() {
        return 0xFF545353;
    }

    @Override
    public int getNormalInputModeMonochromeResId() {
        return R.drawable.normal_input_mode_monochrome_dark;
    }

    @Override
    public int getMaybeInputModeMonochromeResId() {
        return R.drawable.maybe_input_mode_monochrome_dark;
    }
}