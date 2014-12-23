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

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

public class WearAppListenerService extends WearableListenerService {

    private static final String TAG = "CampFahrplan:WearAppListenerService";

    public static final String ACTION_REFRESH_LECTURE_DATA = "nerd.tuxmobil.fahrplan.congress.REFRESH_LECTURE_DATA";

    public static final String PATH_REQUEST_NEW_LECTURE_DATA = "/request-new-lecture-data";

    public static final String PATH_LECTURE_DATA = "/lecture-data";

    public static final String KEY_LECTURE_DATA = "lectures";

    public static final String KEY_SCHEDULE_VERSION = "schedule_version";

    private static LectureList lectures = null;

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        googleApiClient.connect();
    }

    public static void requestRefreshLectureData(Context context, LectureList lectureList) {
        Log.d(TAG, "requestRefreshLectureData");
        lectures = lectureList;

        Intent intent = new Intent(context, WearAppListenerService.class);
        intent.setAction(ACTION_REFRESH_LECTURE_DATA);

        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int superResult = super.onStartCommand(intent, flags, startId);

        if (intent != null && ACTION_REFRESH_LECTURE_DATA.equals(intent.getAction())) {
            Log.i(TAG, "onStartCommand: REFRESH_LECTURE_DATA command received");

            // according to javadoc of onStartCommand, long operations should be started within e.g. a thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run in background for handleSendLectureData");
                    handleSendLectureData(WearHelper.buildArrayFromLectures(WearHelper.filterLectures(lectures)));
                    lectures = null;

                    Log.d(TAG, "stopping service again");
                    // TODO look how this behaves if the service is simultaneously bound by Google Play Services
                    stopSelf();
                }
            }).start();
        }

        return superResult;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        if (messageEvent.getPath().equals(PATH_REQUEST_NEW_LECTURE_DATA)) {
            handleSendLectureData(WearHelper.buildArrayFromLectures(WearHelper.filterLectures(FahrplanMisc.loadLecturesForAllDays(this))));
        }
    }

    private void handleSendLectureData(String[] lectures) {
        if(!googleApiClient.isConnected()) {
            Log.e(TAG, "not connected to google api, connecting now (blocking)");

            // if called by the Google APIs, this everything is executed in a separate thread; so this is okay
            ConnectionResult connectionResult = googleApiClient.blockingConnect(1, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "connection to google api failed");
                return;
            }
        }

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(PATH_LECTURE_DATA);
        // attention: a update event will only be delivered to the wear app if something has really changed!
        dataMapRequest.getDataMap().putStringArray(KEY_LECTURE_DATA, lectures);
        dataMapRequest.getDataMap().putString(KEY_SCHEDULE_VERSION, MyApp.version);
        // TODO use for lectures for more efficient transmission
//        dataMapRequest.getDataMap().putDataMapArrayList();

        Wearable.DataApi.putDataItem(googleApiClient, dataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "ERROR: failed to putDataItem, status code: " + result.getStatus().getStatusCode());
                        } else {
                            Log.d(TAG, "sent items successfully to wear app");
                        }
                    }
                });
    }
}
