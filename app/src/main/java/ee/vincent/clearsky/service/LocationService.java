package ee.vincent.clearsky.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

/**
 * Created by jakob on 2.01.2015.
 */
public class LocationService extends IntentService {


    private static final String TAG = "LocationService";



    public LocationService() {
        super("LocationService");
    }

    public LocationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.e(TAG, "got intent: " + intent.toString());

        Location location =
                intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);

        if ( location != null ) {

            // TODO store location to db
            // TODO send location with localbroadcast to map activity
            Log.e(TAG, "got location: " + location.toString());

        }

    }


}
