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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class WearAppListenerService extends WearableListenerService {

    private static final String TAG = "CampFahrplan:WearAppListenerService";

    public static final String PATH_REQUEST_LECTURE_DATA = "/request-lecture-data";

    public static final String PATH_LECTURE_DATA = "/lecture-data";

    public static final String KEY_LECTURE_DATA = "lectures";

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

        // filter out lectures which are completed or too far in the future
        List<Lecture> filteredLectures = filterLectures(lectures);

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(PATH_LECTURE_DATA);
        dataMapRequest.getDataMap().putStringArray(KEY_LECTURE_DATA, buildArrayFromLectures(filteredLectures));

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

    private List<Lecture> filterLectures(List<Lecture> lectures) {
        List<Lecture> results = new ArrayList<Lecture>();
        for (Lecture lecture : lectures) {
            // TODO implement something here
            results.add(lecture);
        }

        return results;
    }

    private String[] buildArrayFromLectures(List<Lecture> lectures) {
        String[] results = new String[lectures.size()];
        int indexCounter = 0;

        for (Lecture lecture : lectures) {
            JSONObject lectureAsJson = new JSONObject();
            try {
                lectureAsJson.put("title", lecture.title);
                lectureAsJson.put("speakers", lecture.speakers);
                lectureAsJson.put("highlight", lecture.highlight);
                lectureAsJson.put("start_time", lecture.relStartTime);
                lectureAsJson.put("end_time", lecture.relStartTime + lecture.duration);
                lectureAsJson.put("room", lecture.room);
                lectureAsJson.put("room_index", lecture.room_index);

                results[indexCounter] = lectureAsJson.toString();
                ++indexCounter;
            } catch (JSONException e) {
                Log.e(TAG, "failed building array from lectures (current: " + lecture.lecture_id + ")", e);
            }
        }

        return results;
    }
}
