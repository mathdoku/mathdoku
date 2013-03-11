package net.cactii.mathdoku;

/**
 * The Development Helper class is intended to support Development and Unit
 * Testing of this application. Variables and methods should not be used in
 * production code with exception of static variable {@link #mode}.
 * 
 * Checks on variable {@link #mode} should always be made in such a way that the
 * result can be determined at compile time. In this way the enclosed block will
 * not be included in the compiled case when the condition for executing the
 * block evaluates to false. Example of intended usage:
 * 
 * <pre class="prettyprint">
 * if (DevelopmentHelper.mode = Mode.UNIT_TESTING) {
 * 	// code which should only be included in case the app is used for unit
 * 	// testing
 * }
 * </pre>
 * 
 * @author Paul Dingemans
 * 
 */
public class DevelopmentHelper {
	public static String TAG_LOG = "MathDoku.DevelopmentHelper";

	public enum Mode {
		DEVELOPMENT, UNIT_TESTING, PRODUCTION
	};

	public static final Mode mode = Mode.DEVELOPMENT;
}
