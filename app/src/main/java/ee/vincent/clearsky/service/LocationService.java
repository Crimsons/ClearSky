package ee.vincent.clearsky.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderApi;

import ee.vincent.clearsky.Constants;
import ee.vincent.clearsky.R;
import ee.vincent.clearsky.activity.MainActivity;

/**
 * Created by jakob on 2.01.2015.
 */
public class LocationService extends Service {


    private static final String TAG = "LocationService";
    private static final String KEY_LOCATION = "location_parcelable";

    public static boolean isRunning = false;
    private ServiceHandler mServiceHandler;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {

            Location location = msg.getData().getParcelable(KEY_LOCATION);

            if ( location != null ) {

                // TODO store location to db
                // TODO send location with localbroadcast to map activity
                Log.e(TAG, "got location: " + location.toString());
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
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_text))
                .setSmallIcon(R.drawable.ic_launcher)
                //.setLargeIcon(aBitmap)
                .setContentIntent(pendingIntent)
                .getNotification();
        startForeground(Constants.LOC_SERVICE_NOTIF_ID, notification);

        // TODO add backstack to notification intent

        isRunning = true;
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
        isRunning = false;
        super.onDestroy();
    }


}
