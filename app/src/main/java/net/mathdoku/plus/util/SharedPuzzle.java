package net.mathdoku.plus.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.View;

import net.mathdoku.plus.R;
import net.mathdoku.plus.archive.ui.ArchiveFragment;
import net.mathdoku.plus.puzzle.grid.Grid;
import net.mathdoku.plus.puzzle.grid.GridLoader;

import java.util.ArrayList;
import java.util.List;

public class SharedPuzzle {
	@SuppressWarnings("unused")
	private static final String TAG = SharedPuzzle.class.getName();

	// Context in which the SharedPuzzle is created.
	private final Context mContext;

	// List of uri which have to be enclosed as attachments in the share email.
	private List<Uri> mUris;

	// Elements of the share url for Mathdoku Plus
	private final String mSharedPuzzleSchemeMathdokuPlus;
	private final String mSharedPuzzleHostMathdokuPlus;
	private String mSharedPuzzlePathPrefixMathdokuPlus;
	private final String SHARE_URI_VERSION_MATHDOKU_PLUS = "2";

	// Elements of the share url for Mathdoku Original
	private final String mSharedPuzzleSchemeMathdokuOriginal;
	private final String mSharedPuzzleHostMathdokuOriginal;
	private String mSharedPuzzlePathPrefixMathdokuOriginal;
	private final String SHARE_URI_VERSION_MATHDOKU_ORIGINAL = "2";

	/**
	 * Creates new instance of {@see SharedPuzzle}.
	 * 
	 * @param context
	 *            The context in which the SharedPuzzle is created.
	 */
	public SharedPuzzle(Context context) {
		mContext = context;
		mUris = null;

		// Mathdoku Plus elements of share url
		mSharedPuzzleSchemeMathdokuPlus = mContext.getResources().getString(
				R.string.shared_puzzle_scheme_mathdoku_plus);
		mSharedPuzzleHostMathdokuPlus = mContext.getResources().getString(
				R.string.shared_puzzle_host_mathdoku_plus);
		mSharedPuzzlePathPrefixMathdokuPlus = mContext
				.getResources()
				.getString(R.string.shared_puzzle_path_prefix_mathdoku_plus);

		// Strip slash of start of prefix
		if (mSharedPuzzlePathPrefixMathdokuPlus.charAt(0) == '/') {
			mSharedPuzzlePathPrefixMathdokuPlus = mSharedPuzzlePathPrefixMathdokuPlus
					.substring(1);
		}

		// Mathdoku Original elements of share url
		mSharedPuzzleSchemeMathdokuOriginal = mContext
				.getResources()
				.getString(R.string.shared_puzzle_scheme_mathdoku_original);
		mSharedPuzzleHostMathdokuOriginal = mContext.getResources().getString(
				R.string.shared_puzzle_host_mathdoku_original);
		mSharedPuzzlePathPrefixMathdokuOriginal = mContext
				.getResources()
				.getString(R.string.shared_puzzle_path_prefix_mathdoku_original);

		// Strip slash of start of prefix
		if (mSharedPuzzlePathPrefixMathdokuOriginal.charAt(0) == '/') {
			mSharedPuzzlePathPrefixMathdokuOriginal = mSharedPuzzlePathPrefixMathdokuOriginal
					.substring(1);
		}
	}

	/**
	 * Start an email client with a prepared email which can be used to share a
	 * game with another user.
	 */
	public void share(int solvingAttemptId) {
		Grid grid = new GridLoader().load(solvingAttemptId);
		if (grid != null) {
			Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
			intent.setType("message/rfc822");
			intent.putExtra(Intent.EXTRA_SUBJECT, mContext
					.getResources()
					.getString(R.string.share_puzzle_subject));

			// Get the share url for this grid.
			String gridDefinition = grid.getDefinition();
			String shareURL = getShareUrl(gridDefinition);

			// Get the download url for MathDoku
			String downloadUrl = "https://play.google.com/store/apps/details?id=net.mathdoku.plus";

			// Put share and download url's in the email. Note: the url's are
			// included as HTML-formatted-links and as plain-text-urls as the
			// default android client does not seem to accept HTML-formatted
			// links.
			String mathDokuPuzzle = mContext.getResources().getString(
					R.string.share_puzzle_link_description);
			String bodyText;
			if (grid.isActive()) {
				bodyText = mContext.getResources().getString(
						R.string.share_unfinished_puzzle_body,
						toHtmlLink(shareURL, mathDokuPuzzle), "<br/>",
						toHtmlLink(downloadUrl, "Google Play"), "<br/><br/>",
						"<br/><br/>", shareURL + "<br/><br/>", downloadUrl);
			} else {
				bodyText = mContext.getResources().getString(
						R.string.share_finished_puzzle_body,
						toHtmlLink(shareURL, mathDokuPuzzle),
						Util.durationTimeToString(grid.getElapsedTime()),
						"<br/>", toHtmlLink(downloadUrl, "Google Play"),
						"<br/><br/>", "<br/><br/>", shareURL + "<br/><br/>",
						downloadUrl);
			}
			intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(bodyText));

			// Store attachments uris
			if (mUris != null && mUris.size() > 0) {
				intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,
						(ArrayList<? extends Parcelable>) mUris);
			}

