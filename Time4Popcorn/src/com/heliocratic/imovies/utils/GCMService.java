package com.heliocratic.imovies.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.android.gms.internal.ex;
import com.heliocratic.imovies.R;
import com.heliocratic.imovies.ui.MainActivity;

/**
 * Created by taras.melko on 9/15/14.
 */
public class GCMService extends IntentService {

	String mes = "test";
	String title = "test";
	String icon;
	String language;
	private Handler handler;
	public Bitmap imageData;

	public GCMService() {
		super("GCMService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		handler = new Handler();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		dumpIntent(intent);
		title = extras.getString("title");
		mes = extras.getString("message");
		if (extras.getString("icon") != null)
			icon = extras.getString("icon");

		getBitmap();
		GCMReceiver.completeWakefulIntent(intent);
	}

	private void getBitmap() {
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				imageData = getBitmapFromURL(icon);
				return null;
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				showToast();
			};

		}.execute();
	}

	public void showToast() {
		handler.post(new Runnable() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			public void run() {
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				PendingIntent pIntent = PendingIntent.getActivity(
						getApplicationContext(), 0, intent, 0);

				if (imageData == null) {
					imageData = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_launcher);
				}

				Notification n = new Notification.Builder(
						getApplicationContext())
						.setContentTitle(title)
						.setContentText(mes)
						.setContentIntent(pIntent)
						.setAutoCancel(true)
						.setSmallIcon(R.drawable.ic_launcher)
						.setStyle(
								new Notification.BigPictureStyle()
										.bigPicture(imageData))
						.setSound(
								Uri.parse("android.resource://"
										+ getApplicationContext()
												.getPackageName() + "/"
										+ R.raw.push)).build();
				notificationManager.notify(0, n);
			}
		});

	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("ERROR", e.toString() + "");
			return null;
		}
	}

	public static void dumpIntent(Intent i) {

		Bundle bundle = i.getExtras();
		if (bundle != null) {
			Set<String> keys = bundle.keySet();
			Iterator<String> it = keys.iterator();
			Log.e("Dump", "Dumping Intent start");
			while (it.hasNext()) {
				String key = it.next();
				Log.e("Dump", "[" + key + "=" + bundle.get(key) + "]");
			}
			Log.e("Dump", "Dumping Intent end");
		}
	}
}
