/*
 * Copyright (C) 2014 The Android Open Source Project
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

package nerd.tuxmobil.fahrplan.congress;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class WearAppListenerService extends WearableListenerService {

    private static final String TAG = "CampFahrplan:WearAppListenerService";

    public static final String PATH_REQUEST_LECTURE_DATA = "/request-lecture-data";

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        if (messageEvent.getPath().equals(PATH_REQUEST_LECTURE_DATA)) {
            handleRequestLectureData(messageEvent.getSourceNodeId());
        }
    }

    private void handleRequestLectureData(String sourceNodeId) {
        if(!googleApiClient.isConnected()) {
            Log.e(TAG, "not connected to google api, connecting now (blocking)");

            ConnectionResult connectionResult = googleApiClient.blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "connection to google api failed");
                return;
            }
        }

        List<Lecture> lectures = MyApp.lectureList;
        if (lectures == null) {
            Log.w(TAG, "no lectures found");
            return;
        }


    }
}
