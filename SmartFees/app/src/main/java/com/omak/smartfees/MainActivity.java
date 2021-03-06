package com.omak.smartfees;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.omak.smartfees.Global.Constants;
import com.omak.smartfees.Global.Utils;
import com.splunk.mint.Mint;

import java.util.Calendar;

public class MainActivity extends Activity {

    /**
     * Page display time
     */
    private final int SPLASH_DISPLAY_LENGTH = 1500;

    /**
     * Flag to handle the handler
     */
    private boolean mHandlerFlag = true;

    private Handler handler = new Handler();
    private Runnable mrun = new Runnable() {

        @Override
        public void run() {

            if (mHandlerFlag) {
                // Finish the current Activity and start HomePage Activity
                Intent home;
                if(Utils.getBooleanSharedPreference(MainActivity.this, Constants.SHARED_PREF_IS_LOGGED_IN)) {
                    home = new Intent(MainActivity.this, MainHomeActivity.class);
                } else {
                    home = new Intent(MainActivity.this, LoginActivity.class);
                }
                startActivity(home);
                MainActivity.this.finish();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // Activity was brought to front and not created,
            // Thus finishing this will get us to the last viewed activity
            finish();
            return;
        }
        /**
         * Run the code inside the runnable after the display time is finished
         * using a handler
         */
        Mint.initAndStartSession(MainActivity.this, "b5fd4e68");
        handler.postDelayed(mrun, 2 * SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public void onBackPressed() {
        /**
         * Disable the handler to execute when user presses back when in
         * SplashPage Activity
         */
        mHandlerFlag = false;
        super.onBackPressed();
    }
}
