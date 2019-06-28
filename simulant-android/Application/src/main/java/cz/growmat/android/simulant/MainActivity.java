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


package cz.growmat.android.simulant;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;

import static java.security.AccessController.getContext;



/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase
        implements View.OnClickListener {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    // Layout Views
    //private ListView mConversationView;
    private SeekBar mSeekBarSetpoint;
    private EditText mEditTextSetpoint;
    private Button mButtonSetSetpoint, mButtonSetpointPlus, mButtonSetpointMinus, mButtonSetpointPlus2, mButtonSetpointMinus2, mButtonExit, mButtonTechEdit;
    private TextView mTextViewI, mTextViewF, mTextViewValue, mTextViewUnit, mTextViewName;

    private double setpointOutPWM = 0.0;
    public double valueInS, valueInI, valueInF, valueInBatt;
    String mConnectedDeviceName = "";

    public float parX0, parX1, parY0, parY1;
    double k, d;

    //TechEditActivity mTechActivity;

    BluetoothCommFragment fragment;

    public void showNotification(String title, String message, int defaults) {
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Notification mNotification = new Notification(R.drawable.ic_launcher, "Notification", System.currentTimeMillis());

        //mNotification.flags |= Notification.FLAG_NO_CLEAR;
        //mNotification.flags = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        mNotification.defaults |= defaults;

        //Intent notificationIntent = new Intent(this, BluetoothSerialBridgeService.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(MainActivity.this);

        builder.setAutoCancel(false);
        //builder.setTicker("this is ticker text");
        builder.setContentTitle("SIMULANT - OPEN APP");
        //builder.setContentText("OPEN APP");
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        //builder.setSubText("This is subtext...");   //API level 16
        //builder.setNumber(100);
        builder.build();

        mNotification = builder.getNotification();

        //mNotification.setLatestEventInfo(this, title, message, pendingIntent);
        mNotificationManager.notify(1, mNotification);
    }


    @Override
    public void onClick(View view)
    {
        hideSoftInput(view);
        switch (view.getId()) {
            case R.id.buttonSetpointPlus:
                setpointOutPWM += 1.0;
                break;
            case R.id.buttonSetpointMinus:
                setpointOutPWM -= 1.0;
                break;
            case R.id.buttonSetpointPlus2:
                setpointOutPWM += 10.0;
                break;
            case R.id.buttonSetpointMinus2:
                setpointOutPWM -= 10.0;
                break;
            case R.id.buttonSetSetpoint:
                try {
                    setpointOutPWM = Double.parseDouble(mEditTextSetpoint.getText().toString());
                }
                catch(Exception e) {
                    Log.e(TAG, e.toString());
                }
                //InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                break;
            case R.id.buttonExit:
                NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                notificationManager.cancelAll();
                finish();
                System.exit(0);

            case R.id.buttonTechEdit:
                DataHolder.getInstance().setData(0, valueInS);
                DataHolder.getInstance().setData(1, valueInI);
                DataHolder.getInstance().setData(2, valueInF);

                Intent intent = new Intent(this, TechEditActivity.class);
                //intent.putExtra("valueInI", valueInI);
                startActivityForResult(intent, 10);
                break;
        }




        /*
        if(view == mButtonSetpointPlus)
           setpoint += 1.0;
        if(view == mButtonSetpointMinus)
            setpoint -= 1.0;
        */

        //String sSetpoint = new Double(setpoint).toString();
        //editTextSetpoint.setText(sSetpoint);
        //seekBarSetpoint.setProgress((int) setpoint);

        updateSetpoint();
    }

    // This is the callback for the started sub-activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10 && resultCode == Activity.RESULT_OK) {
            //Bundle extras = data.getExtras();
            //if (extras != null) {
            //    String name = extras.getString(User.USER_NAME);
            updateUI();
        }
    }


    public void hideSoftInput(View view) {
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private int updateSetpoint() {
        setpointOutPWM = Math.max(0.0, setpointOutPWM);
        //setpoint = Math.min(255.0, setpoint);

        String sS = "S";
        String sSetpoint = new Double(setpointOutPWM).toString();
        mEditTextSetpoint.setText(String.format("%.0f", setpointOutPWM));
        mSeekBarSetpoint.setProgress((int) setpointOutPWM);

        fragment.sendMessage(sS + sSetpoint);
        return 0;
    }

    private void updateUI() {
        SharedPreferences mPrefs = this.getSharedPreferences(Constants.PREFS_NAME, 0);
        parX0 = mPrefs.getFloat(Constants.PREFS_X0_0, (float)0.0);
        parX1 = mPrefs.getFloat(Constants.PREFS_X1_0, (float)0.0);
        parY0 = mPrefs.getFloat(Constants.PREFS_Y0_0, (float)0.0);
        parY1 = mPrefs.getFloat(Constants.PREFS_Y1_0, (float)0.0);
        mTextViewName.setText(mPrefs.getString(Constants.PREFS_NAME_0, ""));
        mTextViewUnit.setText(mPrefs.getString(Constants.PREFS_UNIT_0, ""));

        k = (parY1 - parY0) / (parX1 - parX0);
        d = parY1 - k * parX1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showNotification("SIMULANT", "OPEN APP", 0);

        mSeekBarSetpoint = (SeekBar) findViewById(R.id.seekBarSetpoint);
        mEditTextSetpoint = (EditText) findViewById(R.id.editTextSetpoint);
        mButtonSetpointPlus = (Button) findViewById(R.id.buttonSetpointPlus);
        mButtonSetpointMinus = (Button) findViewById(R.id.buttonSetpointMinus);
        mButtonSetpointPlus2 = (Button) findViewById(R.id.buttonSetpointPlus2);
        mButtonSetpointMinus2 = (Button) findViewById(R.id.buttonSetpointMinus2);
        mButtonSetSetpoint = (Button) findViewById(R.id.buttonSetSetpoint);
        mButtonExit = (Button) findViewById(R.id.buttonExit);
        mButtonTechEdit = (Button) findViewById(R.id.buttonTechEdit);

        mTextViewI = (TextView) findViewById(R.id.textViewI);
        mTextViewF = (TextView) findViewById(R.id.textViewF);

        mTextViewName = (TextView) findViewById(R.id.textViewTechName);
        mTextViewValue = (TextView) findViewById(R.id.textViewTechValue);
        mTextViewUnit = (TextView) findViewById(R.id.textViewTechUnit);

        mSeekBarSetpoint.setMax(255);
        mSeekBarSetpoint.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               if(fromUser) {
                   setpointOutPWM = progress;
               }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
               updateSetpoint();
            }
        });

        mButtonSetSetpoint.setOnClickListener(this);
        mButtonSetpointPlus.setOnClickListener(this);
        mButtonSetpointMinus.setOnClickListener(this);
        mButtonSetpointPlus2.setOnClickListener(this);
        mButtonSetpointMinus2.setOnClickListener(this);
        mButtonExit.setOnClickListener(this);
        mButtonTechEdit.setOnClickListener(this);

        updateUI();


        /*
        mEditTextSetpoint.setOnFocusChangeListener(new EditText.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus){
                //if(v.getId() == R.id.textbox && !hasFocus) {
                Log.i(TAG, Boolean.toString(hasFocus));
                if(!hasFocus) {
                    InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });
        */

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //BluetoothChatFragment fragment = new BluetoothChatFragment();
            fragment = new BluetoothCommFragment();
            fragment.mHandler = mHandler;
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }
    }

    @Override
    public void onDestroy() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        super.onDestroy();
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
                    //output.setDisplayedChild(1);
                    //TextView logTextView = findViewById(R.id.log_text_view);
                    //logTextView.setText("");
                    //LogWrapper logWrapper = new LogWrapper();

                    ((TextView)mLogFragment.getLogView()).setText("");
                    //((TextView)mLogFragment.getLogView()).setVisibility(View.VISIBLE);
                } else {
                    //output.setDisplayedChild(0);
                    //((TextView)mLogFragment.getLogView()).setVisibility(View.INVISIBLE);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    LogFragment mLogFragment;

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
        //LogFragment logFragment = (LogFragment) getSupportFragmentManager()
        //      .findFragmentById(R.id.log_fragment);
        //msgFilter.setNext(logFragment.getLogView());

        mLogFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(mLogFragment.getLogView());

        Log.i(TAG, "Ready");
    }

    private void setStatus(int resId) {
        /*
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        */
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    private void setStatus(CharSequence subTitle) {
        /*
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        */
        final ActionBar actionBar = getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //FragmentActivity activity = getActivity();
            FragmentActivity activity = MainActivity.this;
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothCommService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            //mConversationArrayAdapter.clear();
                            break;
                        case BluetoothCommService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothCommService.STATE_LISTEN:
                        case BluetoothCommService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //mConversationArrayAdapter.add(">: " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ": " + readMessage);

                    int pos;
                    pos = readMessage.indexOf('I');
                    if(pos >= 0) {
                        String s = readMessage.substring(pos + 1);
                        //Log.i(TAG, s);

                        try {
                            valueInI = Double.parseDouble(s);
                            //mTextViewI.setText(String.format("%.2f", valueInS * 0.01955));
                            mTextViewI.setText(String.format("%.2f", valueInI));
                            mTextViewValue.setText(String.format("%.2f", valueInI * k + d));

                            DataHolder.getInstance().setData(0, valueInS);
                            DataHolder.getInstance().setData(1, valueInI);
                            DataHolder.getInstance().setData(2, valueInF);
                            //if(mTechActivity != null) {
                                //mTechActivity.valueInI = valueInI;
                            //   mTechActivity.updateValueInI(valueInI);
                            //}
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    pos = readMessage.indexOf('F');
                    if(pos >= 0) {
                        String s = readMessage.substring(pos + 1);
                        //Log.i(TAG, s);

                        try {
                            valueInF = Double.parseDouble(s);
                            mTextViewF.setText(String.format("%.2f", valueInF));
                        } catch (Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "CONNECTED TO "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
}
