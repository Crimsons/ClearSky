package ee.vincent.clearsky.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import ee.vincent.clearsky.Conf;
import ee.vincent.clearsky.R;
import ee.vincent.clearsky.service.LocationService;

/**
 * Created by jakob on 3.01.2015.
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                continueToApp();
            }
        }, Conf.SPLASH_DURATION);
    }

    private void continueToApp() {

        Intent intent;
        if (LocationService.isRunning() ) {
            intent = new Intent(this, RouteActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);

        // we do not want to come back to splashactivity
        // when user presses back key in mainactivity
        finish();
    }


}
