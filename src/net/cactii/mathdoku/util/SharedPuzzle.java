package net.cactii.mathdoku.util;

import java.util.ArrayList;
import java.util.List;

import net.cactii.mathdoku.R;
import net.cactii.mathdoku.grid.Grid;
import net.cactii.mathdoku.ui.ArchiveFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.view.View;

public class SharedPuzzle {

	// Context in which the SharedPuzzle is created.
	private final Context mContext;

	// List of uri which have to be enclosed as attachments in the share email.
	private ArrayList<Uri> mUris;

	// Elements of the share url
	private static final String SHARE_URI_SCHEME = "http";
	private static final String SHARE_URI_HOST = "mathdoku.net";
	private static final String SHARE_URI_PUZZLE = "puzzle";
	private static final String SHARE_URI_VERSION = "2";

	/**
	 * Creates new instance of {@see SharedPuzzle}.
	 * 
	 * @param context
	 *            The context in which the SharedPuzzle is created.
	 */
	public SharedPuzzle(Context context) {
		mContext = context;
		mUris = null;
	}

	/**
	 * Start an email client with a prepared email which can be used to share a
	 * game with another user.
	 */
	public void share(int solvingAttemptId) {
		Grid grid = new Grid();
		if (grid.load(solvingAttemptId)) {

			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources()
					.getString(R.string.share_puzzle_subject));

			// Get the share url for this grid.
			String shareURL = getShareUrl(grid.toGridDefinitionString());

			// Get the download url for MathDoku
			// TODO: replace link before release to Google Play
			String downloadUrl = "https://mathdoku.googlecode.com/files/MathDoku"
					+ Util.getPackageVersionNumber() + ".apk";

			// Put share and download url's in the email. Note: the url's are
			// included as HTML-formatted-links and as plain-text-urls as the
			// default android client does not seem to accept HTML-formatted
			// links.
			String mathDokuPuzzle = mContext.getResources().getString(
					R.string.share_puzzle_link_description);
			String bodyText = null;
			if (grid.isActive()) {
				bodyText = mContext.getResources().getString(
						R.string.share_unfinished_puzzle_body,
						toHtmlLink(shareURL, mathDokuPuzzle),
						"<br/>",
						toHtmlLink(downloadUrl,
								"MathDoku Project Download Page"),
						"<br/><br/>", "<br/><br/>", shareURL + "<br/><br/>",
						downloadUrl);
			} else {
				bodyText = mContext.getResources().getString(
						R.string.share_finished_puzzle_body,
						toHtmlLink(shareURL, mathDokuPuzzle),
						Util.durationTimeToString(grid.getElapsedTime()),
						"<br/>",
						toHtmlLink(downloadUrl,
								"MathDoku Project Download Page"),
						"<br/><br/>", "<br/><br/>", shareURL + "<br/><br/>",
						downloadUrl);
			}
			intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(bodyText));

			// Store attachments uris
			if (mUris != null && mUris.size() > 0) {
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, mUris);
			}

			// Start activity choosers
			try {
				mContext.startActivity(Intent.createChooser(
						intent,
						mContext.getResources().getString(
								R.string.feedback_choose_action_title)));
			} catch (android.content.ActivityNotFoundException ex) {
				// No clients installed which can handle this intent.
			}
		}
	}

	/**
	 * Format a link and description to an HTML link.
	 * 
	 * @param link
	 *            The link
	 * @param description
	 *            The description of the link.
	 * @return A string containing the HTML link.
	 */
	private String toHtmlLink(String link, String description) {
		return "<a href =\"" + link + "\">" + description + "</a>";
	}

	/**
	 * Extract certain statistics charts from the given view and add them as
	 * attachments to the share email. This method has to be called before
	 * calling {@see #share(int)}.
	 * 
	 * @param view
	 *            The view from which the statistics charts have to be
	 *            extracted.
	 * @return The reference to the {@see SharedPuzzle} object itself so it can
	 *         be used as a builder.
	 */
	public SharedPuzzle addStatisticsChartsAsAttachments(View view) {
		if (view != null) {
			Screendump screendump = new Screendump(mContext);

			// Check if avoidable moves chart can be found
			if (screendump
					.save(view
							.findViewWithTag(ArchiveFragment.AVOIDABLE_MOVES_CHART_TAG_ID),
							FileProvider.AVOIDABLE_MOVES_CHART_FILE_NAME)) {
				if (mUris == null) {
					mUris = new ArrayList<Uri>();
				}
				mUris.add(FileProvider
						.getUri(FileProvider.AVOIDABLE_MOVES_CHART_FILE_NAME));
			}

			// Check if cheats chart can be found
			if (screendump.save(
					view.findViewWithTag(ArchiveFragment.CHEATS_CHART_TAG_ID),
					FileProvider.CHEATS_CHART_FILE_NAME)) {
				if (mUris == null) {
					mUris = new ArrayList<Uri>();
				}
				mUris.add(FileProvider
						.getUri(FileProvider.CHEATS_CHART_FILE_NAME));
			}
		}

		return this;
	}

	/**
	 * Get the share url for the given grid definition.
	 * 
	 * @param gridDefinition
	 *            The grid definition for which the share url has to be made.
	 * @return The share url for the given grid definition.
	 */
	private String getShareUrl(String gridDefinition) {
		return SHARE_URI_SCHEME + "://" + SHARE_URI_HOST + "/"
				+ SHARE_URI_PUZZLE + "/" + SHARE_URI_VERSION + "/"
				+ gridDefinition + "/" + gridDefinition.hashCode();
	}

	/**
	 * Get the grid definition from the given uri.
	 * 
	 * @param uri
	 *            The uri to be checked.
	 * @return The grid definition as stored in the uri. Null in case the given
	 *         uri is not a valid share url.
	 */
	public static String getGridDefinitionFromUrl(Uri uri) {
		// The data should contain exactly 4 segments
		List<String> pathSegments = uri.getPathSegments();
		if (pathSegments == null || pathSegments.size() != 4) {
			return null;
		}
		if (pathSegments.get(0).equals(SHARE_URI_PUZZLE) == false) {
			return null;
		}
		if (pathSegments.get(1).equals(SHARE_URI_VERSION) == false) {
			return null;
		}
		// Check if grid definition (part 3) matches with the hashcode (part 4).
		// This is a simple measure to check if the uri is complete and not
		// manually changed by an ordinary user. It it still possible to
		// manually manipulate the grid definition and the hashcode but this can
		// do no harm as it is still checked whether a valid grid is specified.
		String gridDefinition = pathSegments.get(2);
		if (gridDefinition.hashCode() != Integer.valueOf(pathSegments.get(3))) {
			return null;
		}

		// The given uri is valid.
		return gridDefinition;
	}
}