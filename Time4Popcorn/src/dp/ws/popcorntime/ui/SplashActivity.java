package dp.ws.popcorntime.ui;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.utils.Preference;

public class SplashActivity extends Activity {
	private Timer mSplashTime;
	String PROJECT_NUMBER = "1059048236175";
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
