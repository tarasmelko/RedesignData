package dp.ws.popcorntime.googlecast;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class SubtitleSender {

	private GoogleApiClient mApiClient;
	private PopcornChannel mPopcornChannel;
	private int part;
	private ArrayList<JSONArray> data;

	public void launchReceiver(GoogleApiClient apiClient) {
		mApiClient = apiClient;
	}

	public void attachChannel() {
		mPopcornChannel = new PopcornChannel();
		try {
			Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mPopcornChannel.getNamespace(), mPopcornChannel);
		} catch (IOException e) {
			Log.e("tag", "Exception while creating channel", e);
		}
	}

	public void send(ArrayList<JSONArray> data) {
		this.data = data;
		part = 0;
		send(data.get(part));
	}

	private void send(JSONArray subtitles) {
		if (mApiClient != null && mPopcornChannel != null) {
			try {
				JSONObject msg = new JSONObject();
				msg.put("part", (part + 1));
				msg.put("data", subtitles);
				Cast.CastApi.sendMessage(mApiClient, mPopcornChannel.getNamespace(), msg.toString()).setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status result) {
						if (result.isSuccess()) {
							part++;
							if (part < data.size()) {
								send(data.get(part));
							}
						} else {
							Log.e("tag", "Sending message failed: " + result.getStatusCode());
						}
					}
				});
			} catch (Exception e) {
				Log.e("tag", "Exception while sending message", e);
			}
		}
	}
}
