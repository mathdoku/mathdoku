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

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.appstate.AppStateClient;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.plus.PlusClient;

/**
 * Example base class for games. This implementation takes care of setting up
 * the GamesClient object and managing its lifecycle. Subclasses only need to
 * override the @link{#onSignInSucceeded} and @link{#onSignInFailed} abstract
 * methods. To initiate the sign-in flow when the user clicks the sign-in
 * button, subclasses should call @link{#beginUserInitiatedSignIn}. By default,
 * this class only instantiates the GamesClient object. If the PlusClient or
 * AppStateClient objects are also wanted, call the
 * GooglePlayServiceFragmentActivity(int) constructor and specify the requested
 * clients. For example, to request PlusClient and GamesClient, use
 * GooglePlayServiceFragmentActivity(CLIENT_GAMES | CLIENT_PLUS). To request all
 * available clients, use GooglePlayServiceFragmentActivity(CLIENT_ALL).
 * Alternatively, you can also specify the requested clients via
 * 
 * @author Bruno Oliveira (Google)
 * @link{#setRequestedClients , but you must do so before @link{#onCreate} gets
 *                            called, otherwise the call will have no effect.
 */
@SuppressWarnings("WeakerAccess")
public abstract class GooglePlayServiceFragmentActivity extends
		AppFragmentActivity implements GameHelper.GameHelperListener {

	// The game helper object. This class is mainly a wrapper around this
	// object.
	private GameHelper mGameHelper;

	// Request code to be used when invoking other Activities to complete the
	// sign-in flow.
	public static final int RC_RESOLVE = GameHelper.RC_RESOLVE;

	// Request code to be used when invoking Activities whose result is not
	// cared about.
	public static final int RC_UNUSED = GameHelper.RC_UNUSED;

	// We expose these constants here because we don't want users of this class
	// to have to know about GameHelper at all.
	@SuppressWarnings("WeakerAccess")
	private static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
	public static final int CLIENT_APPSTATE = GameHelper.CLIENT_APPSTATE;
	public static final int CLIENT_PLUS = GameHelper.CLIENT_PLUS;
	public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;

	// Requested clients. By default, that's just the games client.
	@SuppressWarnings("WeakerAccess")
	private int mRequestedClients = CLIENT_GAMES;

	// stores any additional scopes.
	private String[] mAdditionalScopes;

	private String mDebugTag = "GooglePlayServiceFragmentActivity";
	private boolean mDebugLog = false;

	/**
	 * Constructs a GooglePlayServiceFragmentActivity with default client
	 * (GamesClient).
	 */
	protected GooglePlayServiceFragmentActivity() {
		super();
		mGameHelper = new GameHelper(this);
	}

	/**
	 * Constructs a GooglePlayServiceFragmentActivity with the requested
	 * clients.
	 * 
	 * @param requestedClients
	 *            The requested clients (a combination of CLIENT_GAMES,
	 *            CLIENT_PLUS and CLIENT_APPSTATE).
	 */
	protected GooglePlayServiceFragmentActivity(int requestedClients) {
		super();
		setRequestedClients(requestedClients);
	}

	/**
	 * Sets the requested clients. The preferred way to set the requested
	 * clients is via the constructor, but this method is available if for some
	 * reason your code cannot do this in the constructor. This must be called
	 * before onCreate in order to have any effect. If called after onCreate,
	 * this method is a no-op.
	 * 
	 * @param requestedClients
	 *            A combination of the flags CLIENT_GAMES, CLIENT_PLUS and
	 *            CLIENT_APPSTATE, or CLIENT_ALL to request all available
	 *            clients.
	 * @param additionalScopes
	 *            . Scopes that should also be requested when the auth request
	 *            is made.
	 */
	void setRequestedClients(int requestedClients, String... additionalScopes) {
		mRequestedClients = requestedClients;
		mAdditionalScopes = additionalScopes;
	}

	@Override
	public void onCreate(Bundle b) {
		super.onCreate(b);
		mGameHelper = new GameHelper(this);
		if (mDebugLog) {
			mGameHelper.enableDebugLog(mDebugTag);
		}
		mGameHelper.setup(this, mRequestedClients, mAdditionalScopes);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGameHelper.onStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mGameHelper.onStop();
	}

	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		mGameHelper.onActivityResult(request, response);
	}

	protected GamesClient getGamesClient() {
		return mGameHelper.getGamesClient();
	}

	protected AppStateClient getAppStateClient() {
		return mGameHelper.getAppStateClient();
	}

	protected PlusClient getPlusClient() {
		return mGameHelper.getPlusClient();
	}

	protected boolean isSignedIn() {
		return mGameHelper.isSignedIn();
	}

	protected void beginUserInitiatedSignIn() {
		mGameHelper.beginUserInitiatedSignIn();
	}

	protected void signOut() {
		mGameHelper.signOut();
	}

	protected void showAlert(String title, String message) {
		mGameHelper.showAlert(title, message);
	}

	protected void showAlert(String message) {
		mGameHelper.showAlert(message);
	}

	@SuppressWarnings("SameParameterValue")
	protected void enableDebugLog(boolean enabled, String tag) {
		mDebugLog = enabled;
		mDebugTag = tag;
		if (mGameHelper != null) {
			mGameHelper.enableDebugLog(tag);
		}
	}

	protected String getInvitationId() {
		return mGameHelper.getInvitationId();
	}

	protected void reconnectClients(int whichClients) {
		mGameHelper.reconnectClients(whichClients);
	}

	protected String getScopes() {
		return mGameHelper.getScopes();
	}

	protected String[] getScopesArray() {
		return mGameHelper.getScopesArray();
	}

	protected boolean hasSignInError() {
		return mGameHelper.hasSignInError();
	}

	protected GameHelper.SignInFailureReason getSignInError() {
		return mGameHelper.getSignInError();
	}
}
