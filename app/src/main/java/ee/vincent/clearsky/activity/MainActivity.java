package ee.vincent.clearsky.activity;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import ee.vincent.clearsky.Conf;
import ee.vincent.clearsky.R;
import ee.vincent.clearsky.adapter.ListItemDecoration;
import ee.vincent.clearsky.adapter.RoutesListAdapter;
import ee.vincent.clearsky.database.Datasource;
import ee.vincent.clearsky.dialog.NewRouteDialog;
import ee.vincent.clearsky.model.Route;
import ee.vincent.clearsky.service.LocationService;

/**
 * Created by jakob on 2.01.2015.
 */
public class MainActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        NewRouteDialog.NewRouteDialogListener {

    private static final String TAG = "MainActivity";

    // UI refs
    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private RoutesListAdapter mAdapter;

    // Google API client to register location callbacks
    private GoogleApiClient googleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean resolvingError = false;
    private static final String STATE_RESOLVING_ERROR = "resolving_error";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        initUI();

        // setup location API
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!resolvingError) {  // more about this later
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_RESOLVE_ERROR) {
            resolvingError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!googleApiClient.isConnecting() &&
                        googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, resolvingError);
    }


    private void initUI() {

        recyclerView = (RecyclerView) findViewById(R.id.routes_list);
        Button startNewTrackerBtn = (Button)findViewById(R.id.btn_start_new_tracker);
        Button closeAppBtn = (Button)findViewById(R.id.btn_close_app);

        startNewTrackerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartNewTrackerBtnClick();
            }
        });

        closeAppBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseAppBtnClick();
            }
        });


        // initialize routes listview
        List<Route> routes = Datasource.getInstance(this).getRoutes();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.addItemDecoration(new ListItemDecoration(
                getResources().getDrawable(R.drawable.abc_list_divider_mtrl_alpha)));

        // specify an adapter (see also next example)
        mAdapter = new RoutesListAdapter(routes);
        recyclerView.setAdapter(mAdapter);

    }

    private void onStartNewTrackerBtnClick() {

        if ( googleApiClient.isConnected() ) {
            startTracking();
        } else {
            Log.e(TAG, "Cannot start new tracker, googleApiClient not connected!");
            if ( !googleApiClient.isConnecting() )
                googleApiClient.connect();
        }

    }

    private void onCloseAppBtnClick() {

        // stop location updates
        if ( googleApiClient.isConnected() ) {
            stopTracking();
            finish();
        } else {
            Log.e(TAG, "Cannot close app, googleApiClient not connected!");
            if ( !googleApiClient.isConnecting() )
                googleApiClient.connect();
        }

    }

    private void startTracking() {

        DialogFragment dialog = new NewRouteDialog();
        dialog.show(getFragmentManager(), NewRouteDialog.TAG);
    }

    @Override
    public void NewRouteDialogSuccess(String routeName) {

        if ( TextUtils.isEmpty(routeName) ) {
            DialogFragment dialog = new NewRouteDialog();
            dialog.show(getFragmentManager(), NewRouteDialog.TAG);
            Toast.makeText(this, getString(R.string.toast_insert_name),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // create new route
        Route route = new Route();
        route.setName(routeName);
        route.setCreated(System.currentTimeMillis());
        long routeId = Datasource.getInstance(this).insertRoute(route);

        // initialize LocationService with routeId
        LocationService.setRouteId(routeId);

        // start location updates that are delivered
        // to LocationService via PendingIntent
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(Conf.LOCATION_UPDATES_INTERVAL);
        locationRequest.setFastestInterval(Conf.LOCATION_UPDATES_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        Intent intent = new Intent(this, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, 0);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest, pendingIntent);

        // goto Route view
        intent = new Intent(this, RouteActivity.class);
        startActivity(intent);
    }

    private void stopTracking() {

        Intent intent = new Intent(this, LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, 0);

        LocationServices.FusedLocationApi
                .removeLocationUpdates(googleApiClient, pendingIntent)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.e(TAG, "LocationUpdates canceled! " + status.toString() + " status message: " + status.getStatusMessage());

                        // stop the service
                        Intent intent = new Intent(MainActivity.this, LocationService.class);
                        stopService(intent);
                    }
                });
    }


    // Gogole API stuff
    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.e(TAG, "GoogleApiClient connected!");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        if (resolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (connectionResult.hasResolution()) {
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                googleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
            showErrorDialog(connectionResult.getErrorCode());
            resolvingError = true;
        }
    }

    /* Creates a dialog for googleApiClient error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        resolvingError = false;
    }

    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((RouteActivity)getActivity()).onDialogDismissed();
        }
    }

}
