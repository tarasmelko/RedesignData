package com.heliocratic.imovies.ui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.heliocratic.imovies.R;
import com.heliocratic.imovies.utils.Preference;

public class SplashActivity extends Activity {
	private Timer mSplashTime;
	String PROJECT_NUMBER = "67246118737";
	GoogleCloudMessaging gcm;
	String regid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.splash_activity);
		getRegId();

		mSplashTime = new Timer();

		TimerTask launchMainActivity = new TimerTask() {

			@Override
			public void run() {
				if (!Preference.getUserRegistered().equals("")) {
					Intent intent = new Intent(SplashActivity.this,
							MainActivity.class);
					startActivity(intent);
					finish();
				} else {
					Intent intent = new Intent(SplashActivity.this,
							LoginActivity.class);
					startActivity(intent);
					finish();
				}
			}
		};

		mSplashTime.schedule(launchMainActivity, 3000);

		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		
		if(tm.getDeviceId()!=null)
			Preference.saveIMEI(tm.getDeviceId());
		
		if(Preference.getImei().isEmpty()){
			Preference.saveIMEI(Secure.getString(getContentResolver(),
                    Secure.ANDROID_ID));
		}
		
		if(Preference.getImei().isEmpty()){
			Preference.saveIMEI(Build.SERIAL);
		}
		Log.e("IMEI", Preference.getImei());
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	public void getRegId() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging
								.getInstance(getApplicationContext());
					}
					if (gcm.register(PROJECT_NUMBER) != null)
						regid = gcm.register(PROJECT_NUMBER);
					msg = "Device registered, registration ID=" + regid;
					Log.e("GCM", msg);

				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();

				}
				return msg;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

			}

			@Override
			protected void onPostExecute(String msg) {
				Preference.saveUserRegistrationId(regid);
			}
		}.execute(null, null, null);
	}
}
