package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private ProgressDialog progressDialog;

    private GoogleApiClient googleApiClient;
    private DotsPageIndicator dotsPageIndicator;
    private GridViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = ProgressDialog.show(this, getString(R.string.activity_main_loading_data),
                null, true, true);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                LogUtil.info("user dismissed dialog, exiting activity");
                MainActivity.this.finish();
            }
        });

        // Taken from the GridViewPager sample of the android wear v21 sdk
        final Resources res = getResources();
        pager = (GridViewPager) findViewById(R.id.pager);
        pager.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // Adjust page margins:
                //   A little extra horizontal spacing between pages looks a bit
                //   less crowded on a round display.
                final boolean round = insets.isRound();
                int rowMargin = res.getDimensionPixelOffset(R.dimen.page_row_margin);
                int colMargin = res.getDimensionPixelOffset(round ? R.dimen.page_column_margin_round : R.dimen.page_column_margin);
                pager.setPageMargins(rowMargin, colMargin);

                // GridViewPager relies on insets to properly handle
                // layout for round displays. They must be explicitly
                // applied since this listener has taken them over.
                pager.onApplyWindowInsets(insets);
                return insets;
            }
        });

        dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.pager_dots_indicator);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        LogUtil.debug("onCreate finished");
    }

    @Override
    protected void onStart() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient.isConnected()) {
            LogUtil.debug("onStop: disconnecting google apis");

            Wearable.DataApi.removeListener(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        LogUtil.debug("connected to google apis");

        Wearable.DataApi.addListener(googleApiClient, this);

        // check if lecture data is available ... if not, get the data from the app


        new AsyncTask<Void, Void, String[]>() {

            @Override
            protected String[] doInBackground(Void... params) {
                try {
                    // try to get the current data first
                    // see: http://stackoverflow.com/questions/24601251/what-is-the-uri-for-wearable-dataapi-getdataitem-after-using-putdatamaprequest
                    DataApi.DataItemResult result = Wearable.DataApi.getDataItem(googleApiClient, getUriForDataItem()).await();
                    if (!result.getStatus().isSuccess()) {
                        throw new Exception("result status is not success");
                    }

                    final DataMap map = DataMapItem.fromDataItem(result.getDataItem()).getDataMap();
                    if (map == null) {
                        throw new Exception("map is null");
                    }

                    String[] lectures = map.getStringArray(Constants.KEY_LECTURE_DATA);
                    if (lectures == null) {
                        throw new Exception("no lectures in store");
                    }

                    return lectures;
                } catch (Exception e) {
                    NodeApi.GetConnectedNodesResult foundNodes = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                    // send request for lecture data
                    for (Node node : foundNodes.getNodes()) {
                        LogUtil.debug("requesting lecture data from " + node.getDisplayName());
                        Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), Constants.PATH_REQUEST_NEW_LECTURE_DATA, new byte[] {});
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(String[] lectures) {
                super.onPostExecute(lectures);

                if (lectures == null) {
                    return;
                }

                LogUtil.debug("taking pre-stored lectures");
                fillPagerWithData(lectures);
            }

            private Uri getUriForDataItem() {
                return new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME).authority(getLocalNodeId()).path(Constants.PATH_LECTURE_DATA).build();
            }

            private String getLocalNodeId() {
                NodeApi.GetLocalNodeResult nodeResult = Wearable.NodeApi.getLocalNode(googleApiClient).await();
                return nodeResult.getNode().getId();
            }

        }.execute();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Wearable.DataApi.removeListener(googleApiClient, this);

        progressDialog.cancel();
        finish();

        Toast.makeText(this, R.string.activity_main_connection_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        for (DataEvent event : events) {
            if (event.getDataItem().getUri().getPath().equals(Constants.PATH_LECTURE_DATA)) {
                DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                fillPagerWithData(map.getStringArray(Constants.KEY_LECTURE_DATA));
            }
        }
    }

    private void fillPagerWithData(String[] lectures) {
        pager.setAdapter(new SampleGridPagerAdapter(this, getFragmentManager()));
        dotsPageIndicator.setPager(pager);

        progressDialog.cancel();
    }
}
