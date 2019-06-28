/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.growmat.android.simulant;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class TechEditActivity extends Activity implements View.OnClickListener {

    /**
     * Tag for Log
     */
    private static final String TAG = "TechEditActivity";

    private Button mButtonGetIX0, mButtonGetIX1, mButtonSetX0, mButtonSetX1, mButtonSave, mButtonBack;
    private EditText mEditTextX0, mEditTextX1, mEditTextY0, mEditTextY1, mEditTextUnit, mEditTextName;
    private TextView mTextViewUnit, mTextViewValue, mTextViewI, mTextViewF, mTextViewY0, mTextViewY1;

    public double valueInS, valueInI, valueInF;
    double k, d;
    public float parX0, parX1, parY0, parY1;

    public void hideSoftInput(View view) {
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void updateValueInI() {
        valueInS = DataHolder.getInstance().getData(1);
        valueInI = DataHolder.getInstance().getData(1);
        valueInF = DataHolder.getInstance().getData(1);

        double y = k * valueInI + d;
        if(mTextViewValue != null) {
            mTextViewValue.setText(String.format("%.2f", y));

            mTextViewI.setText(String.format("%.2f", valueInI));
            mTextViewI.setText(String.format("%.2f", valueInI));
        }
    }

    private void updateUI() {
        SharedPreferences mPrefs = this.getSharedPreferences(Constants.PREFS_NAME, 0);
        parX0 = mPrefs.getFloat(Constants.PREFS_X0_0, (float)0.0);
        parX1 = mPrefs.getFloat(Constants.PREFS_X1_0, (float)0.0);
        parY0 = mPrefs.getFloat(Constants.PREFS_Y0_0, (float)0.0);
        parY1 = mPrefs.getFloat(Constants.PREFS_Y1_0, (float)0.0);

        mEditTextX0.setText(String.format("%.2f", parX0));
        mEditTextX1.setText(String.format("%.2f",parX1));
        mEditTextY0.setText(String.format("%.2f",parY0));
        mEditTextY1.setText(String.format("%.2f",parY1));
        mEditTextUnit.setText(mPrefs.getString(Constants.PREFS_UNIT_0, ""));
        mTextViewUnit.setText(mPrefs.getString(Constants.PREFS_UNIT_0, ""));
        mEditTextName.setText(mPrefs.getString(Constants.PREFS_NAME_0, ""));

        k = (parY1 - parY0) / (parX1 - parX0);
        d = parY1 - k * parX1;

        mTextViewY0.setText(String.format("%.2f", k * 4.0 + d));
        mTextViewY1.setText(String.format("%.2f", k * 20.0 + d));

        updateValueInI();
        //Bundle bundle = getIntent().getExtras();
        //valueInI = bundle.getDouble("valueInI");

        //double y = k * valueInI + d;
        //mTextViewValue.setText(String.format("%.2f",y));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tech_edit);

        mButtonGetIX0 = (Button) findViewById(R.id.buttonGetIX0);
        mButtonGetIX1 = (Button) findViewById(R.id.buttonGetIX1);
        mButtonSetX0 = (Button) findViewById(R.id.buttonSetX0);
        mButtonSetX1 = (Button) findViewById(R.id.buttonSetX1);
        mButtonSave = (Button) findViewById(R.id.buttonSave);
        mButtonBack = (Button) findViewById(R.id.buttonBack);

        mEditTextX0 = (EditText) findViewById(R.id.editTextX0);
        mEditTextX1 = (EditText) findViewById(R.id.editTextX1);
        mEditTextY0 = (EditText) findViewById(R.id.editTextY0);
        mEditTextY1 = (EditText) findViewById(R.id.editTextY1);
        mEditTextUnit = (EditText) findViewById(R.id.editTextUnit);
        mEditTextName = (EditText) findViewById(R.id.editTextName);

        mTextViewUnit = (TextView)findViewById(R.id.textViewUnit);
        mTextViewValue = (TextView)findViewById(R.id.textViewValue);

        mTextViewI = (TextView)findViewById(R.id.textViewI);
        mTextViewF = (TextView)findViewById(R.id.textViewF);

        mTextViewY0 = (TextView)findViewById(R.id.textViewY0);
        mTextViewY1 = (TextView)findViewById(R.id.textViewY1);

        mButtonGetIX0.setOnClickListener(this);
        mButtonGetIX1.setOnClickListener(this);
        mButtonSetX0.setOnClickListener(this);
        mButtonSetX1.setOnClickListener(this);
        mButtonSave.setOnClickListener(this);
        mButtonBack.setOnClickListener(this);

        updateUI();
    }

    @Override
    public void onClick(View view) {
        hideSoftInput(view);

        SharedPreferences mPrefs = this.getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor mEditor = mPrefs.edit();

        switch (view.getId()) {
            case R.id.buttonGetIX0:
                parX0 = (float)valueInI;
                mEditor.putFloat(Constants.PREFS_X0_0, parX0).commit();
                break;
            case R.id.buttonGetIX1:
                parX1 = (float)valueInI;
                mEditor.putFloat(Constants.PREFS_X1_0, parX1).commit();
                break;

            case R.id.buttonSetX0:
                parX0 = 4.0f;
                mEditor.putFloat(Constants.PREFS_X0_0, parX0).commit();
                break;
            case R.id.buttonSetX1:
                parX1 = 20.0f;
                mEditor.putFloat(Constants.PREFS_X1_0, parX1).commit();
                break;

            case R.id.buttonSave:
                mEditor.putFloat(Constants.PREFS_X0_0, Float.parseFloat(mEditTextX0.getText().toString()));//.commit();
                mEditor.putFloat(Constants.PREFS_X1_0, Float.parseFloat(mEditTextX1.getText().toString()));
                mEditor.putFloat(Constants.PREFS_Y0_0, Float.parseFloat(mEditTextY0.getText().toString()));
                mEditor.putFloat(Constants.PREFS_Y1_0, Float.parseFloat(mEditTextY1.getText().toString()));
                mEditor.putString(Constants.PREFS_UNIT_0, mEditTextUnit.getText().toString());
                mEditor.putString(Constants.PREFS_NAME_0, mEditTextName.getText().toString());
                mEditor.commit();

                break;

            case R.id.buttonBack:
                setResult(Activity.RESULT_OK);
                finish();
        }
        updateUI();
    }

}
