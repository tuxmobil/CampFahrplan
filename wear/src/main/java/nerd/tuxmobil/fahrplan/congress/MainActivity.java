package nerd.tuxmobil.fahrplan.congress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private ProgressDialog progressDialog;

    private GoogleApiClient googleApiClient;
    private DotsPageIndicator dotsPageIndicator;
    private GridViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = ProgressDialog.show(this, getString(R.string.activity_main_loading_data),
                null, true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Wearable.DataApi.removeListener(googleApiClient, this);
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LogUtil.debug("connected to google apis");

        Wearable.DataApi.addListener(googleApiClient, this);

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(googleApiClient).await();
                // send request for lecture data
                for (Node node : result.getNodes()) {
                    LogUtil.debug("requesting lecture data from " + node.getDisplayName());
                    Wearable.MessageApi.sendMessage(googleApiClient, node.getId(), Constants.PATH_REQUEST_LECTURE_DATA, new byte[] {});
                }

                return null;
            }

        }.execute();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        progressDialog.dismiss();
        finish();

        Toast.makeText(this, R.string.activity_main_connection_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();

        LogUtil.debug("onDataChanged: received data = " + events);

        for (DataEvent event : events) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();



            } else if (event.getType() == DataEvent.TYPE_DELETED) {

            } else {

            }
        }

//        pager.setAdapter(new SampleGridPagerAdapter(this, getFragmentManager()));
//        dotsPageIndicator.setPager(pager);
    }
}
