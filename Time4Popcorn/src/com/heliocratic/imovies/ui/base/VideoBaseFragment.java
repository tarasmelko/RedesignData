package com.heliocratic.imovies.ui.base;

import java.util.ArrayList;
import java.util.List;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.heliocratic.imovies.R;
import com.heliocratic.imovies.controller.WatchLoader;
import com.heliocratic.imovies.database.tables.Favorites;
import com.heliocratic.imovies.model.LoaderResponse;
import com.heliocratic.imovies.model.videodata.VideoData;
import com.heliocratic.imovies.model.videoinfo.Torrent;
import com.heliocratic.imovies.model.videoinfo.VideoInfo;
import com.heliocratic.imovies.model.videoinfo.movie.MovieInfo;
import com.heliocratic.imovies.model.videoinfo.tvshow.TVShowInfo;
import com.heliocratic.imovies.subtitles.SubtitleCallbacks;
import com.heliocratic.imovies.subtitles.Subtitles;
import com.heliocratic.imovies.ui.VLCPlayerActivity;
import com.heliocratic.imovies.ui.VideoActivity;
import com.heliocratic.imovies.ui.locale.LocaleFragment;
import com.heliocratic.imovies.utils.StorageHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class VideoBaseFragment extends LocaleFragment implements
		LoaderCallbacks<LoaderResponse>, SubtitleCallbacks {

	public static final String RESPONSE_JSON_KEY = "popcorntime_response_json";

	private final int WATCH_LOADER_ID = 1001;
	private final int STARS_COUNT = 5;
	private final float MAX_RATING = 10;
	protected final float RATING_COEF = STARS_COUNT / MAX_RATING;

	protected PopcornBaseActivity mActivity;
	private DisplayImageOptions imageOptions;

	protected View prepare;
	protected ImageView poster;
	protected ToggleButton favorites;
	protected TextView title;
	protected RatingBar rating;
	protected TextView description;
	protected Spinner subtitleSpinner;
	protected Spinner torrentSpinner;
	protected Button watchItNow;
	private Animation prepareAnim;

	protected boolean isFavorites;
	protected Subtitles mSubtitles;
	protected String torrentUrl;
	protected String fileName;
	private VideoInfo videoInfo;
	private int torrentPos = 0;
	private String metadataLoadingErrorMsg;
	private ArrayAdapter<String> mSubtitleAdapter;
	private ArrayAdapter<String> mTorrentAdapter;

	protected abstract void updateTorrentInfo(int position);

	protected abstract void onFavoritesChecked(boolean isChecked);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (PopcornBaseActivity) getActivity();
		videoInfo = mActivity.getIntent().getExtras()
				.getParcelable(VideoActivity.VIDEO_INFO_KEY);
		imageOptions = new DisplayImageOptions.Builder().cacheInMemory(false)
				.cacheOnDisk(true).build();
		prepareAnim = AnimationUtils.loadAnimation(getActivity(),
				R.anim.popcorn_prepare);
		mSubtitleAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.item_spinner_video);
		mSubtitleAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTorrentAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.item_spinner_video);
		mTorrentAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = mActivity.setPopcornSplashView(R.layout.view_prepare);
		prepare = view.findViewById(R.id.video_prepare);
		Button close = (Button) view.findViewById(R.id.video_prepare_close);
		close.setOnClickListener(closeListener);
		return view;
	}

	protected void populateView(View view) {
		poster = (ImageView) view.findViewById(R.id.video_poster);
		ImageLoader.getInstance().displayImage(videoInfo.posterBigUrl, poster,
				imageOptions);
		title = (TextView) view.findViewById(R.id.video_title);
		title.setText(Html.fromHtml("<b>" + videoInfo.title + "</b>"));
		description = (TextView) view.findViewById(R.id.video_description);
		rating = (RatingBar) view.findViewById(R.id.video_rating);
		rating.setRating(videoInfo.rating * RATING_COEF);
		favorites = (ToggleButton) view.findViewById(R.id.video_favorites);
		favorites.setChecked(isFavorites);
		favorites.setOnCheckedChangeListener(favoritesListener);
		subtitleSpinner = (Spinner) view.findViewById(R.id.video_subtitles);
		torrentSpinner = (Spinner) view.findViewById(R.id.video_torrents);
		watchItNow = (Button) view.findViewById(R.id.video_watchitnow);
		watchItNow.setOnClickListener(watchItNowListener);

		initSubtitleSpinner();
		initTorrentSpinner();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		metadataLoadingErrorMsg = getResources().getString(
				R.string.error_metadata);
		subtitleSpinner.setPromptId(R.string.subtitles);
		torrentSpinner.setPromptId(R.string.torrents);
		watchItNow.setText(R.string.watch_it_now);
		replaceSubtitleData(mSubtitles.data);
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case Subtitles.LOADER_ID:
			return mSubtitles.onCreateLoader(id, args);
		case WATCH_LOADER_ID:
			return new WatchLoader(getActivity(), args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader,
			LoaderResponse response) {
		switch (loader.getId()) {
		case Subtitles.LOADER_ID:
			mSubtitles.onLoadFinished(loader, response);
			break;
		case WATCH_LOADER_ID:
			loadWatchFinished(response);
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	@Override
	public void onSubtitleLoadSucces() {
		if (isAdded()) {
			replaceSubtitleData(mSubtitles.data);
		}
	}

	@Override
	public void onSubtitleLoadError(String message) {

	}

	public void onBackPressed() {
		if (mActivity.isPopcornSplashVisible()) {
			breakPrepare();
		} else {
			getActivity().finish();
		}
	}

	protected void checkIsFavorites(VideoInfo info) {
		Cursor cursor = mActivity.getContentResolver().query(
				Favorites.CONTENT_URI, null,
				Favorites._IMDB + "=\"" + info.imdb + "\"", null, null);
		if (cursor != null && cursor.getCount() > 0) {
			isFavorites = true;
			Favorites.update(mActivity, info);
		} else {
			isFavorites = false;
		}
		if (cursor != null)
			cursor.close();
	}

	protected void replaceSubtitleData(List<String> subtitleSpinnerData) {
		if (subtitleSpinnerData != null && subtitleSpinnerData.size() > 0) {
			subtitleSpinnerData.set(0,
					getResources().getString(R.string.without_subtitle));

			mSubtitleAdapter.clear();
			mSubtitleAdapter.addAll(subtitleSpinnerData);

			subtitleSpinner.setVisibility(View.VISIBLE);
			subtitleSpinner.setSelection(mSubtitles.position, false);
		}
	}

	protected void replaceTorrentData(ArrayList<Torrent> torrents) {
		if (torrents.size() > 0) {
			List<String> torrentSpinnerData = new ArrayList<String>();
			for (int i = 0; i < torrents.size(); i++) {
				Torrent torrent = torrents.get(i);
				torrentSpinnerData.add(torrent.quality + ", "
						+ getResources().getString(R.string.size) + ": "
						+ StorageHelper.getSizeText(torrent.size) + ", "
						+ getResources().getString(R.string.seeds) + ": "
						+ torrent.seeds + ", "
						+ getResources().getString(R.string.peers) + ": "
						+ torrent.peers);
			}

			mTorrentAdapter.clear();
			mTorrentAdapter.addAll(torrentSpinnerData);

			torrentSpinner.setVisibility(View.VISIBLE);
			torrentSpinner.setSelection(torrentPos, false);
			watchItNow.setVisibility(View.VISIBLE);
		} else {
			torrentSpinner.setVisibility(View.GONE);
			watchItNow.setVisibility(View.GONE);
		}
	}

	private void initSubtitleSpinner() {
		subtitleSpinner.setAdapter(mSubtitleAdapter);
		subtitleSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mSubtitles.position = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void initTorrentSpinner() {
		torrentSpinner.setAdapter(mTorrentAdapter);
		torrentSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				torrentPos = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private void loadWatchFinished(LoaderResponse response) {
		if (response.error != null) {
			Toast.makeText(getActivity(), metadataLoadingErrorMsg,
					Toast.LENGTH_SHORT).show();
		} else {
			if (response.data != null) {
				String[] _data = response.data.split(LoaderResponse.DELIMETER);
				if (_data.length == 1) {
					startWatch(_data[0], fileName, null);
				} else if (_data.length == 2) {
					startWatch(_data[0], fileName, _data[1]);
				}
			}
		}

		prepare.clearAnimation();
		mActivity.setPopcornSplashVisible(false);
	}

	private void startWatch(String torrentFilePath, String videoFileName,
			String subFilePath) {
		Intent intent = new Intent(getActivity(), VLCPlayerActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("file://" + torrentFilePath));

		if (VideoData.Type.MOVIES.equals(videoInfo.getType())) {
			intent.putExtra(PlayerBaseActivity.VIDEO_INFO_EXTARA_KEY,
					(MovieInfo) videoInfo);
		} else if (VideoData.Type.TV_SHOWS.equals(videoInfo.getType())) {
			intent.putExtra(PlayerBaseActivity.VIDEO_INFO_EXTARA_KEY,
					(TVShowInfo) videoInfo);
		}

		if (videoFileName != null && !"".equals(videoFileName)) {
			intent.putExtra(PlayerBaseActivity.FILENAME_EXTARA_KEY,
					videoFileName);
		}

		if (subFilePath != null && !"".equals(subFilePath)) {
			intent.putExtra(PlayerBaseActivity.SUBTITLE_FILE_PATH_EXTARA_KEY,
					subFilePath);
		}

		if (mSubtitles.data != null && mSubtitles.urls != null) {
			intent.putExtra(PlayerBaseActivity.SUBTITLE_POSITION_EXTARA_KEY,
					mSubtitles.position);
			intent.putStringArrayListExtra(
					PlayerBaseActivity.SUBTITLE_DATA_EXTARA_KEY,
					mSubtitles.data);
			intent.putStringArrayListExtra(
					PlayerBaseActivity.SUBTITLE_URLS_EXTARA_KEY,
					mSubtitles.urls);
		}

		startActivity(intent);
	}

	private void breakPrepare() {
		getLoaderManager().destroyLoader(WATCH_LOADER_ID);
		prepare.clearAnimation();
		mActivity.setPopcornSplashVisible(false);
	}

	/*
	 * Listeners
	 */

	private OnClickListener closeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			breakPrepare();
		}
	};

	private OnClickListener watchItNowListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			updateTorrentInfo(torrentPos);

			if (!TextUtils.isEmpty(torrentUrl)) {
				if (torrentUrl.contains("http://yts.re")) {
					Log.e("WAS", torrentUrl);
					torrentUrl = torrentUrl.substring(13,
							torrentUrl.length());
					torrentUrl = "http://yts.to" + torrentUrl;
					Log.e("BECOME", torrentUrl);

				}

				prepare.startAnimation(prepareAnim);
				mActivity.setPopcornSplashVisible(true);

				Bundle data = new Bundle();
				data.putString(WatchLoader.TEMP_FOLDER_PATH_KEY, StorageHelper
						.getInstance().getChacheDirectoryPath());
				data.putString(WatchLoader.TORRENT_URL_KEY, torrentUrl);
				data.putString(WatchLoader.SUBTITLE_URL_KEY,
						mSubtitles.getUrl());
				getLoaderManager().restartLoader(WATCH_LOADER_ID, data,
						VideoBaseFragment.this).forceLoad();
			}
		}
	};

	private OnCheckedChangeListener favoritesListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			onFavoritesChecked(isChecked);
			isFavorites = isChecked;
		}

	};
}