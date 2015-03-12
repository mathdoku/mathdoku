package robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.AndroidManifest;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.SdkConfig;
import org.robolectric.annotation.Config;
import org.robolectric.res.FsFile;

import java.io.File;
import java.util.Properties;

public class RobolectricGradleTestRunner extends RobolectricTestRunner {
	private static final String MANIFEST_PATH = "src/main/AndroidManifest.xml";
	private static final String ANDROID_STUDIO_APP_PREFIX = "app/";
	private String appDir;

	public RobolectricGradleTestRunner(Class<?> testClass)
			throws InitializationError {
		super(testClass);

		// Android Studio has a different execution root for tests than pure
		// gradle. In case the manifest can not be found at the default manifest
		// path, we need to prefix the paths with the prefix.
		appDir = new File(MANIFEST_PATH).exists() ? ""
				: ANDROID_STUDIO_APP_PREFIX;
	}

	@Override
	protected AndroidManifest getAppManifest(Config config) {
		config = overwriteConfig(config, "manifest", appDir + MANIFEST_PATH);

		// Override the default assets folders as by default it will use the
		// asset folder relative to the manifest file.
		FsFile manifestFile = getBaseDir().join(config.manifest());
		FsFile baseDir = manifestFile.getParent();
		FsFile resDir = baseDir.join(config.resourceDir());
		FsFile assetsDir = getBaseDir().join(appDir + "src/test/assets");

		return super.createAppManifest(manifestFile, resDir, assetsDir);
	}

	protected Config.Implementation overwriteConfig(Config config, String key,
			String value) {
		Properties properties = new Properties();
		properties.setProperty(key, value);
		return new Config.Implementation(config,
				Config.Implementation.fromProperties(properties));
	}

	@Override
	protected SdkConfig pickSdkVersion(AndroidManifest appManifest,
			Config config) {
		// Currently Robolectric does not supports Android SDK version 19 or
		// higher. So downgrade to simulate the latest supported version.
		config = overwriteConfig(config, "emulateSdk", "18");
		return super.pickSdkVersion(appManifest, config);
	}
}