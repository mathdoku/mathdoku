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

import com.google.android.gms.games.GamesClient;

/**
 * Based on an example base class for games written by Bruno Oliveira (Google). The class is simplied to support the
 * GAMES_CLIENT ONLY
 */
@SuppressWarnings("WeakerAccess")
public abstract class GooglePlayServiceFragmentActivity extends AppFragmentActivity implements GameHelper
        .GameHelperListener {

    // The game helper object. This class is mainly a wrapper around this object.
    private GameHelper mGameHelper;

    // Request code to be used when invoking Activities whose result is not cared about.
    public static final int RC_UNUSED = GameHelper.RC_UNUSED;

    private String mDebugTag = GooglePlayServiceFragmentActivity.class.getName();
    private boolean mDebugLog = false;

    /**
     * Constructs a GooglePlayServiceFragmentActivity with default client (GamesClient).
     */
    protected GooglePlayServiceFragmentActivity() {
        super();
        mGameHelper = new GameHelper(this);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        mGameHelper = new GameHelper(this);
        if (mDebugLog) {
            mGameHelper.enableDebugLog(mDebugTag);
        }
        mGameHelper.setup(this);
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

    protected void beginUserInitiatedSignIn() {
        mGameHelper.beginUserInitiatedSignIn();
    }

    protected void signOut() {
        mGameHelper.signOut();
    }

    @SuppressWarnings("SameParameterValue")
    protected void enableDebugLog(boolean enabled, String tag) {
        mDebugLog = enabled;
        mDebugTag = tag;
        if (mGameHelper != null) {
            mGameHelper.enableDebugLog(tag);
        }
    }
}
