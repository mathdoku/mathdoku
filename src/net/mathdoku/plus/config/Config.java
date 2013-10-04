package net.mathdoku.plus.config;

/**
 * See $root/config/Config.java for the template of the configuration class.
 * When building the project with ant this template class (after substituting
 * the variables) will be copied to the source directory.
 * 
 * Checks on variable {@link Config#mAppMode} should always be made in such a way that
 * the result can be determined at compile time. In this way the enclosed block
 * will not be included in the compiled case when the condition for executing
 * the block evaluates to false. Example of intended usage:
 * 
 * <pre class="prettyprint">
 * if (Config.mAppMode == AppMode.UNIT_TESTING) {
 * 	// code which should only be included in case the app is used for unit
 * 	// testing
 * }
 * </pre>
 *
 * DO NOT ALTER THE SOURCE FILE DIRECTLTY IN CASE YOU WANT TO PERSIST YOUR
 * CHANGES. PERSISTENT CHANGES SHOULD ALWAYS BE MADE TO THE TEMPLATE FILE IN
 * $root/config/Config.java.
 */
public class Config {
	public static String TAG_LOG = "MathDoku.Config";

	public enum AppMode {
		DEVELOPMENT, BUG_SENSE, PRODUCTION
	};

	public static final AppMode mAppMode = AppMode.DEVELOPMENT;
}
