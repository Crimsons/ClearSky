package ee.vincent.clearsky.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

import ee.vincent.clearsky.Conf;
import ee.vincent.clearsky.Constants;
import ee.vincent.clearsky.R;
import ee.vincent.clearsky.activity.RouteActivity;
import ee.vincent.clearsky.database.Datasource;
import ee.vincent.clearsky.model.Point;

/**
 * Created by jakob on 2.01.2015.
 */
public class LocationService extends Service {


    private static final String TAG = "LocationService";
    private static final String KEY_LOCATION = "location_parcelable";

    private static boolean running = false;
    private static long routeId;

    private ServiceHandler mServiceHandler;
    private Location lastLocation;


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            Location location = msg.getData().getParcelable(KEY_LOCATION);

            if ( location != null ) {

                Log.e(TAG, "got location: " + location.toString());

                if ( lastLocation == null
                        || Math.round(lastLocation.distanceTo(location)) > Conf.MIN_DISTANCE_TO_FIX ) {

                    // store location in db
                    Point point = new Point();
                    point.setRouteId(routeId);
                    point.setTime(location.getTime());
                    point.setLatitude(location.getLatitude());
                    point.setLongitude(location.getLongitude());
                    point.setAltitude(location.getAltitude());
                    point.setSpeed(location.getSpeed());
                    Datasource.getInstance(LocationService.this).insertPoint(point);

                    // send location with localbroadcast to listening activities
                    Intent localIntent = new Intent(Constants.Action.BROADCAST)
                            .putExtra(Constants.Extra.LOCATION, location);
                    LocalBroadcastManager.getInstance(LocationService.this)
                            .sendBroadcast(localIntent);

                    lastLocation = location;
                } else {
                    Log.e(TAG, "discarded location. too near!");
                }

            }

        }
    }

    @Override
    public void onCreate() {

        Log.e(TAG, "onCreate service");

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceHandler = new ServiceHandler(thread.getLooper());

        // set as foreground service
        Intent resultIntent = new Intent(this, RouteActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RouteActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_stat_notif_pedestrian)
                .setLargeIcon(largeIcon)
                .setContentIntent(pendingIntent)
                .getNotification();
        startForeground(Constants.LOC_SERVICE_NOTIF_ID, notification);

        running = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");

        Location location =
                intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
        Bundle data = new Bundle();
        data.putParcelable(KEY_LOCATION, location);
        Message msg = mServiceHandler.obtainMessage();
        msg.setData(data);
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        running = false;
        super.onDestroy();
    }


    public static boolean isRunning() {
        return running;
    }

    public static void setRunning(boolean running) {
        LocationService.running = running;
    }

    public static long getRouteId() {
        return routeId;
    }

    public static void setRouteId(long routeId) {
        LocationService.routeId = routeId;
    }
}
