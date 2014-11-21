package com.heliocratic.imovies.ui.base;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.videolan.libvlc.LibVLC;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.MediaRouteButton;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.heliocratic.imovies.PopcornApplication;
import com.heliocratic.imovies.R;
import com.heliocratic.imovies.googlecast.CastPopcornListener;
import com.heliocratic.imovies.googlecast.GoogleCast;
import com.heliocratic.imovies.model.LoaderResponse;
import com.heliocratic.imovies.model.videoinfo.VideoInfo;
import com.heliocratic.imovies.subtitles.SubtitleCallbacks;
import com.heliocratic.imovies.subtitles.Subtitles;
import com.heliocratic.imovies.subtitles.format.VTT;
import com.heliocratic.imovies.torrent.PopcornTorrent;
import com.heliocratic.imovies.torrent.VideoResult;
import com.heliocratic.imovies.torrent.VideoTaskCallbacks;
import com.heliocratic.imovies.ui.locale.LocaleDialogFragment;
import com.heliocratic.imovies.ui.locale.LocaleFragmentActivity;
import com.heliocratic.imovies.utils.ExtGenericFilter;
import com.heliocratic.imovies.utils.StorageHelper;

public abstract class PlayerBaseActivity extends LocaleFragmentActivity implements VideoTaskCallbacks, CastPopcornListener, LoaderCallbacks<LoaderResponse>,
		SubtitleCallbacks {

	public static final String SETTINGS_HW_ACCELERATION = "hardware-acceleration";

	public static final String VIDEO_INFO_EXTARA_KEY = "popcorn_video_info";
	public static final String SUBTITLE_FILE_PATH_EXTARA_KEY = "popcorn_subtitle";
	public static final String SUBTITLE_POSITION_EXTARA_KEY = "popcorn_subtitle_position";
	public static final String FILENAME_EXTARA_KEY = "popcorn_filename";
	public static final String SUBTITLE_DATA_EXTARA_KEY = "popcorn_subtitle_data";
	public static final String SUBTITLE_URLS_EXTARA_KEY = "popcorn_subtitle_urls";

	protected LibVLC mLibVLC;
	protected String mLocation;
	protected SharedPreferences mPreferences;
	protected Button mCloseButton;
	private VideoInfo mVideoInfo;
	private String mVideoPath;

	// torrent
	protected PopcornTorrent mTorrent;
	protected boolean isTorrentVideoReady = false;
	private ProgressBar mTorrentProgressBar;
	private TextView mTorrentProgressText;

	// google cast
	private MediaRouteButton mRouteButton;
	protected GoogleCast mGoogleCast;
	protected boolean isCastEnabled = false;
	protected boolean isCastPlaying = false;

	// subtitle
	protected Subtitles mSubtitles;
	protected ImageButton mSubtitleButton;
	private String mSubtitlePath;
	private String mVTTSubtitlePath;
	private SubtitleDialog mSubtitleDialog;
	private SubtitleLoadTask mSubtitleLoadTask;

	private CastErrorDialog mCastErrorDialog;
	private ErrorDialog mErrorDialog;

	protected void initPopcorn() {
		mCloseButton = (Button) findViewById(R.id.player_overlay_close);
		mCloseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		mTorrent = PopcornTorrent.getInstance();
		mTorrent.setVideoTaskCallbacs(PlayerBaseActivity.this);
		mTorrent.onCreate(getApplicationContext());
		mTorrentProgressBar = (ProgressBar) findViewById(R.id.torrent_progress_bar);
		mTorrentProgressText = (TextView) findViewById(R.id.torrent_progress_text);

		mRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
		mGoogleCast = new GoogleCast(PlayerBaseActivity.this, PlayerBaseActivity.this);
		mGoogleCast.onCreate(mRouteButton);

		mSubtitleButton = (ImageButton) findViewById(R.id.player_overlay_subtitle);
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		mPreferences = getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		mVideoInfo = getIntent().getExtras().getParcelable(VIDEO_INFO_EXTARA_KEY);
		mSubtitles = new Subtitles(PlayerBaseActivity.this, PlayerBaseActivity.this, mVideoInfo);
		mSubtitles.position = getIntent().getIntExtra(SUBTITLE_POSITION_EXTARA_KEY, 0);
		if (getIntent().hasExtra(SUBTITLE_DATA_EXTARA_KEY)) {
			mSubtitles.data = getIntent().getStringArrayListExtra(SUBTITLE_DATA_EXTARA_KEY);
			mSubtitles.urls = getIntent().getStringArrayListExtra(SUBTITLE_URLS_EXTARA_KEY);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mGoogleCast != null) {
			mGoogleCast.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mGoogleCast != null) {
			mGoogleCast.onResume();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mLocaleHelper.updateLocale();
	}

	@Override
	protected void onDestroy() {
		if (mSubtitleLoadTask != null && AsyncTask.Status.FINISHED != mSubtitleLoadTask.getStatus()) {
			mSubtitleLoadTask.cancel(true);
		}
		if (mGoogleCast != null) {
			mGoogleCast.onDestroy();
		}
		mTorrent.onDestroy();
		super.onDestroy();
	}

	/*
	 * Torrent
	 */

	@Override
	public void onVideoPreExecute() {
		isTorrentVideoReady = false;
		mCloseButton.setVisibility(View.VISIBLE);
		mTorrentProgressBar.setVisibility(View.VISIBLE);
		mTorrentProgressBar.setProgress(0);
		mTorrentProgressText.setVisibility(View.VISIBLE);
		mTorrentProgressText.setText(getResources().getString(R.string.please_wait));
	}

	@Override
	public void onVideoPostExecute(VideoResult result) {
		if (VideoResult.SUCCESS == result) {
			isTorrentVideoReady = true;
			mCloseButton.setVisibility(View.INVISIBLE);
			mTorrentProgressBar.setVisibility(View.INVISIBLE);
			mTorrentProgressText.setVisibility(View.INVISIBLE);

			mLocation = mTorrent.getFileLocation();
			mVideoPath = mLocation.replace("file://", "");
			mVideoPath = mVideoPath.substring(0, mVideoPath.lastIndexOf("/"));

			String loc = mLocation.replace("file://", "");
			loc = loc.substring(0, loc.lastIndexOf(".") + 1);
			mSubtitlePath = loc + Subtitles.FORMAT_SRT;
			mVTTSubtitlePath = loc + Subtitles.FORMAT_VTT;

			// subtitle
			removeSubtitleFiles();
			if (getIntent().hasExtra(SUBTITLE_FILE_PATH_EXTARA_KEY)) {
				try {
					FileUtils.copyFile(new File(getIntent().getStringExtra(SUBTITLE_FILE_PATH_EXTARA_KEY)), new File(mSubtitlePath));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (mSubtitles.data != null) {
				initSubtitle();
			} else {
				restartSubtitlesLoader();
			}
		} else if (VideoResult.NO_FREE_SPACE == result) {
			showErrorDialog(getResources().getString(R.string.no_free_space));
			StorageHelper.getInstance().clearChacheDirectory();
		} else if (VideoResult.TORRENT_NOT_ADDED == result) {
			showErrorDialog("Something is wrong. Torrent not added.");
		} else if (VideoResult.NO_VIDEO_FILE == result) {
			showErrorDialog("Something is wrong. No file in torrent.");
		}
	}

	@Override
	public void onVideoProgressUpdate(int progress, String status) {
		mTorrentProgressBar.setProgress(progress);
		mTorrentProgressText.setText(status);
	}

	/*
	 * Google cast
	 */

	@Override
	public void onCastConnection() {
		if (new File(mSubtitlePath).exists()) {
			try {
				VTT.convert(mSubtitlePath, mVTTSubtitlePath);
			} catch (Exception e) {
				e.printStackTrace();
			}
			mGoogleCast.loadMovieMedia(mLocation.replace("file://", ""), mLibVLC.getTime(), mTorrent.getContentName(), mVTTSubtitlePath);
		} else {
			mGoogleCast.loadMovieMedia(mLocation.replace("file://", ""), mLibVLC.getTime(), mTorrent.getContentName(), null);
		}
	}

	@Override
	public void onCastRouteSelected() {
		if (mLibVLC.isPlaying()) {
			mLibVLC.pause();
		}
	}

	@Override
	public void onCastRouteUnselected(long position) {
		if (position > 0) {
			mLibVLC.setTime(position);
		}
	}

	@Override
	public void onCastStatePlaying() {
		isCastPlaying = true;
	}

	@Override
	public void onCastStatePaused() {
		isCastPlaying = false;
	}

	@Override
	public void onCastStateIdle() {

	}

	@Override
	public void onCastStateBuffering() {

	}

	@Override
	public void onCastMediaLoadSuccess() {
		isCastEnabled = true;
		if (mSubtitles.position > 0) {
			sendSubtitleToChromecast(true);
		}
	}

	@Override
	public void onCastMediaLoadCancelInterrupt() {
		isCastEnabled = false;
		if (mCastErrorDialog == null) {
			mCastErrorDialog = new CastErrorDialog();
		}
		if (!mCastErrorDialog.isAdded()) {
			mCastErrorDialog.show(getFragmentManager(), "cast_error");
		}
	}

	@Override
	public void teardown() {
		isCastEnabled = false;
		isCastPlaying = false;
	}

	/*
	 * Subtitle
	 */

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		if (Subtitles.LOADER_ID == id) {
			return mSubtitles.onCreateLoader(id, args);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader, LoaderResponse response) {
		if (Subtitles.LOADER_ID == loader.getId()) {
			mSubtitles.onLoadFinished(loader, response);
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> laoder) {

	}

	private void restartSubtitlesLoader() {
		mSubtitleButton.setVisibility(View.GONE);
		mSubtitles.restartLoader(PlayerBaseActivity.this);
	}

	@Override
	public void onSubtitleLoadSucces() {
		if (mSubtitles.data != null && mSubtitles.data.size() > 0) {
			mSubtitles.data.set(0, getResources().getString(R.string.without_subtitle));
			initSubtitle();
			downloadSubtitle();
		}
	}

	@Override
	public void onSubtitleLoadError(String message) {
		// reload
	}

	private void initSubtitle() {
		mSubtitleButton.setVisibility(View.VISIBLE);
		mSubtitleButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mSubtitleDialog == null) {
					mSubtitleDialog = new SubtitleDialog();
				}
				if (!mSubtitleDialog.isAdded()) {
					mSubtitleDialog.show(getFragmentManager(), "player_subtitle_dialog");
				}
			}
		});
	}

	private void downloadSubtitle() {
		if (mSubtitleLoadTask != null && AsyncTask.Status.FINISHED != mSubtitleLoadTask.getStatus()) {
			mSubtitleLoadTask.cancel(true);
		}
		mSubtitleLoadTask = new SubtitleLoadTask();
		mSubtitleLoadTask.execute(mSubtitles.getUrl());
	}

	protected void removeSubtitleFiles() {
		File path = new File(mVideoPath);
		StorageHelper.deleteRecursive(path, new ExtGenericFilter(Subtitles.FORMAT_SRT));
		StorageHelper.deleteRecursive(path, new ExtGenericFilter(Subtitles.FORMAT_VTT));
	}

	/*
	 * Other
	 */

	protected boolean isResume() {
		if (mErrorDialog != null && mErrorDialog.isAdded()) {
			return false;
		}

		return true;
	}

	private void sendSubtitleToChromecast(boolean enable) {
		try {
			// mGoogleCast.sendSubtitleJson(JSON.convert(mSubtitlePath));
			mGoogleCast.sendSubtitleVtt(enable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Dialogs
	 */

	private class CastErrorDialog extends LocaleDialogFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.googlecast_error_title);
			builder.setMessage(R.string.googlecast_error_message);

			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {

				}
			});

			return builder.create();
		}
	}

	private class ErrorDialog extends DialogFragment {

		private String msg;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setCancelable(false);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.app_name));
			builder.setPositiveButton(getResources().getString(R.string.ok), null);
			builder.setMessage(msg);
			AlertDialog dialog = builder.create();
			dialog.show();
			Button update = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			update.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getActivity().finish();
				}
			});

			return dialog;
		}

		public void setMessage(String msg) {
			this.msg = msg;
		}
	}

	private void showErrorDialog(String message) {
		if (mErrorDialog == null) {
			mErrorDialog = new ErrorDialog();
		}
		if (!mErrorDialog.isAdded()) {
			mErrorDialog.setMessage(message);
			mErrorDialog.show(getFragmentManager(), "video_error");
		}
	}

	private class SubtitleDialog extends LocaleDialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.subtitles));
			builder.setSingleChoiceItems(mSubtitles.data.toArray(new String[mSubtitles.data.size()]), mSubtitles.position,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mSubtitles.position = which;
							downloadSubtitle();
							dialog.dismiss();
						}
					});

			Dialog dialog = builder.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.setOwnerActivity(PlayerBaseActivity.this);
			return dialog;
		}
	}

	private class SubtitleLoadTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			if (params[0] == null) {
				removeSubtitleFiles();
				mLibVLC.setSpuTrack(-1);
				if (isCastEnabled) {
					sendSubtitleToChromecast(false);
				}
			} else {
				try {
					Subtitles.load(params[0], mSubtitlePath);
					mLibVLC.addSubtitleTrack(mSubtitlePath);
					if (isCastEnabled) {
						VTT.convert(mSubtitlePath, mVTTSubtitlePath);
						mGoogleCast.reloadMovie(mLocation.replace("file://", ""), mTorrent.getContentName(), mVTTSubtitlePath);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;
		}

	}
}
