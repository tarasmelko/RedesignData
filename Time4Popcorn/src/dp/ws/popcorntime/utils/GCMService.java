package dp.ws.popcorntime.utils;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.ui.MainActivity;

/**
 * Created by taras.melko on 9/15/14.
 */
public class GCMService extends IntentService {

	String mes;
	String title;
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
		icon = extras.getString("icon");
		language = extras.getString("language");
		Log.e("DATA", language);
		switch (Integer.parseInt(language)) {
		case 1:
			if (Preference.getUSA())
				getBitmap();
			break;
		case 2:
			if (Preference.getFrance())
				getBitmap();
			break;
		case 3:
			if (Preference.getSpain())
				getBitmap();
			break;
		case 4:
			if (Preference.getIndia())
				getBitmap();
			break;
		case 5:
			if (Preference.getItaly())
				getBitmap();
			break;
		case 6:
			if (Preference.getGermany())
				getBitmap();
			break;
		case 7:
			if (Preference.getChina())
				getBitmap();
			break;
		}
		GCMReceiver.completeWakefulIntent(intent);
	}

	private void getBitmap() {
		new AsyncTask<String, String, String>() {

			@Override
			protected String doInBackground(String... params) {
				imageData = getBitmapFromURL(Constants.ICON_PREFIX + icon);
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
						.setLargeIcon(imageData)
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
