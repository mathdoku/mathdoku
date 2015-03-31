package net.mathdoku.plus.puzzle.ui.theme;

import net.mathdoku.plus.R;

public class LightTheme extends Theme {
    private static LightTheme singletonLightTheme;

    private LightTheme() {
        // Prevent accidental instantiation
    }

    public static LightTheme getInstance() {
        if (singletonLightTheme == null) {
            singletonLightTheme = new LightTheme();
        }
        return singletonLightTheme;
    }

    @Override
    public int getNameResId() {
        return R.string.theme_light;
    }

    @Override
    public int getGridBackgroundColor() {
        return 0xFFFFFFFF;
    }

    @Override
    public int getDefaultTextColor() {
        return 0xFF212121;
    }

    @Override
    public int getDefaultCageBorderColor() {
        return 0xFF000000;
    }

    @Override
    public boolean getBorderAntiAlias() {
        return false;
    }

    @Override
    public int getBadMathCageBorderColor() {
        return 0xffff4444;
    }

    @Override
    public int getSelectedCageBorderColor() {
        return 0xFF000000;
    }

    @Override
    public int getSelectedBadMathCageBorderColor() {
        return 0xFFff4444;
    }

    @Override
    public int getInnerCageCellBorderColor() {
        return 0x79000000;
    }

    @Override
    public int getSelectedCellBorderColor() {
        return getSelectedCellBackgroundColor();
    }

    @Override
    public int getSelectedCellBackgroundColor() {
        return 0xFFE6E6E6;
    }

    @Override
    public int getNormalInputModeMonochromeResId() {
        return R.drawable.normal_input_mode_monochrome_light;
    }

    @Override
    public int getMaybeInputModeMonochromeResId() {
        return R.drawable.maybe_input_mode_monochrome_light;
    }
}