			// Start activity choosers
			try {
				mContext.startActivity(Intent.createChooser(intent, mContext
						.getResources()
						.getString(R.string.feedback_choose_action_title)));

			} catch (android.content.ActivityNotFoundException ex) {
				Log.d(TAG, "No email app installed", ex);
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.dialog_no_email_client_found_title)
						.setMessage(
								R.string.dialog_sharing_puzzles_is_not_possible_body)
						.setNeutralButton(R.string.dialog_general_button_close,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										// Do nothing
									}
								})
						.create()
						.show();
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
		return mSharedPuzzleSchemeMathdokuPlus + "://"
				+ mSharedPuzzleHostMathdokuPlus + "/"
				+ mSharedPuzzlePathPrefixMathdokuPlus + "/"
				+ SHARE_URI_VERSION_MATHDOKU_PLUS + "/" + gridDefinition + "/"
				+ gridDefinition.hashCode();
	}

	/**
	 * Get the grid definition from the given uri.
	 * 
	 * @param uri
	 *            The uri to be checked.
	 * @return The grid definition as stored in the uri. Null in case the given
	 *         uri is not a valid share url.
	 */
	public String getGridDefinitionFromUrl(Uri uri) {
		if (uri == null) {
			return null;
		}

		// Get scheme and host used in uri
		String scheme = uri.getScheme();
		if (scheme == null) {
			return null;
		}
		String host = uri.getHost();
		if (host == null) {
			return null;
		}

		// Check if a Mathdoku Plus uri was specified.
		if (scheme.equals(mSharedPuzzleSchemeMathdokuPlus)
				&& host.equals(mSharedPuzzleHostMathdokuPlus)) {
			String gridDefinition = getGridDefinitionFromMathdokuPlusUrl(uri);
			if (gridDefinition != null) {
				return gridDefinition;
			}
		}

		// Check if a Mathdoku Original uri was specified.
		if (scheme.equals(mSharedPuzzleSchemeMathdokuOriginal)
				&& host.equals(mSharedPuzzleHostMathdokuOriginal)) {
			return getGridDefinitionFromMathdokuOriginalUrl(uri);
		}

		// Unknown scheme or host or something else went wrong.
		return null;
	}

	/**
	 * Get the grid definition from the given uri.
	 * 
	 * @param uri
	 *            The uri to be checked.
	 * @return The grid definition as stored in the uri. Null in case the given
	 *         uri is not a valid share url.
	 */
	private String getGridDefinitionFromMathdokuPlusUrl(Uri uri) {
		if (uri == null) {
			return null;
		}

		// Get scheme and host used in uri
		String scheme = uri.getScheme();
		if (scheme == null) {
			return null;
		}
		String host = uri.getHost();
		if (host == null) {
			return null;
		}

		// Only process when valid scheme and host are specified.
		if (!scheme.equals(mSharedPuzzleSchemeMathdokuPlus)
				|| !host.equals(mSharedPuzzleHostMathdokuPlus)) {
			return null;
		}

		// The data should contain exactly 4 segments
		List<String> pathSegments = uri.getPathSegments();
		if (pathSegments == null || pathSegments.size() != 4) {
			return null;
		}
		if (!pathSegments.get(0).equals(mSharedPuzzlePathPrefixMathdokuPlus)) {
			return null;
		}
		if (!pathSegments.get(1).equals(SHARE_URI_VERSION_MATHDOKU_PLUS)) {
			return null;
		}
		// Check if grid definition (part 3) matches with the hash code (part
		// 4).
		// This is a simple measure to check if the uri is complete and not
		// manually changed by an ordinary user. It it still possible to
		// manually manipulate the grid definition and the hash code but this
		// can
		// do no harm as it is still checked whether a valid grid is specified.
		String gridDefinition = pathSegments.get(2);
		if (gridDefinition.hashCode() != Integer.valueOf(pathSegments.get(3))) {
			return null;
		}

		// The given uri is valid.
		return gridDefinition;
	}

	/**
	 * Get the grid definition from the given uri.
	 * 
	 * @param uri
	 *            The uri to be checked.
	 * @return The grid definition as stored in the uri. Null in case the given
	 *         uri is not a valid share url.
	 */
	private String getGridDefinitionFromMathdokuOriginalUrl(Uri uri) {
		if (uri == null) {
			return null;
		}

		// Get scheme and host used in uri
		String scheme = uri.getScheme();
		if (scheme == null) {
			return null;
		}
		String host = uri.getHost();
		if (host == null) {
			return null;
		}

		// Only process when valid scheme and host are specified.
		if (!scheme.equals(mSharedPuzzleSchemeMathdokuOriginal)
				|| !host.equals(mSharedPuzzleHostMathdokuOriginal)) {
			return null;
		}

		// The data should contain exactly 4 segments
		List<String> pathSegments = uri.getPathSegments();
		if (pathSegments == null || pathSegments.size() != 4) {
			return null;
		}
		if (!pathSegments
				.get(0)
				.equals(mSharedPuzzlePathPrefixMathdokuOriginal)) {
			return null;
		}
		if (!pathSegments.get(1).equals(SHARE_URI_VERSION_MATHDOKU_ORIGINAL)) {
			return null;
		}
		// Check if grid definition (part 3) matches with the hash code (part
		// 4).
		// This is a simple measure to check if the uri is complete and not
		// manually changed by an ordinary user. It it still possible to
		// manually manipulate the grid definition and the hash code but this
		// can
		// do no harm as it is still checked whether a valid grid is specified.
		String gridDefinition = pathSegments.get(2);
		if (gridDefinition.hashCode() != Integer.valueOf(pathSegments.get(3))) {
			return null;
		}

		// The given uri is valid.
		return gridDefinition;
	}
}