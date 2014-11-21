package com.heliocratic.imovies.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.heliocratic.imovies.model.LoaderResponse;
import com.heliocratic.imovies.subtitles.Subtitles;

import android.content.Context;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;

public class WatchLoader extends Loader<LoaderResponse> {

	public static final String TEMP_FOLDER_PATH_KEY = "popcorntime_video_torrent_tempfolderpath";
	public static final String TORRENT_URL_KEY = "popcorntime_video_torrent_url";
	public static final String SUBTITLE_URL_KEY = "popcorntime_video_subtitle_url";

	private Bundle data = null;
	private WatchTask task = null;
	private LoaderResponse response = null;

	public WatchLoader(Context context, Bundle data) {
		super(context);
		this.data = data;
	}

	@Override
	protected void onStartLoading() {
		super.onStartLoading();
		if (response != null) {
			deliverResult(response);
			response = null;
		}
	}

	@Override
	protected void onReset() {
		super.onReset();
		if (task != null && AsyncTask.Status.FINISHED != task.getStatus()) {
			task.cancel(true);
		}
	}

	@Override
	protected void onForceLoad() {
		super.onForceLoad();

		task = new WatchTask();
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, data.getString(TEMP_FOLDER_PATH_KEY), data.getString(TORRENT_URL_KEY),
				data.getString(SUBTITLE_URL_KEY));
	}

	private void setResponse(LoaderResponse response) {
		this.response = response;
	}

	private class WatchTask extends AsyncTask<String, Void, LoaderResponse> {

		@Override
		protected LoaderResponse doInBackground(String... params) {
			LoaderResponse response = new LoaderResponse();

			String tempFolderPath = params[0];
			String subtitleURl = params[2];

			byte[] buffer = new byte[1024];

			try {
				File tempFolder = new File(tempFolderPath + "/temp");
				if (!tempFolder.exists()) {
					tempFolder.mkdirs();
				}

				// load torrent file
				File torrentFile = new File(tempFolderPath + "/temp/metadata.torrent");

				URLConnection connectionTorrent = new URL(params[1]).openConnection();
				connectionTorrent.connect();

				InputStream is = connectionTorrent.getInputStream();
				OutputStream os = new FileOutputStream(torrentFile);

				int read;
				while ((read = is.read(buffer)) != -1) {
					os.write(buffer, 0, read);
				}
				os.flush();
				is.close();
				os.close();

				response.data = torrentFile.getPath();

				// load subtitle
				if (!TextUtils.isEmpty(subtitleURl)) {
					String subtitlePath = tempFolderPath + "/temp/subtitle.srt";
					Subtitles.load(subtitleURl, subtitlePath);
					response.data += LoaderResponse.DELIMETER + subtitlePath;
				}
			} catch (Exception ex) {
				response.error = ex.getMessage();
			}

			return response;
		}

		@Override
		protected void onPostExecute(LoaderResponse result) {
			if (isStarted()) {
				deliverResult(result);
			} else {
				setResponse(result);
			}
		}
	}
}
