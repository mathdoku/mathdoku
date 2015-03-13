package net.mathdoku.plus.config;

/**
 * See $project/app/src-templates/.../Config.java for the template of the configuration class. When building the project
 * with gradle this template class will be copied to the source directory and the @CONFIG.APP_MODE@ will be replaced
 * with a specific value.
 * <p/>
 * Checks on variable {@link Config#APP_MODE} should always be made in such a way that the result can be determined at
 * compile time. In this way the enclosed block will not be included in the compiled case when the condition for
 * executing the block evaluates to false. Example of intended usage:
 * <p/>
 * <pre>
 * if (Config.APP_MODE == AppMode.DEVELOPMENT) {
 *     // code which should only be included in case the app is used for unit
 *     //testing
 * }
 * </pre>
 * <p/>
 * DO NOT ALTER THE SOURCE FILE DIRECTLY IN CASE YOU WANT TO PERSIST YOUR CHANGES. PERSISTENT CHANGES SHOULD ALWAYS BE
 * MADE TO THE CORRESPONDING TEMPLATE FILE IN FOLDER $project/app/src-templates/.../Config.java.
 */
public class Config {
    @SuppressWarnings("unused")
    private static final String TAG = Config.class.getName();

    public enum AppMode {
        DEVELOPMENT,
        PRODUCTION
    }

    public static final AppMode APP_MODE = AppMode.DEVELOPMENT;

    public static boolean EnabledInDevelopmentModeOnly() {
        return Config.APP_MODE == AppMode.DEVELOPMENT;
    }

    // Convenience method for code readability of initialization of debug variables.
    public static boolean DisabledAlways() {
        return false;
    }
}
