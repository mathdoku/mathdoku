package net.mathdoku.plus.sharedpuzzle;

import android.app.AlertDialog;
import android.content.Context;
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
import net.mathdoku.plus.util.Screendump;
import net.mathdoku.plus.util.Util;
import net.mathdoku.plus.util.fileprovider.AvoidableMovesChartFileType;
import net.mathdoku.plus.util.fileprovider.CheatsChartFileType;
import net.mathdoku.plus.util.fileprovider.FileProvider;

import java.util.ArrayList;
import java.util.List;

public class SharedPuzzle {
    @SuppressWarnings("unused")
    private static final String TAG = SharedPuzzle.class.getName();
    public static final String HTML_SINGLE_BREAK = "<br/>";
    public static final String HTML_DOUBLE_BREAK = "<br/><br/>";

    private final Context mContext;
    private List<Uri> mUris;
    private MathdokuPlusShareUri mathdokuPlusShareUri;

    /**
     * Creates new instance of {@see SharedPuzzle}.
     *
     * @param context
     *         The context in which the SharedPuzzle is created.
     */
    public SharedPuzzle(Context context) {
        mContext = context;
        mUris = null;

        mathdokuPlusShareUri = new MathdokuPlusShareUri(mContext.getResources());
    }

    /**
     * Start an email client with a prepared email which can be used to share a game with another user.
     */
    public void share(int solvingAttemptId) {
        Grid grid = new GridLoader().load(solvingAttemptId);
        if (grid != null) {
            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_SUBJECT, mContext.getResources()
                    .getString(R.string.share_puzzle_subject));

            // Get the share url for this grid.
            String gridDefinition = grid.getDefinition();
            String shareURL = mathdokuPlusShareUri.getShareUrl(gridDefinition);

            // Get the download url for MathDoku
            String downloadUrl = "https://play.google.com/store/apps/details?id=net.mathdoku.plus";

            // Put share and download url's in the email. Note: the url's are
            // included as HTML-formatted-links and as plain-text-urls as the
            // default android client does not seem to accept HTML-formatted
            // links.
            String mathDokuPuzzle = mContext.getResources()
                    .getString(R.string.share_puzzle_link_description);
            String bodyText;
            if (grid.isActive()) {
                bodyText = mContext.getResources()
                        .getString(R.string.share_unfinished_puzzle_body, toHtmlLink(shareURL, mathDokuPuzzle),
                                   HTML_SINGLE_BREAK,
                                   toHtmlLink(downloadUrl, mContext.getResources()
                                           .getString(R.string.google_play)), HTML_DOUBLE_BREAK, HTML_DOUBLE_BREAK,
                                   shareURL + HTML_DOUBLE_BREAK, downloadUrl);
            } else {
                bodyText = mContext.getResources()
                        .getString(R.string.share_finished_puzzle_body, toHtmlLink(shareURL, mathDokuPuzzle),
                                   Util.durationTimeToString(grid.getElapsedTime()), HTML_SINGLE_BREAK,
                                   toHtmlLink(downloadUrl, mContext.getResources()
                                           .getString(R.string.google_play)), HTML_DOUBLE_BREAK, HTML_DOUBLE_BREAK,
                                   shareURL + HTML_DOUBLE_BREAK, downloadUrl);
            }
            intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(bodyText));

            // Store attachments uris
            if (Util.isListNotNullOrEmpty(mUris)) {
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, (ArrayList<? extends Parcelable>) mUris);
            }

            // Start activity choosers
            try {
                mContext.startActivity(Intent.createChooser(intent, mContext.getResources()
                        .getString(R.string.feedback_choose_action_title)));

            } catch (android.content.ActivityNotFoundException ex) {
                Log.d(TAG, "No email app installed", ex);
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(R.string.dialog_no_email_client_found_title)
                        .setMessage(R.string.dialog_sharing_puzzles_is_not_possible_body)
                        .setNeutralButton(R.string.dialog_general_button_close, null)
                        .create()
                        .show();
            }
        }
    }

    /**
     * Format a link and description to an HTML link.
     *
     * @param link
     *         The link
     * @param description
     *         The description of the link.
     * @return A string containing the HTML link.
     */
    private String toHtmlLink(String link, String description) {
        return "<a href =\"" + link + "\">" + description + "</a>";
    }

    /**
     * Extract certain statistics charts from the given view and add them as attachments to the share email. This method
     * has to be called before calling {@see #share(int)}.
     *
     * @param view
     *         The view from which the statistics charts have to be extracted.
     * @return The reference to the {@see SharedPuzzle} object itself so it can be used as a builder.
     */
    public SharedPuzzle addStatisticsChartsAsAttachments(View view) {
        if (view != null) {
            Screendump screendump = new Screendump(mContext);

            // Check if avoidable moves chart can be found
            if (screendump.save(view.findViewWithTag(ArchiveFragment.AVOIDABLE_MOVES_CHART_TAG_ID),
                                AvoidableMovesChartFileType.NAME)) {
                if (mUris == null) {
                    mUris = new ArrayList<Uri>();
                }
                mUris.add(FileProvider.getUri(AvoidableMovesChartFileType.NAME));
            }

            // Check if cheats chart can be found
            if (screendump.save(view.findViewWithTag(ArchiveFragment.CHEATS_CHART_TAG_ID),
                                CheatsChartFileType.NAME)) {
                if (mUris == null) {
                    mUris = new ArrayList<Uri>();
                }
                mUris.add(FileProvider.getUri(CheatsChartFileType.NAME));
            }
        }

        return this;
    }

    /**
     * Get the grid definition from the given uri.
     *
     * @param uri
     *         The uri to be checked.
     * @return The grid definition as stored in the uri. Null in case the given uri is not a valid share url.
     */
    public String getGridDefinitionFromUrl(Uri uri) {
        if (mathdokuPlusShareUri.matches(uri)) {
            String gridDefinition = mathdokuPlusShareUri.getGridDefinitionFromUri(uri);
            if (gridDefinition != null) {
                return gridDefinition;
            }
        }

        MathdokuOriginalShareUri mathdokuOriginalShareUri = new MathdokuOriginalShareUri(mContext.getResources());
        if (mathdokuOriginalShareUri.matches(uri)) {
            return mathdokuOriginalShareUri.getGridDefinitionFromUri(uri);
        }

        // Unknown scheme or host or something else went wrong.
        return null;
    }
}