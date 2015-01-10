package ee.vincent.clearsky.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import ee.vincent.clearsky.Conf;
import ee.vincent.clearsky.Constants;
import ee.vincent.clearsky.R;
import ee.vincent.clearsky.database.Datasource;
import ee.vincent.clearsky.model.Point;
import ee.vincent.clearsky.model.Route;

public class RouteActivity extends Activity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "RouteActivity";

    private GoogleApiClient googleApiClient;
    private GoogleMap map;
    private Marker startMarker;
    private Polyline route;

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
        setContentView(R.layout.activity_route);

        resolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        buildGoogleApiClient();

        // initialize map
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // setup local broadcast receiver that
        // receives location updates from service
        IntentFilter intentFilter = new IntentFilter(Constants.Action.BROADCAST);
        LocationReceiver locationReceiver = new LocationReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(locationReceiver, intentFilter);

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


    /**
     * Load route and objects on map.
     * Called when map is ready.
     */
    private void initUI() {

        long routeId = getIntent().getLongExtra(Constants.Extra.ROUTE_ID, -1);
        if ( routeId > -1 )
            new DataLoaderTask().execute(routeId);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        initUI();


    }

    private void addRouteToMap(Route route) {

        List<Point> points = route.getPoints();
        if (points.size() > 0) {

            // bounds used to fit route to the visible map area
            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

            // create route
            PolylineOptions rectOptions = new PolylineOptions()
                    .color(getResources().getColor(android.R.color.holo_red_light));

            for (int i = 0; i < points.size(); i++) {
                Point point = points.get(i);
                LatLng location = new LatLng(point.getLatitude(), point.getLongitude());

                boundsBuilder.include(location);

                // add point to route
                rectOptions.add(location);

                // add marker to map
                int markerDrawable;
                if ( i == 0 )
                    markerDrawable = R.drawable.ic_map_waypoint_start;
                else
                    markerDrawable = R.drawable.ic_map_waypoint;
                map.addMarker(new MarkerOptions()
                        .position(location)
                        .icon(BitmapDescriptorFactory.fromResource(markerDrawable)));
            }

            // add route to map
            map.addPolyline(rectOptions);

            // move camera to fit route
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLngBounds(boundsBuilder.build(), 32);
            map.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                }

                @Override
                public void onCancel() {

                }

            });

        }

    }

    private void initRouteWithStartPoint(LatLng location) {

        Log.e(TAG, "Initializing route!");

        // start marker
        startMarker = map.addMarker(new MarkerOptions()
                .position(location)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_waypoint_start)));

        // create polyline
        PolylineOptions rectOptions = new PolylineOptions()
                .color(getResources().getColor(android.R.color.holo_red_light));
        route = map.addPolyline(rectOptions);

        // move camera to start point
        CameraPosition cameraPosition = CameraPosition.builder()
                .zoom(Conf.INITIAL_ZOOM_LEVEL)
                .target(location)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory
                .newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                findViewById(R.id.waiting_for_fix).setVisibility(View.GONE);
            }

            @Override
            public void onCancel() {

            }

        });

    }

    private void addPointToMap(LatLng point) {

        // dismiss point if map is not ready
        if ( map == null )
            return;

        if ( route == null ) {
           initRouteWithStartPoint(point);
        } else {

            Log.e(TAG, "Adding point!");

            // add new point to polyline
            List<LatLng> points = route.getPoints();
            points.add(point);
            route.setPoints(points);

            // add point marker
            startMarker = map.addMarker(new MarkerOptions()
                    .position(point)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_waypoint)));

            // move camera to added point
            CameraUpdate cameraUpdate = CameraUpdateFactory
                    .newLatLng(point);
            map.animateCamera(cameraUpdate);
        }

    }


    private class DataLoaderTask extends AsyncTask<Long, Void, Route> {

        @Override
        protected Route doInBackground(Long... params) {

            long routeId = params[0];
            Route route = Datasource.getInstance(RouteActivity.this)
                    .getRoute(routeId);
            List<Point> points = Datasource.getInstance(RouteActivity.this)
                    .getRoutePoints(route.getId());
            route.setPoints(points);

            return route;
        }

        @Override
        protected void onPostExecute(Route route) {

            if ( route != null ) {
                addRouteToMap(route);
            }

        }
    }

    // Broadcast receiver for receiving location updates
    private class LocationReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private LocationReceiver() {}

        public void onReceive(Context context, Intent intent) {

            Location location = intent.getParcelableExtra(Constants.Extra.LOCATION);
            addPointToMap(new LatLng(location.getLatitude(), location.getLongitude()));

        }

    }


    // Google API stuff
    protected synchronized void buildGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {

        /*Location mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(googleApiClient);

        if (mLastLocation != null) {

            if ( map != null ) {

                LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                // add marker
                Marker melbourne = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker()));

                // goto location
                CameraPosition.Builder positionBuilder = new CameraPosition.Builder();
                positionBuilder.target(latLng);
                positionBuilder.zoom(10);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(positionBuilder.build());
                map.moveCamera(cameraUpdate);

                Log.e(TAG, "adding my location marker");

            } else {
                Log.e(TAG, "onConnected: map null");
            }

        } else {
            Log.e(TAG, "onConnected: lastLocation null");
        }*/

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
