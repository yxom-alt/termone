/*
 * Copyright (C) 2015 Steven Luo
 * Copyright (C) 2019 Roumen Petrov.  All rights reserved.
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

package jackpal.androidterm;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.termoneplus.Application;

import java.security.GeneralSecurityException;

import jackpal.androidterm.util.ShortcutEncryption;


public final class RunShortcut extends RemoteInterface {
    public static final String ACTION_RUN_SHORTCUT = "com.termoneplus.RUN_SHORTCUT";

    public static final String RUN_SHORTCUT_WINDOW_HANDLE = "com.termoneplus.WindowHandle";
    public static final String RUN_SHORTCUT_COMMAND = "com.termoneplus.Command";

    @Override
    protected void handleIntent() {
        TermService service = getTermService();
        Intent intent = null;
        String action = null;

        if (service != null) intent = getIntent();
        if (intent != null) action = intent.getAction();
        if (action != null)
            processAction(intent, action);

        finish();
    }

    private void processAction(@NonNull Intent intent, @NonNull String action) {
        switch (action) {
            case ACTION_RUN_SHORTCUT:
                runShortcut(intent);
                break;
        }
    }

    private void runShortcut(@NonNull Intent intent) {
        // Decrypt and verify the command
        String command;
        try {
            String request = intent.getStringExtra(RUN_SHORTCUT_COMMAND);
            if (request == null) {
                Log.e(Application.APP_TAG, "No command provided in shortcut!");
                return;
            }
            ShortcutEncryption.Keys keys = ShortcutEncryption.getKeys(this);
            if (keys == null) {
                // No keys -- no valid shortcuts can exist
                Log.e(Application.APP_TAG, "No shortcut encryption keys found!");
                return;
            }
            command = ShortcutEncryption.decrypt(request, keys);
        } catch (GeneralSecurityException e) {
            Log.e(Application.APP_TAG, "Invalid shortcut: " + e.toString());
            return;
        }

        String handle = intent.getStringExtra(RUN_SHORTCUT_WINDOW_HANDLE);
        if (handle != null) {
            // Target the request at an existing window if open
            handle = appendToWindow(handle, command);
        } else {
            // Open a new window
            handle = openNewWindow(command);
        }

        Intent result = new Intent();
        result.putExtra(RUN_SHORTCUT_WINDOW_HANDLE, handle);
        setResult(RESULT_OK, result);
    }
}
