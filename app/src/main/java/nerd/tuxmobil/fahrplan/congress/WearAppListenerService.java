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
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class WearAppListenerService extends WearableListenerService {

    private static final String TAG = "CampFahrplan:WearAppListenerService";

    public static final String ACTION_REFRESH_LECTURE_DATA = "nerd.tuxmobil.fahrplan.congress.REFRESH_LECTURE_DATA";

    public static final String ACTION_REFRESH_SINGLE_LECTURE_DATA = "nerd.tuxmobil.fahrplan.congress.REFRESH_SINGLE_LECTURE_DATA";

    public static final String PATH_REQUEST_NEW_LECTURE_DATA = "/request-new-lecture-data";

    public static final String PATH_LECTURE_DATA = "/lecture-data";

    public static final String KEY_LECTURE_DATA = "lecture_";

    private static LectureList lectures = null;

    private static Lecture singleLecture = null;

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
    public void onDestroy() {
        super.onDestroy();

        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    public static void requestRefreshSingleLectureData(Context context, Lecture lecture) {
        Log.d(TAG, "requestRefreshSingleLectureData");
        singleLecture = lecture;

        Intent intent = new Intent(context, WearAppListenerService.class);
        intent.setAction(ACTION_REFRESH_SINGLE_LECTURE_DATA);

        context.startService(intent);
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

        if (intent != null) {
            if (ACTION_REFRESH_LECTURE_DATA.equals(intent.getAction())) {
                doUpdateAllLectures();
            } else if (ACTION_REFRESH_SINGLE_LECTURE_DATA.equals(intent.getAction())) {
                doUpdateSingleLecture();
            }
        }

        return superResult;
    }

    private void doUpdateAllLectures() {
        Log.i(TAG, "REFRESH_LECTURE_DATA command received");

        // according to javadoc of onStartCommand, long operations should be started within e.g. a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run in background for doUpdateAllLectures");
                handleSendLectureData(WearHelper.buildDataMapListFromLectures(WearHelper.filterLectures(lectures)));
                lectures = null;

                Log.d(TAG, "stopping service again");
                // TODO look how this behaves if the service is simultaneously bound by Google Play Services
                stopSelf();
            }
        }).start();
    }

    private void doUpdateSingleLecture() {
        Log.i(TAG, "ACTION_REFRESH_SINGLE_LECTURE_DATA command received");

        // according to javadoc of onStartCommand, long operations should be started within e.g. a thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run in background for doUpdateSingleLecture");
                handleUpdateSingleLectureData(WearHelper.buildDataMapFromLecture(singleLecture));
                singleLecture = null;

                Log.d(TAG, "stopping service again");
                // TODO look how this behaves if the service is simultaneously bound by Google Play Services
                stopSelf();
            }
        }).start();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived: " + messageEvent);

        if (messageEvent.getPath().equals(PATH_REQUEST_NEW_LECTURE_DATA)) {
            handleSendLectureData(WearHelper.buildDataMapListFromLectures(WearHelper.filterLectures(FahrplanMisc.loadLecturesForAllDays(this))));
        }
    }

    private void handleSendLectureData(ArrayList<DataMap> lectures) {
        if (!handleGoogleApiConnect()) {
            return;
        }

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(PATH_LECTURE_DATA);
        // attention: a update event will only be delivered to the wear app if something has really changed!
        DataMap map = dataMapRequest.getDataMap();
        for (DataMap lecture : lectures) {
            map.putDataMap(KEY_LECTURE_DATA + lecture.getString("id"), lecture);
        }

        sendDataMap(dataMapRequest);
    }

    private void handleUpdateSingleLectureData(DataMap lecture) {
        if (!handleGoogleApiConnect()) {
            return;
        }

        // get previous datamap (as we override the existing one otherwise!)
        NodeApi.GetLocalNodeResult nodeResult = Wearable.NodeApi.getLocalNode(googleApiClient).await();
        if (!nodeResult.getStatus().isSuccess()) {
            Log.e(TAG, "handleUpdateSingleLectureData failed as local node discovery was unsuccessful");
            return;
        }

        DataMap dataMap = null;
        DataApi.DataItemResult result = Wearable.DataApi.getDataItem(googleApiClient,
                    getUriForLectureData(nodeResult.getNode().getId())).await();
        if (result.getStatus().isSuccess()) {
            dataMap = DataMapItem.fromDataItem(result.getDataItem()).getDataMap();
        }

        if (dataMap == null) {
            Log.e(TAG, "handleUpdateSingleLectureData failed: dataMap == null");
            return;
        }

        Log.d(TAG, "handleUpdateSingleLectureData: dataMap currently contains: " + dataMap.keySet());

        dataMap.putDataMap(KEY_LECTURE_DATA + lecture.getString("id"), lecture);
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(PATH_LECTURE_DATA);

        dataMapRequest.getDataMap().putAll(dataMap);
        sendDataMap(dataMapRequest);
    }

    private Uri getUriForLectureData(String nodeId) {
        return new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(nodeId)
                .path(PATH_LECTURE_DATA).build();
    }

    private boolean handleGoogleApiConnect() {
        if (!googleApiClient.isConnected()) {
            Log.e(TAG, "not connected to google api, connecting now (blocking)");

            // if called by the Google APIs, this everything is executed in a separate thread; so this is okay
            ConnectionResult connectionResult = googleApiClient.blockingConnect(1, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(TAG, "connection to google api failed");
                return false;
            }
        }

        return true;
    }

    private void sendDataMap(PutDataMapRequest dataMapRequest) {
        Wearable.DataApi.putDataItem(googleApiClient, dataMapRequest.asPutDataRequest())
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.e(TAG, "ERROR: failed to putDataItem, status code: "
                                    + result.getStatus().getStatusCode());
                        } else {
                            Log.d(TAG, "sent items successfully to nodes");
                        }
                    }
                });
    }
}
