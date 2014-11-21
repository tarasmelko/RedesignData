package com.heliocratic.imovies.subtitles;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.universalchardet.UniversalDetector;

import com.heliocratic.imovies.IMoviesApplication;
import com.heliocratic.imovies.controller.URLLoader;
import com.heliocratic.imovies.model.LoaderResponse;
import com.heliocratic.imovies.model.videodata.VideoData;
import com.heliocratic.imovies.model.videoinfo.VideoInfo;
import com.heliocratic.imovies.model.videoinfo.tvshow.TVShowInfo;
import com.heliocratic.imovies.utils.LanguageUtil;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Subtitles {

	public static final String LANGUAGE = "subtitle-language";
	public static final String FONT_SIZE_PREF = "subtitle-font-size";

	public static class FontSize {

		public static final float EXTRA_SMALL = 0.7f;
		public static final float SMALL = 0.85f;
		public static final float NORMAL = 1f;
		public static final float LARGE = 1.25f;
		public static final float EXTRA_LARGE = 1.5f;

		public static final int DEFAULT_POSITION = 2;
		public static final float[] SIZES = new float[] { EXTRA_SMALL, SMALL, NORMAL, LARGE, EXTRA_LARGE };
	}

	public static final String FORMAT_SRT = "srt";
	public static final String FORMAT_VTT = "vtt";

	public static final int LOADER_ID = 1602;

	private static final String UTF_8 = "UTF-8";
	private final String MOVIE_URL = "http://api.yifysubtitles.com/subs/";
	private final String MOVIE_DOWNLOAD_URL = "http://www.yifysubtitles.com";
	private final String TVSHOW_URL = "http://sub.torrentsapi.com/list?imdb=";

	public int position = 0;
	public ArrayList<String> data;
	public ArrayList<String> urls;
	private Activity activity;
	private SubtitleCallbacks subtitleCallbacks;
	private VideoInfo info;

	public Subtitles(Activity activity, SubtitleCallbacks subtitleCallbacks, VideoInfo info) {
		this.activity = activity;
		this.subtitleCallbacks = subtitleCallbacks;
		this.info = info;
	}

	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		return new URLLoader(activity, args);
	}

	public void onLoadFinished(Loader<LoaderResponse> loader, LoaderResponse response) {
		loaderHandler.sendMessage(loaderHandler.obtainMessage(LOADER_ID, response));
	}

	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	public void restartLoader(LoaderCallbacks<LoaderResponse> loaderCallbacks) {
		urls = null;
		data = null;
		position = 0;
		String url = "";
		if (VideoData.Type.MOVIES.equals(info.getType())) {
			url = MOVIE_URL + info.imdb;
		} else if (VideoData.Type.TV_SHOWS.equals(info.getType())) {
			TVShowInfo tvShowInfo = (TVShowInfo) info;
			url = TVSHOW_URL + info.imdb + "&s=" + (tvShowInfo.seasonPosition + 1) + "&e=" + tvShowInfo.episodePosition;
		}
		if (!"".equals(url)) {
			Bundle data = new Bundle();
			data.putString(URLLoader.URL_KEY, url);
			activity.getLoaderManager().restartLoader(LOADER_ID, data, loaderCallbacks).forceLoad();
		}
	}

	public String getUrl() {
		if (urls != null && urls.size() > position) {
			return urls.get(position);
		}

		return null;
	}

	private Handler loaderHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			try {
				loaderFinished((LoaderResponse) msg.obj);
			} catch (JSONException e) {
				urls = null;
				data = null;
				position = 0;
				subtitleCallbacks.onSubtitleLoadError(e.getMessage());
				e.printStackTrace();
			}
			removeMessages(LOADER_ID);
		}
	};

	private void loaderFinished(LoaderResponse response) throws JSONException {
		if (response.error == null) {
			JSONObject json = new JSONObject(response.data);
			if (VideoData.Type.MOVIES.equals(info.getType())) {
				parseMovies(json);
			} else if (VideoData.Type.TV_SHOWS.equals(info.getType())) {
				parseTVShows(json);
			}
			subtitleCallbacks.onSubtitleLoadSucces();
		} else {
			subtitleCallbacks.onSubtitleLoadError(response.error);
		}
	}

	private void parseMovies(JSONObject jsonSubtitles) throws JSONException {
		int subtitlesCount = jsonSubtitles.getInt("subtitles");
		if (subtitlesCount > 0) {
			urls = new ArrayList<String>();
			data = new ArrayList<String>();
			urls.add(null);
			data.add("none");
			JSONObject subs = jsonSubtitles.getJSONObject("subs").getJSONObject(info.imdb);
			@SuppressWarnings("unchecked")
			Iterator<String> iter = subs.keys();
			String subLang = ((IMoviesApplication) activity.getApplication()).getSubtitleLanguage();
			while (iter.hasNext()) {
				String key = iter.next();
				JSONArray subInfos = subs.getJSONArray(key);
				int subRating = Integer.MIN_VALUE;
				String subUrl = "";
				for (int i = 0; i < subInfos.length(); i++) {
					JSONObject subInfo = subInfos.getJSONObject(i);
					int rating = subInfo.getInt("rating");
					if (rating > subRating) {
						subRating = rating;
						subUrl = subInfo.getString("url");
					}
				}
				if (!"".equals(subUrl)) {
					urls.add(MOVIE_DOWNLOAD_URL + subUrl);
					data.add(LanguageUtil.languageToNativeLanguage(key));
					if (subLang.equals(key)) {
						position = data.size() - 1;
					}
				}
			}
		}
	}

	private void parseTVShows(JSONObject jsonSubtitles) throws JSONException {
		int subtitlesCount = jsonSubtitles.getInt("subtitles");
		if (subtitlesCount > 0) {
			urls = new ArrayList<String>();
			data = new ArrayList<String>();
			urls.add(null);
			data.add("none");
			JSONObject subs = jsonSubtitles.getJSONObject("subs");
			@SuppressWarnings("unchecked")
			Iterator<String> iter = subs.keys();
			String subLang = ((IMoviesApplication) activity.getApplication()).getSubtitleLanguage();
			while (iter.hasNext()) {
				String key = iter.next();
				JSONArray subInfos = subs.getJSONArray(key);
				int subRating = Integer.MIN_VALUE;
				String subUrl = "";
				for (int i = 0; i < subInfos.length(); i++) {
					JSONObject subInfo = subInfos.getJSONObject(i);
					int rating = subInfo.getInt("rating");
					String format = subInfo.getString("format");
					if (rating > subRating && FORMAT_SRT.equals(format)) {
						subRating = rating;
						subUrl = subInfo.getString("url");
					}
				}
				if (!"".equals(subUrl)) {
					urls.add(subUrl);
					key = LanguageUtil.isoToLanguage(key);
					data.add(LanguageUtil.languageToNativeLanguage(key));
					if (subLang.equals(key)) {
						position = data.size() - 1;
					}
				}
			}
		}
	}

	public static float getFontScale(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(IMoviesApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		int pos = prefs.getInt(FONT_SIZE_PREF, FontSize.DEFAULT_POSITION);
		if (pos < FontSize.SIZES.length) {
			return FontSize.SIZES[pos];
		} else {
			return FontSize.SIZES[FontSize.DEFAULT_POSITION];
		}
	}

	public static void load(String url, String savePath) throws Exception {
		File saveFile = new File(savePath);
		if (saveFile.exists()) {
			saveFile.delete();
		}
		URLConnection connectionSubtitle = new URL(url).openConnection();
		connectionSubtitle.connect();
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(connectionSubtitle.getInputStream()));

		ZipEntry zi = zis.getNextEntry();
		while (zi != null) {
			String[] part = zi.getName().split("\\.");
			if (part.length == 0) {
				zi = zis.getNextEntry();
				continue;
			}

			String extension = part[part.length - 1];
			if (FORMAT_SRT.equals(extension)) {
				UniversalDetector detector = new UniversalDetector(null);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				int count = 0;
				byte[] buffer = new byte[1024];
				while ((count = zis.read(buffer)) > 0) {
					if (!detector.isDone()) {
						detector.handleData(buffer, 0, count);
					}
					baos.write(buffer, 0, count);
				}
				detector.dataEnd();

				String subtitleEncoding = detector.getDetectedCharset();
				detector.reset();
				Log.d("tag", "detector: " + subtitleEncoding);
				if (subtitleEncoding == null || "".equals(subtitleEncoding)) {
					subtitleEncoding = UTF_8;
				} else if ("MACCYRILLIC".equals(subtitleEncoding)) {
					subtitleEncoding = "Windows-1256"; // for arabic
				}

				String subtitle = new String(baos.toByteArray(), Charset.forName(subtitleEncoding));
				FileUtils.write(saveFile, subtitle, Charset.forName(UTF_8));
				break;
			} else {
				Log.w("tag", "Sub loaded extension: " + extension);
			}
			zi = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}
}
