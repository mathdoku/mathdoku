/*
 * Copyright (C) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mathdoku.plus.ui.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.multiplayer.Invitation;

import net.mathdoku.plus.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("WeakerAccess")
public class GameHelper implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    public static final String FAILED = "(failed)";

    /**
     * Listener for sign-in success or failure events.
     */
    public interface GameHelperListener {
        /**
         * Called when sign-in fails. As a result, a "Sign-In" button can be shown to the user; when that button is
         * clicked, call
         * <p/>
         * {@link GameHelper#beginUserInitiatedSignIn}. Note that not all calls to this method mean an error; it may be
         * a result of the fact that automatic sign-in could not proceed because user interaction was required (consent
         * dialogs). So implementations of this method should NOT display an error message unless a call to
         *
         * @link{GamesHelper# hasSignInError} indicates that an error indeed occurred.
         */
        void onSignInFailed();

        /**
         * Called when the auto sign in has completed.
         */
        void onAutoSignInSucceeded();

        /**
         * Called when the use initiated sign in has completed.
         */
        void onUserInitiatedSignInSucceeded();
    }

    // States we can be in
    @SuppressWarnings("WeakerAccess")
    public static final int STATE_UNCONFIGURED = 0;
    @SuppressWarnings("WeakerAccess")
    public static final int STATE_DISCONNECTED = 1;
    @SuppressWarnings("WeakerAccess")
    public static final int STATE_CONNECTING = 2;
    @SuppressWarnings("WeakerAccess")
    public static final int STATE_CONNECTED = 3;

    // State names (for debug logging, etc)
    @SuppressWarnings("WeakerAccess")
    public static final String[] STATE_NAMES = {"UNCONFIGURED", "DISCONNECTED", "CONNECTING", "CONNECTED"};

    // State we are in right now
    private int mState = STATE_UNCONFIGURED;

    // Are we expecting the result of a resolution flow?
    private boolean mExpectingResolution = false;

    /**
     * The Activity we are bound to. We need to keep a reference to the Activity because some games methods require an
     * Activity (a Context won't do). We are careful not to leak these references: we release them on onStop().
     */
    private Activity mActivity = null;

    // OAuth scopes required for the clients. Initialized in setup().
    private String[] mScopes;

    // Request code we use when invoking other Activities to complete the
    // sign-in flow.
    static final int RC_RESOLVE = 9001;

    // Request code when invoking Activities whose result we don't care about.
    static final int RC_UNUSED = 9002;

    // Client objects we manage. If a given client is not enabled, it is null.
    private GamesClient mGamesClient = null;

    // What clients we manage (OR-able values, can be combined as flags)
    @SuppressWarnings("WeakerAccess")
    public static final int CLIENT_NONE = 0x00;
    public static final int CLIENT_GAMES = 0x01;

    // What clients were requested? (bit flags)
    private int mRequestedClients = CLIENT_NONE;

    // What clients are currently connected? (bit flags)
    private int mConnectedClients = CLIENT_NONE;

    // What client are we currently connecting?
    private int mClientCurrentlyConnecting = CLIENT_NONE;

    // Whether to automatically try to sign in on onStart().
    private boolean mAutoSignIn = true;

    /*
     * Whether user has specifically requested that the sign-in process begin.
     * If mUserInitiatedSignIn is false, we're in the automatic sign-in attempt
     * that we try once the Activity is started -- if true, then the user has
     * already clicked a "Sign-In" button or something similar
     */
    private boolean mUserInitiatedSignIn = false;

    // The connection result we got from our last attempt to sign-in.
    private ConnectionResult mConnectionResult = null;

    // The error that happened during sign-in.
    private SignInFailureReason mSignInFailureReason = null;

    // Print debug logs?
    private boolean mDebugLog = false;
    private String mDebugTag = "GameHelper";

    /*
     * If we got an invitation id when we connected to the games client, it's
     * here. Otherwise, it's null.
     */
    private String mInvitationId;

    // Listener
    private GameHelperListener mListener = null;

    /**
     * Construct a GameHelper object, initially tied to the given Activity. After constructing this object, call
     *
     * @link{setup} from the onCreate() method of your Activity.
     */
    public GameHelper(Activity activity) {
        mActivity = activity;
    }

    private static final int TYPE_DEVELOPER_ERROR = 1001;
    private static final int TYPE_GAMEHELPER_BUG = 1002;

    boolean checkState(int type, String operation, String warning, int... expectedStates) {
        for (int expectedState : expectedStates) {
            if (mState == expectedState) {
                return true;
            }
        }
        StringBuilder sb = new StringBuilder();
        if (type == TYPE_DEVELOPER_ERROR) {
            sb.append("GameHelper: you attempted an operation at an invalid. ");
        } else {
            sb.append("GameHelper: bug detected. Please report it at our bug tracker ");
            sb.append("https://github.com/playgameservices/android-samples/issues. ");
            sb.append("Please include the last couple hundred lines of logcat output ");
            sb.append("and describe the operation that caused this. ");
        }
        sb.append("Explanation: ")
                .append(warning);
        sb.append("Operation: ")
                .append(operation)
                .append(". ");
        sb.append("State: ")
                .append(STATE_NAMES[mState])
                .append(". ");
        if (expectedStates.length == 1) {
            sb.append("Expected state: ")
                    .append(STATE_NAMES[expectedStates[0]])
                    .append(".");
        } else {
            sb.append("Expected states:");
            for (int expectedState : expectedStates) {
                sb.append(" ")
                        .append(STATE_NAMES[expectedState]);
            }
            sb.append(".");
        }

        logWarn(sb.toString());
        return false;
    }

    void assertConfigured(String operation) {
        if (mState == STATE_UNCONFIGURED) {
            String error = "GameHelper error: Operation attempted without setup: " + operation +
                    ". The setup() method must be called before attempting any other operation.";
            logError(error);
            throw new IllegalStateException(error);
        }
    }

    /**
     * Same as calling @link{setup(GameHelperListener, int)} requesting only the CLIENT_GAMES client.
     */
    public void setup(GameHelperListener listener) {
        if (mState != STATE_UNCONFIGURED) {
            String error = "GameHelper: you called GameHelper.setup() twice. You can only call " + "it once.";
            logError(error);
            throw new IllegalStateException(error);
        }
        mListener = listener;
        mRequestedClients = CLIENT_GAMES;

        debugLog("setup: creating GamesClient");
        mGamesClient = new GamesClient.Builder(getContext(), this, this).setGravityForPopups(
                Gravity.TOP | Gravity.CENTER_HORIZONTAL)
                .setScopes(new String[] {Scopes.GAMES})
                .create();

        setState(STATE_DISCONNECTED);
    }

    void setState(int newState) {
        String oldStateName = STATE_NAMES[mState];
        String newStateName = STATE_NAMES[newState];
        mState = newState;
        debugLog("State change " + oldStateName + " -> " + newStateName);
    }

    /**
     * Returns the GamesClient object. In order to call this method, you must have called
     *
     * @link{setup} with a set of clients that includes CLIENT_GAMES.
     */
    public GamesClient getGamesClient() {
        if (mGamesClient == null) {
            throw new IllegalStateException("No GamesClient. Did you request it at setup?");
        }
        return mGamesClient;
    }

    /**
     * Call this method from your Activity's onStart().
     */
    public void onStart(Activity act) {
        mActivity = act;

        debugLog("onStart, state = " + STATE_NAMES[mState]);
        assertConfigured("onStart");

        switch (mState) {
            case STATE_DISCONNECTED:
                attemptToConnect();
                break;
            case STATE_CONNECTING:
                // connection process is in progress; no action required
                debugLog("onStart: connection process in progress, no action taken.");
                break;
            case STATE_CONNECTED:
                // already connected (for some strange reason). No complaints :-)
                debugLog("onStart: already connected (unusual, but ok).");
                break;
            default:
                throw new IllegalStateException("onStart: BUG: unexpected state " + STATE_NAMES[mState]);
        }
    }

    private void attemptToConnect() {
        if (mAutoSignIn) {
            debugLog("onStart: Now connecting clients.");
            startConnections();
        } else {
            debugLog("onStart: Not connecting (user specifically signed out).");
        }
    }

    /**
     * Call this method from your Activity's onStop().
     */
    public void onStop() {
        debugLog("onStop, state = " + STATE_NAMES[mState]);
        assertConfigured("onStop");
        switch (mState) {
            case STATE_CONNECTED:
            case STATE_CONNECTING:
                // kill connections
                debugLog("onStop: Killing connections");
                killConnections();
                break;
            case STATE_DISCONNECTED:
                debugLog("onStop: not connected, so no action taken.");
                break;
            default:
                String msg = "onStop: BUG: unexpected state " + STATE_NAMES[mState];
                logError(msg);
                throw new IllegalStateException(msg);
        }

        // let go of the Activity reference
        mActivity = null;
    }

    /**
     * Enables debug logging
     */
    public void enableDebugLog(String tag) {
        mDebugLog = true;
        mDebugTag = tag;
        debugLog("Debug log enabled, tag: " + tag);
    }

    /**
     * Sign out and disconnect from the APIs.
     */
    public void signOut() {
        if (mState == STATE_DISCONNECTED) {
            // nothing to do
            debugLog("signOut: state was already DISCONNECTED, ignoring.");
            return;
        }

        // For the games client, signing out means calling signOut and
        // disconnecting
        if (mGamesClient != null && mGamesClient.isConnected()) {
            debugLog("Signing out from GamesClient.");
            mGamesClient.signOut();
        }

        // Ready to disconnect
        debugLog("Proceeding with disconnection.");
        killConnections();
    }

    void killConnections() {
        if (!checkState(TYPE_GAMEHELPER_BUG, "killConnections",
                        "killConnections() should only " + "get called while connected or " +
                                "connecting.", STATE_CONNECTED, STATE_CONNECTING)) {
            return;
        }
        debugLog("killConnections: killing connections.");

        mConnectionResult = null;
        mSignInFailureReason = null;

        if (mGamesClient != null && mGamesClient.isConnected()) {
            debugLog("Disconnecting GamesClient.");
            mGamesClient.disconnect();
        }
        mConnectedClients = CLIENT_NONE;
        debugLog("killConnections: all clients disconnected.");
        setState(STATE_DISCONNECTED);
    }

    private static String activityResponseCodeToString(int respCode) {
        switch (respCode) {
            case Activity.RESULT_OK:
                return "RESULT_OK";
            case Activity.RESULT_CANCELED:
                return "RESULT_CANCELED";
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                return "RESULT_APP_MISCONFIGURED";
            case GamesActivityResultCodes.RESULT_LEFT_ROOM:
                return "RESULT_LEFT_ROOM";
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                return "RESULT_LICENSE_FAILED";
            case GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED:
                return "RESULT_RECONNECT_REQUIRED";
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                return "SIGN_IN_FAILED";
            default:
                return String.valueOf(respCode);
        }
    }

    /**
     * Handle activity result. Call this method from your Activity's onActivityResult callback. If the activity result
     * pertains to the sign-in process, processes it appropriately.
     */
    public void onActivityResult(int requestCode, int responseCode) {
        debugLog("onActivityResult: req=" + (requestCode == RC_RESOLVE ? "RC_RESOLVE" : String.valueOf(
                requestCode)) + ", resp=" + activityResponseCodeToString(responseCode));
        if (requestCode != RC_RESOLVE) {
            debugLog("onActivityResult: request code not meant for us. Ignoring.");
            return;
        }

        // no longer expecting a resolution
        mExpectingResolution = false;

        if (mState != STATE_CONNECTING) {
            debugLog("onActivityResult: ignoring because state isn't STATE_CONNECTING (" + "it's " +
                             STATE_NAMES[mState] + ")");
            return;
        }

        // We're coming back from an activity that was launched to resolve a
        // connection problem. For example, the sign-in UI.
        if (responseCode == Activity.RESULT_OK) {
            // Ready to try to connect again.
            debugLog("onAR: Resolution was RESULT_OK, so connecting current client again.");
            connectCurrentClient();
        } else if (responseCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            debugLog("onAR: Resolution was RECONNECT_REQUIRED, so reconnecting.");
            connectCurrentClient();
        } else if (responseCode == Activity.RESULT_CANCELED) {
            // User cancelled.
            debugLog("onAR: Got a cancellation result, so disconnecting.");
            mAutoSignIn = false;
            mUserInitiatedSignIn = false;
            // cancelling is not a failure!
            mSignInFailureReason = null;
            killConnections();
            notifyListener(false);
        } else {
            // Whatever the problem we were trying to solve, it was not
            // solved. So give up and show an error message.
            debugLog("onAR: responseCode=" + activityResponseCodeToString(responseCode) + ", so giving up.");
            giveUp(new SignInFailureReason(mConnectionResult.getErrorCode(), responseCode));
        }
    }

    void notifyListener(boolean success) {
        debugLog(
                "Notifying LISTENER of sign-in " + (success ? "SUCCESS" : mSignInFailureReason != null ? "FAILURE " +
                        "(error)" : "FAILURE (no error)"));
        if (mListener != null) {
            if (success) {
                if (mUserInitiatedSignIn) {
                    mListener.onUserInitiatedSignInSucceeded();
                } else {
                    mListener.onAutoSignInSucceeded();
                }
            } else {
                mListener.onSignInFailed();
            }
        }
    }

    /**
     * Starts a user-initiated sign-in flow. This should be called when the user clicks on a "Sign In" button. As a
     * result, authentication/consent dialogs may show up. At the end of the process, the GameHelperListener's
     * onSignInSucceeded() or onSignInFailed() methods will be called.
     */

    public void beginUserInitiatedSignIn() {
        if (mState == STATE_CONNECTED) {
            // nothing to do
            logWarn("beginUserInitiatedSignIn() called when already connected. " + "Calling " +
                            "listener directly to notify of success.");
            notifyListener(true);
            return;
        } else if (mState == STATE_CONNECTING) {
            logWarn("beginUserInitiatedSignIn() called when already connecting. " + "Be patient! " +
                            "You can only call this method after you get an " +
                            "onSignInSucceeded() or onSignInFailed() callback. Suggestion: " +
                            "disable " + "the sign-in button on startup and also when it's " +
                            "clicked, and re-enable " + "when you get the callback.");
            // ignore call (listener will get a callback when the connection
            // process finishes)
            return;
        }

        debugLog("Starting USER-INITIATED sign-in flow.");

        // sign in automatically on onStart()
        mAutoSignIn = true;

        // Is Google Play services available?
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        debugLog("isGooglePlayServicesAvailable returned " + result);
        if (result != ConnectionResult.SUCCESS) {
            // Google Play services is not available.
            debugLog("Google Play services not available. Show error dialog.");
            mSignInFailureReason = new SignInFailureReason(result, 0);
            showFailureDialog();
            notifyListener(false);
            return;
        }

        // indicate that user is actively trying to sign in (so we know to
        // resolve
        // connection problems by showing dialogs)
        mUserInitiatedSignIn = true;

        if (mConnectionResult != null) {
            // We have a pending connection result from a previous failure, so
            // start with that.
            debugLog("beginUserInitiatedSignIn: continuing pending sign-in flow.");
            setState(STATE_CONNECTING);
            resolveConnectionResult();
        } else {
            // We don't have a pending connection result, so start anew.
            debugLog("beginUserInitiatedSignIn: starting new sign-in flow.");
            startConnections();
        }
    }

    Context getContext() {
        return mActivity;
    }

    void startConnections() {
        if (!checkState(TYPE_GAMEHELPER_BUG, "startConnections",
                        "startConnections should " + "only get called when disconnected.", STATE_DISCONNECTED)) {
            return;
        }
        debugLog("Starting connections.");
        setState(STATE_CONNECTING);
        mInvitationId = null;
        connectNextClient();
    }

    void connectNextClient() {
        // do we already have all the clients we need?
        debugLog("connectNextClient: requested clients: " + mRequestedClients + ", " +
                         "connected clients: " + mConnectedClients);

        // failsafe, in case we somehow lost track of what clients are connected
        // or not.
        if (mGamesClient != null && mGamesClient.isConnected() && 0 == (mConnectedClients & CLIENT_GAMES)) {
            logWarn("GamesClient was already connected. Fixing.");
            mConnectedClients |= CLIENT_GAMES;
        }

        int pendingClients = mRequestedClients & ~mConnectedClients;
        debugLog("Pending clients: " + pendingClients);

        if (pendingClients == 0) {
            debugLog("All clients now connected. Sign-in successful!");
            succeedSignIn();
            return;
        }

        // which client should be the next one to connect?
        if (mGamesClient != null && 0 != (pendingClients & CLIENT_GAMES)) {
            debugLog("Connecting GamesClient.");
            mClientCurrentlyConnecting = CLIENT_GAMES;
        } else {
            // hmmm, getting here would be a bug.
            throw new AssertionError("Not all clients connected, yet no one is next. R=" + mRequestedClients + ", " +
                                             "C=" + mConnectedClients);
        }

        connectCurrentClient();
    }

    void connectCurrentClient() {
        if (mState == STATE_DISCONNECTED) {
            // we got disconnected during the connection process, so abort
            logWarn("GameHelper got disconnected during connection process. Aborting.");
            return;
        }
        if (!checkState(TYPE_GAMEHELPER_BUG, "connectCurrentClient",
                        "connectCurrentClient " + "should only get called when connecting.", STATE_CONNECTING)) {
            return;
        }

        if (mClientCurrentlyConnecting == CLIENT_GAMES) {
            mGamesClient.connect();
        }
    }

    /**
     * Called when we successfully obtain a connection to a client.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        debugLog("onConnected: connected! client=" + mClientCurrentlyConnecting);

        // Mark the current client as connected
        mConnectedClients |= mClientCurrentlyConnecting;
        debugLog("Connected clients updated to: " + mConnectedClients);

        // If this was the games client and it came with an invite, store it for
        // later retrieval.
        if (mClientCurrentlyConnecting == CLIENT_GAMES && connectionHint != null) {
            debugLog("onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint.getParcelable(GamesClient.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // accept invitation
                debugLog("onConnected: connection hint has a room invite!");
                mInvitationId = inv.getInvitationId();
                debugLog("Invitation ID: " + mInvitationId);
            }
        }

        // connect the next client in line, if any.
        connectNextClient();
    }

    void succeedSignIn() {
        checkState(TYPE_GAMEHELPER_BUG, "succeedSignIn",
                   "succeedSignIn should only " + "get called in the connecting or connected " +
                           "state. Proceeding anyway.", STATE_CONNECTING, STATE_CONNECTED);
        debugLog("All requested clients connected. Sign-in succeeded!");
        setState(STATE_CONNECTED);
        mSignInFailureReason = null;
        mAutoSignIn = true;
        notifyListener(true);
        mUserInitiatedSignIn = false;
    }

    /**
     * Handles a connection failure reported by a client.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // save connection result for later reference
        debugLog("onConnectionFailed");

        mConnectionResult = result;
        debugLog("Connection failure:");
        debugLog("   - code: " + errorCodeToString(mConnectionResult.getErrorCode()));
        debugLog("   - resolvable: " + mConnectionResult.hasResolution());
        debugLog("   - details: " + mConnectionResult.toString());

        if (!mUserInitiatedSignIn) {
            // If the user didn't initiate the sign-in, we don't try to resolve
            // the connection problem automatically -- instead, we fail and wait
            // for the user to want to sign in. That way, they won't get an
            // authentication (or other) popup unless they are actively trying
            // to
            // sign in.
            debugLog("onConnectionFailed: since user didn't initiate sign-in, failing now.");
            mConnectionResult = result;
            setState(STATE_DISCONNECTED);
            notifyListener(false);
            return;
        }

        debugLog("onConnectionFailed: since user initiated sign-in, resolving problem.");

        // Resolve the connection result. This usually means showing a dialog or
        // starting an Activity that will allow the user to give the appropriate
        // consents so that sign-in can be successful.
        resolveConnectionResult();
    }

    /**
     * Attempts to resolve a connection failure. This will usually involve starting a UI flow that lets the user give
     * the appropriate consents necessary for sign-in to work.
     */
    void resolveConnectionResult() {
        // Try to resolve the problem
        checkState(TYPE_GAMEHELPER_BUG, "resolveConnectionResult",
                   "resolveConnectionResult should only be called when connecting. Proceeding " + "anyway.",
                   STATE_CONNECTING);

        if (mExpectingResolution) {
            debugLog("We're already expecting the result of a previous resolution.");
            return;
        }

        debugLog("resolveConnectionResult: trying to resolve result: " + mConnectionResult);
        if (mConnectionResult.hasResolution()) {
            // This problem can be fixed. So let's try to fix it.
            debugLog("Result has resolution. Starting it.");
            try {
                // launch appropriate UI flow (which might, for example, be the
                // sign-in flow)
                mExpectingResolution = true;
                mConnectionResult.startResolutionForResult(mActivity, RC_RESOLVE);
            } catch (SendIntentException e) {
                // Try connecting again
                debugLog("SendIntentException, so connecting again.", e);
                connectCurrentClient();
            }
        } else {
            // It's not a problem what we can solve, so give up and show an
            // error.
            debugLog("resolveConnectionResult: result has no resolution. Giving up.");
            giveUp(new SignInFailureReason(mConnectionResult.getErrorCode()));
        }
    }

    /**
     * Give up on signing in due to an error. Shows the appropriate error message to the user, using a standard error
     * dialog as appropriate to the cause of the error. That dialog will indicate to the user how the problem can be
     * solved (for example, re-enable Google Play Services, upgrade to a new version, etc).
     */
    void giveUp(SignInFailureReason reason) {
        checkState(TYPE_GAMEHELPER_BUG, "giveUp",
                   "giveUp should only be called when " + "connecting. Proceeding anyway.", STATE_CONNECTING);
        mAutoSignIn = false;
        killConnections();
        mSignInFailureReason = reason;
        showFailureDialog();
        notifyListener(false);
    }

    /**
     * Called when we are disconnected from a client.
     */
    @Override
    public void onDisconnected() {
        debugLog("onDisconnected.");
        if (mState == STATE_DISCONNECTED) {
            // This is expected.
            debugLog("onDisconnected is expected, so no action taken.");
            return;
        }

        // Unexpected disconnect (rare!)
        logWarn("Unexpectedly disconnected. Severing remaining connections.");

        // kill the other connections too, and revert to DISCONNECTED state.
        killConnections();
        mSignInFailureReason = null;

        // call the sign in failure callback
        debugLog("Making extraordinary call to onSignInFailed callback");
        notifyListener(false);
    }

    /**
     * Shows an error dialog that's appropriate for the failure reason.
     */
    void showFailureDialog() {
        Context ctx = getContext();
        if (ctx == null) {
            debugLog("*** No context. Can't show failure dialog.");
            return;
        }
        debugLog("Making error dialog for failure: " + mSignInFailureReason);
        Dialog errorDialog;

        switch (mSignInFailureReason.getActivityResultCode()) {
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                errorDialog = makeSimpleDialog(ctx.getString(R.string.gamehelper_app_misconfigured));
                printMisconfiguredDebugInfo();
                break;
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                errorDialog = makeSimpleDialog(ctx.getString(R.string.gamehelper_sign_in_failed));
                break;
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                errorDialog = makeSimpleDialog(ctx.getString(R.string.gamehelper_license_failed));
                break;
            default:
                // No meaningful Activity response code, so generate default Google Play services dialog
                errorDialog = getGooglePlayServicesErrorDialog(ctx, mSignInFailureReason.getServiceErrorCode());
                break;
        }

        debugLog("Showing error dialog.");
        errorDialog.show();
    }

    private Dialog makeSimpleDialog(String text) {
        return new AlertDialog.Builder(getContext()).setMessage(text)
                .setNeutralButton(android.R.string.ok, null)
                .create();
    }

    private Dialog getGooglePlayServicesErrorDialog(Context ctx, int errorCode) {
        Dialog errorDialog;
        errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, mActivity, RC_UNUSED, null);
        if (errorDialog == null) {
            // get fallback dialog
            debugLog("No standard error dialog available. Making fallback dialog.");
            errorDialog = makeSimpleDialog(
                    ctx.getString(R.string.gamehelper_unknown_error) + " " + errorCodeToString(errorCode));
        }
        return errorDialog;
    }

    private void debugLog(String message) {
        if (mDebugLog) {
            Log.d(mDebugTag, "GameHelper: " + message);
        }
    }

    private void debugLog(String message, Throwable tr) {
        if (mDebugLog) {
            Log.d(mDebugTag, "GameHelper: " + message, tr);
        }
    }

    private void logWarn(String message) {
        Log.w(mDebugTag, "!!! GameHelper WARNING: " + message);
    }

    private void logError(String message) {
        Log.e(mDebugTag, "*** GameHelper ERROR: " + message);
    }

    private static String errorCodeToString(int errorCode) {
        switch (errorCode) {
            case ConnectionResult.DEVELOPER_ERROR:
                return "DEVELOPER_ERROR(" + errorCode + ")";
            case ConnectionResult.INTERNAL_ERROR:
                return "INTERNAL_ERROR(" + errorCode + ")";
            case ConnectionResult.INVALID_ACCOUNT:
                return "INVALID_ACCOUNT(" + errorCode + ")";
            case ConnectionResult.LICENSE_CHECK_FAILED:
                return "LICENSE_CHECK_FAILED(" + errorCode + ")";
            case ConnectionResult.NETWORK_ERROR:
                return "NETWORK_ERROR(" + errorCode + ")";
            case ConnectionResult.RESOLUTION_REQUIRED:
                return "RESOLUTION_REQUIRED(" + errorCode + ")";
            case ConnectionResult.SERVICE_DISABLED:
                return "SERVICE_DISABLED(" + errorCode + ")";
            case ConnectionResult.SERVICE_INVALID:
                return "SERVICE_INVALID(" + errorCode + ")";
            case ConnectionResult.SERVICE_MISSING:
                return "SERVICE_MISSING(" + errorCode + ")";
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                return "SERVICE_VERSION_UPDATE_REQUIRED(" + errorCode + ")";
            case ConnectionResult.SIGN_IN_REQUIRED:
                return "SIGN_IN_REQUIRED(" + errorCode + ")";
            case ConnectionResult.SUCCESS:
                return "SUCCESS(" + errorCode + ")";
            default:
                return "Unknown error code " + errorCode;
        }
    }

    // Represents the reason for a sign-in failure
    public static class SignInFailureReason {
        public static final int NO_ACTIVITY_RESULT_CODE = -100;
        int mServiceErrorCode = 0;
        int mActivityResultCode = NO_ACTIVITY_RESULT_CODE;

        public int getServiceErrorCode() {
            return mServiceErrorCode;
        }

        public int getActivityResultCode() {
            return mActivityResultCode;
        }

        public SignInFailureReason(int serviceErrorCode, int activityResultCode) {
            mServiceErrorCode = serviceErrorCode;
            mActivityResultCode = activityResultCode;
        }

        public SignInFailureReason(int serviceErrorCode) {
            this(serviceErrorCode, NO_ACTIVITY_RESULT_CODE);
        }

        @Override
        public String toString() {
            return "SignInFailureReason(serviceErrorCode:" + errorCodeToString(
                    mServiceErrorCode) + (mActivityResultCode == NO_ACTIVITY_RESULT_CODE ? ")" : "," +
                    "activityResultCode:" + activityResponseCodeToString(mActivityResultCode) + ")");
        }
    }

    private void printMisconfiguredDebugInfo() {
        debugLog("****");
        debugLog("****");
        debugLog("**** APP NOT CORRECTLY CONFIGURED TO USE GOOGLE PLAY GAME SERVICES");
        debugLog("**** This is usually caused by one of these reasons:");
        debugLog("**** (1) Your package name and certificate fingerprint do not match");
        debugLog("****     the client ID you registered in Developer Console.");
        debugLog("**** (2) Your App ID was incorrectly entered.");
        debugLog("**** (3) Your game settings have not been published and you are ");
        debugLog("****     trying to log in with an account that is not listed as");
        debugLog("****     a test account.");
        debugLog("****");
        Context ctx = getContext();
        if (ctx == null) {
            debugLog("*** (no Context, so can't print more debug info)");
            return;
        }

        debugLog("**** To help you debug, here is the information about this app");
        debugLog("**** Package name         : " + getContext().getPackageName());
        debugLog("**** Cert SHA1 fingerprint: " + getSHA1CertFingerprint());
        debugLog("**** App ID from          : " + getAppIdFromResource());
        debugLog("****");
        debugLog("**** Check that the above information matches your setup in ");
        debugLog("**** Developer Console. Also, check that you're logging in with the");
        debugLog("**** right account (it should be listed in the Testers section if");
        debugLog("**** your project is not yet published).");
        debugLog("****");
        debugLog("**** For more information, refer to the troubleshooting guide:");
        debugLog("****   http://developers.google.com/games/services/android/troubleshooting");
    }

    private String getAppIdFromResource() {
        try {
            Resources res = getContext().getResources();
            String pkgName = getContext().getPackageName();
            int resId = res.getIdentifier("app_id", "string", pkgName);
            return res.getString(resId);
        } catch (Exception ex) {
            debugLog("failed to retrieve APP ID.", ex);
            return FAILED;
        }
    }

    private String getSHA1CertFingerprint() {
        try {
            return tryToGetSHA1CertFingerprint();
        } catch (PackageManager.NameNotFoundException ex) {
            debugLog("ERROR: package not found.", ex);
            return FAILED;
        } catch (NoSuchAlgorithmException ex) {
            debugLog("ERROR: SHA1 algorithm not found.", ex);
            return FAILED;
        }
    }

    private String tryToGetSHA1CertFingerprint() throws PackageManager.NameNotFoundException, NoSuchAlgorithmException {
        // noinspection ConstantConditions
        Signature[] sigs = getContext().getPackageManager()
                .getPackageInfo(getContext().getPackageName(), PackageManager.GET_SIGNATURES).signatures;
        if (sigs == null || sigs.length == 0) {
            return "ERROR: NO SIGNATURE.";
        } else if (sigs.length > 1) {
            return "ERROR: MULTIPLE SIGNATURES";
        }
        byte[] digest = MessageDigest.getInstance("SHA1")
                .digest(sigs[0].toByteArray());
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < digest.length; ++i) {
            if (i > 0) {
                hexString.append(":");
            }
            byteToString(hexString, digest[i]);
        }
        return hexString.toString();
    }

    private void byteToString(StringBuilder sb, byte b) {
        int unsignedByte = b < 0 ? b + 256 : b;
        int hi = unsignedByte / 16;
        int lo = unsignedByte % 16;
        sb.append("0123456789ABCDEF".substring(hi, hi + 1));
        sb.append("0123456789ABCDEF".substring(lo, lo + 1));
    }
}