package nerd.tuxmobil.fahrplan.congress;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppListenerService extends WearableListenerService {

    private static final String TAG = "CampFahrplan:AppListenerService";

    public AppListenerService() {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "onDataChanged: " + dataEvents);
        }

        final List events = FreezableUtils.freezeIterable(dataEvents);

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                googleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(TAG, "Failed to connect to GoogleApiClient.");
            return;
        }


        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                /*if (PATH.equals(path)) {
                    // Get the data out of the event
                    DataMapItem dataMapItem =
                            DataMapItem.fromDataItem(event.getDataItem());
                    final String title = dataMapItem.getDataMap().getString(KEY_TITLE);
                    Asset asset = dataMapItem.getDataMap().getAsset(KEY_IMAGE);

                    // Build the intent to display our custom notification
                    Intent notificationIntent =
                            new Intent(this, NotificationActivity.class);
                    notificationIntent.putExtra(
                            NotificationActivity.EXTRA_TITLE, title);
                    notificationIntent.putExtra(
                            NotificationActivity.EXTRA_IMAGE, asset);
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                            this,
                            0,
                            notificationIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    // Create the ongoing notification
                    Notification.Builder notificationBuilder =
                            new Notification.Builder(this)
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .setLargeIcon(BitmapFactory.decodeResource(
                                            getResources(), R.drawable.ic_launcher))
                                    .setOngoing(true)
                                    .extend(new Notification.WearableExtender()
                                            .setDisplayIntent(notificationPendingIntent));

                    // Build the notification and show it
                    NotificationManager notificationManager =
                            (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(
                            NOTIFICATION_ID, notificationBuilder.build());
                } else {
                    Log.d(TAG, "Unrecognized path: " + path);
                }*/
            }
        }
    }
}
