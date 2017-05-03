/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.bluetoothchat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ViewAnimator;
import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;
import com.orbotix.DualStackDiscoveryAgent;
import com.orbotix.Sphero;
import com.orbotix.classic.DiscoveryAgentClassic;
import com.orbotix.classic.RobotClassic;
import com.orbotix.common.DiscoveryAgentEventListener;
import com.orbotix.common.DiscoveryException;
import com.orbotix.common.Robot;
import com.orbotix.le.DiscoveryAgentLE;
import com.orbotix.le.RobotLE;
import com.orbotix.common.RobotChangedStateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase implements DiscoveryAgentEventListener, RobotChangedStateListener{
    public Button button;
    public int value;
    public Button center;
    public Sphero mRobot;
    public DualStackDiscoveryAgent agent;
    public static final int REQUEST_CODE_LOCATION_PERMISSION = 42;
    public static final float ROBOT_VELOCITY = 0.3f;
    public static final String TAG = "cassie'sapp";
    public Timer timer = new Timer();

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        value = 0;

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            int hasLocationPermission = checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION );
            if( hasLocationPermission != PackageManager.PERMISSION_GRANTED ) {
                Log.e( "appCassie", "Location permission has not already been granted" );
                List<String> permissions = new ArrayList<String>();
                permissions.add( Manifest.permission.ACCESS_COARSE_LOCATION);
                requestPermissions(permissions.toArray(new String[permissions.size()] ), REQUEST_CODE_LOCATION_PERMISSION );
            } else {
                Log.d( "appCassie", "Location permission already granted" );
            }
        }
        if(checkSelfPermission( Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED)
        {
            android.util.Log.i("appCassie", "Still not granted");
        }
        else
        {
            android.util.Log.i("appCassie", "Location Permission Granted");
        }
        button = (Button) findViewById(R.id.button);
        center = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (BluetoothChatFragment.direction) {
                    case 0:
                        //goLeft();
                        break;
                    case 1:
                        goForward();
                        break;
                    case 2:
                        goBack();
                        break;
                    case 3:
                        goRight();
                        break;
                    case 4:
                        goLeft();
                        break;
                    default:
                        //// STOPSHIP: 4/11/2017
                        break;
                }
                mRobot.setBackLedBrightness(0);
            }
        });

        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //rotate the ball
                mRobot.setBackLedBrightness(225);
                mRobot.drive(90,(float)0.1);
                mRobot.setZeroHeading();

                if (value >=360)
                {
                    value = 0;
                }

            }

        });


        agent = DualStackDiscoveryAgent.getInstance();
        agent.addRobotStateListener(this);
    }

    protected void onStart()
    {
        Log.i(TAG, "In onStart()");
        super.onStart();
//        try
//        {
        startDiscovery();

       /* }catch (DiscoveryException e)
        {
            Log.i("appCassie", "Error occurred in onStart(): " + e.getMessage());
        }*/
    }

    @Override
    protected void onStop()
    {
        android.util.Log.i(TAG, "Stopping connection");
        if(mRobot != null)
        {
            mRobot.disconnect();
        }
        super.onStop();
    }

    @Override
    public void handleRobotsAvailable(List<Robot> robots) {
        Log.i(TAG, "Handling robots available");
        agent.connect(robots.get(0));
    }

    private void startDiscovery()
    {
        Log.i(TAG, "Starting discovery");
        try
        {
            agent.addDiscoveryListener(this);
            Log.i(TAG, "Added Discovery Listener");
            agent.addRobotStateListener(this);
            Log.i(TAG, "Added robot state Listener");
            agent.startDiscovery(this);
            Log.i(TAG, "completed discovery?");
        }catch(DiscoveryException e)
        {
            Log.i(TAG, "Error in startDiscovery(): " + e.getMessage());
        }
    }

    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType type) {
        Log.i("appCassie", "In HandleRobotChangedState Normal");
        switch(type)
        {
            case Online:
                agent.stopDiscovery();
                if(robot instanceof RobotClassic)
                {
                    mRobot = new Sphero(robot);
                }
                else
                {
                    Log.i("App", "Robot found, just not sphero");
                    finish();
                }
                mRobot.setLed(0f, 1f, 0f);
                mRobot.setBackLedBrightness(225);
                break;

            case Disconnected:
                //do stuff
                break;
            default:
                Log.i("AppCassie", "Weird type received: " + type);

        }

    }

    public void goForward()
    {
        Log.i(TAG, "Going forward...");
        mRobot.drive( 0.0f, ROBOT_VELOCITY );

    }

    public void goBack()
    {
        Log.i(TAG, "Going Back...");
        mRobot.drive( 180.0f, ROBOT_VELOCITY );

    }

    public void goLeft()
    {
        Log.i(TAG, "Going Left...");
        mRobot.drive( 270.0f, ROBOT_VELOCITY );

    }
    public void goRight()
    {
        Log.i(TAG, "Going Right...");
        mRobot.drive( 90.0f, ROBOT_VELOCITY );

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        mRobot.setBackLedBrightness(0);
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }


    public Sphero getmRobot()
    {
        return mRobot;
    }
}
