package dp.ws.popcorntime.ui;

import java.util.ArrayList;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.controller.GenreAdapter;
import dp.ws.popcorntime.controller.URLLoader;
import dp.ws.popcorntime.model.LoaderResponse;
import dp.ws.popcorntime.model.videodata.MovieData;
import dp.ws.popcorntime.model.videodata.TVShowData;
import dp.ws.popcorntime.model.videodata.VideoData;
import dp.ws.popcorntime.model.videoinfo.VideoInfo;
import dp.ws.popcorntime.ui.base.PopcornLoadActivity;
import dp.ws.popcorntime.ui.locale.LocalePopupMenu;
import dp.ws.popcorntime.ui.widget.BlockTouchFrameLayout;
import dp.ws.popcorntime.utils.JSONHelper;
import dp.ws.popcorntime.utils.Preference;

public class MainActivity extends PopcornLoadActivity implements
		LoaderCallbacks<LoaderResponse>, OnClickListener {

	private final int EXIT_DELAY_TIME = 2000;

	private final int VIDEO_LIST_LOADER_ID = 1001;

	private VideoData currentVideoData = null;
	private VideoData moviesData = null;
	private VideoData tvShowsData = null;

	private DrawerLayout mDrawerLayout;
	private BlockTouchFrameLayout mContentFrame;
	private RelativeLayout mDrawer;
	private Button moviesDrawerBtn;
	private Button tvShowsDrawerBtn;
	private EditText searchView;
	private ListView mGenreList;
	private GenreAdapter mGenreAdapter;

	private GridVideoFragment videoFragment;
	private GridFavoritesFragment favoritesFragment = new GridFavoritesFragment();

	private boolean doubleBackToExitPressedOnce = false;
	private String exitMsg;

	GoogleCloudMessaging gcm;
	String regid;
	private static final int MILLIS_IN_SECOND = 1000;
	private static final int SECONDS_IN_MINUTE = 60;
	private static final int MINUTES_IN_HOUR = 60;
	private static final int HOURS_IN_DAY = 24;
	private static final long MILLISECONDS_IN_THREE_DAYS = (long) MILLIS_IN_SECOND
			* SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY * 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);

		super.onCreate(savedInstanceState);
		moviesData = new MovieData(MainActivity.this);
		tvShowsData = new TVShowData(MainActivity.this);

		// Splash
		setPopcornSplashView(R.layout.view_splash);

		// Header
		View header = setPopcornHeaderView(R.layout.header_main);
		header.findViewById(R.id.popcorn_action_menu).setOnClickListener(
				MainActivity.this);
		header.findViewById(R.id.popcorn_action_overflow).setOnClickListener(
				MainActivity.this);

		// Content
		View content = setPopcornContentView(R.layout.activity_main);
		setPopcornContentViewId(R.id.main_content_frame);

		mDrawerLayout = (DrawerLayout) content
				.findViewById(R.id.main_drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerLayout.setFocusableInTouchMode(false);
		mDrawerLayout.setDrawerListener(drawerListener);
		mContentFrame = (BlockTouchFrameLayout) content
				.findViewById(R.id.main_content_frame);
		mDrawer = (RelativeLayout) content.findViewById(R.id.main_drawer);

		// video type switch
		moviesDrawerBtn = (Button) mDrawer
				.findViewById(R.id.main_drawer_movies_btn);
		moviesDrawerBtn.setOnClickListener(MainActivity.this);
		tvShowsDrawerBtn = (Button) mDrawer
				.findViewById(R.id.main_drawer_tvshows_btn);
		tvShowsDrawerBtn.setOnClickListener(MainActivity.this);

		mGenreAdapter = new GenreAdapter(MainActivity.this);
		mGenreList = (ListView) mDrawer.findViewById(R.id.main_drawer_list);
		mGenreList.setAdapter(mGenreAdapter);
		mGenreList.setOnItemClickListener(genreListener);

		searchView = (EditText) mDrawer.findViewById(R.id.main_drawer_search);
		searchView.setOnEditorActionListener(searchListener);
		updateSearchCursor(getResources().getConfiguration());

		updateLocaleText();

		firstStart();

		replaceFragment(new MoviesFragment());

		if (Preference.getFTime() == 0) {
			Preference.saveFTime(System.currentTimeMillis());
			Preference.saveUserPaypal(true);
		} else {
			Log.e("time not null",
					Preference.getFTime() + ", : " + System.currentTimeMillis());
			if ((System.currentTimeMillis() - Preference.getFTime()) > MILLISECONDS_IN_THREE_DAYS)
				Preference.saveUserPaypal(false);
		}
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		moviesDrawerBtn.setText(R.string.movies);
		tvShowsDrawerBtn.setText(R.string.tv_shows);
		searchView.setHint(R.string.search);
		moviesData.setLocaleGenres(MainActivity.this);
		tvShowsData.setLocaleGenres(MainActivity.this);
		mGenreAdapter.notifyDataSetInvalidated();
		exitMsg = getString(R.string.exit_msg);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		updateSearchCursor(newConfig);
	}

	private void updateSearchCursor(Configuration config) {
		if (Configuration.ORIENTATION_LANDSCAPE == config.orientation) {
			searchView.setCursorVisible(false);
		} else if (Configuration.ORIENTATION_PORTRAIT == config.orientation) {
			searchView.setCursorVisible(true);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void showContent() {
		replaceFragment(videoFragment);
	}

	@Override
	public void retryLoad() {
		restartVideosLoader();
	}

	private void restartVideosLoader() {
		Bundle data = new Bundle();
		data.putString(URLLoader.URL_KEY, currentVideoData.getRequestURl());
		data.putString(URLLoader.INFO_KEY, currentVideoData.getType());
		getLoaderManager().restartLoader(VIDEO_LIST_LOADER_ID, data, this)
				.forceLoad();
	}

	@Override
	public Loader<LoaderResponse> onCreateLoader(int id, Bundle args) {
		showLoading();

		switch (id) {
		case VIDEO_LIST_LOADER_ID:
			return new URLLoader(MainActivity.this, args);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<LoaderResponse> loader,
			LoaderResponse response) {

		switch (loader.getId()) {
		case VIDEO_LIST_LOADER_ID:
			loaderHandler.sendMessage(loaderHandler.obtainMessage(
					VIDEO_LIST_LOADER_ID, response));
			break;
		default:
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<LoaderResponse> loader) {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.main_drawer_movies_btn:
			onMoviesClick();
			break;
		case R.id.main_drawer_tvshows_btn:
			onTVShowsClick();
			break;
		case R.id.popcorn_action_menu:
			onMenuClick();
			break;
		case R.id.popcorn_action_overflow:
			if (mDrawerLayout.isDrawerOpen(mDrawer)) {
				mDrawerLayout.closeDrawer(mDrawer);
			}
			onOverflowPressed(v);
			break;
		default:
			break;
		}
	}

	private void firstStart() {
		if (currentVideoData != null) {
			currentVideoData.setPage(1);
		}
		searchView.setText(moviesData.getKeywords());
		mGenreAdapter.replaceData(moviesData);
		mGenreList.setItemChecked(moviesData.getGenrePosition(), true);
		mGenreList.clearFocus();
		mGenreList.post(new Runnable() {

			@Override
			public void run() {
				mGenreList.setSelection(moviesData.getGenrePosition());
			}
		});
		searchView.clearFocus();
	}

	private void onMoviesClick() {
		if (currentVideoData != moviesData) {
			moviesBtnSelect();
			tvShowsBtnUnselect();
			selectVideoData(moviesData);
		}
	}

	private void onTVShowsClick() {
		if (currentVideoData != tvShowsData) {
			moviesBtnUnselect();
			tvShowsBtnSelect();
			selectVideoData(tvShowsData);
		}
	}

	private void moviesBtnSelect() {
		moviesDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_selected_selector);
		moviesDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchSelected);
		moviesDrawerBtn.setShadowLayer(1, 1, 1,
				getResources().getColor(R.color.classic_text_shadow));
	}

	private void moviesBtnUnselect() {
		moviesDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_unselected_selector);
		moviesDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchUnselected);
		moviesDrawerBtn.setShadowLayer(0, 0, 0,
				getResources().getColor(android.R.color.transparent));
	}

	private void tvShowsBtnSelect() {
		tvShowsDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_selected_selector);
		tvShowsDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchSelected);
		tvShowsDrawerBtn.setShadowLayer(1, 1, 1,
				getResources().getColor(R.color.classic_text_shadow));
	}

	private void tvShowsBtnUnselect() {
		tvShowsDrawerBtn
				.setBackgroundResource(R.drawable.drawer_switch_unselected_selector);
		tvShowsDrawerBtn.setTextAppearance(MainActivity.this,
				R.style.DrawerSwitchUnselected);
		tvShowsDrawerBtn.setShadowLayer(0, 0, 0,
				getResources().getColor(android.R.color.transparent));
	}

	private void onMenuClick() {
		if (mDrawerLayout.isDrawerOpen(mDrawer)) {
			mDrawerLayout.closeDrawer(mDrawer);

		} else {
			mDrawerLayout.openDrawer(mDrawer);
			if (currentVideoData != null) {
				mGenreList.setSelection(currentVideoData.getGenrePosition());
			}
		}
	}

	private void onOverflowPressed(View v) {
		LocalePopupMenu popup = new LocalePopupMenu(MainActivity.this, v);
		popup.setOnMenuItemClickListener(overflowMenuListener);
		popup.inflate(R.menu.popup_main);
		popup.show();
	}

	@Override
	public void onBackPressed() {
		if (mDrawerLayout.isDrawerOpen(mDrawer)) {
			mDrawerLayout.closeDrawer(mDrawer);
			return;
		}

		if (doubleBackToExitPressedOnce) {
			finish();
		} else {
			doubleBackToExitPressedOnce = true;
			Toast.makeText(this, exitMsg, Toast.LENGTH_SHORT).show();
		}

		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				doubleBackToExitPressedOnce = false;
			}
		}, EXIT_DELAY_TIME);
	}

	private void selectVideoData(VideoData videoData) {
		if (currentVideoData != null) {
			currentVideoData.setPage(1);
		}
		currentVideoData = videoData;
		searchView.setText(currentVideoData.getKeywords());
		mGenreAdapter.replaceData(currentVideoData);
		mGenreList.setItemChecked(currentVideoData.getGenrePosition(), true);
		mGenreList.clearFocus();
		mGenreList.post(new Runnable() {

			@Override
			public void run() {
				mGenreList.setSelection(currentVideoData.getGenrePosition());
			}
		});
		searchView.clearFocus();
		restartVideosLoader();
	}

	public void selecteGenre(int position) {
		if (currentVideoData == null) {
			moviesData.setGenre(position);
			moviesData.setKeywords("");
			moviesBtnSelect();
			selectVideoData(moviesData);
		} else if (position == 0) {
			replaceFragment(new MoviesFragment());
		} else {
			searchView.setText("");
			currentVideoData.setPage(1);
			currentVideoData.setGenre(position);
			currentVideoData.setKeywords("");
			mGenreAdapter.notifyDataSetInvalidated();
			searchView.clearFocus();
			restartVideosLoader();
		}
	}

	private Handler loaderHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case VIDEO_LIST_LOADER_ID:
				videoListFinished((LoaderResponse) msg.obj);
				break;
			default:
				break;
			}
		}
	};

	private void videoListFinished(LoaderResponse response) {
		if (response.error != null) {
			showError();
		} else {
			ArrayList<VideoInfo> data = null;
			try {
				if (VideoData.Type.MOVIES.equals(response.info)) {
					data = JSONHelper.parseMovies(response.data);
				} else if (VideoData.Type.TV_SHOWS.equals(response.info)) {
					data = JSONHelper.parseTVShows(response.data);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (data != null) {
				Bundle args = new Bundle();
				args.putParcelableArrayList(
						GridVideoFragment.VIDEO_INFO_LIST_KEY, data);
				videoFragment = new GridVideoFragment();
				videoFragment.setArguments(args);
				videoFragment.setVideoData(currentVideoData);
				showContent();
			} else {
				showError();
			}
		}
	}

	/*
	 * Listeners
	 */

	private OnMenuItemClickListener overflowMenuListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.popup_favorites:
				if (!favoritesFragment.isAdded()) {
					getLoaderManager().destroyLoader(VIDEO_LIST_LOADER_ID);
					if (mDrawerLayout.isDrawerOpen(mDrawer)) {
						mDrawerLayout.closeDrawer(mDrawer);
					}
					if (moviesData == currentVideoData) {
						moviesBtnUnselect();
					} else if (tvShowsData == currentVideoData) {
						tvShowsBtnUnselect();
					}
					mGenreList.setItemChecked(
							currentVideoData.getGenrePosition(), false);
					mGenreAdapter.inactive();
					currentVideoData.setPage(1);
					currentVideoData = null;
					replaceFragment(favoritesFragment);
				}
				return true;
			case R.id.popup_settings:
				startActivity(new Intent(MainActivity.this,
						SettingsActivity.class));
				return true;
			default:
				return false;
			}
		}
	};

	private DrawerListener drawerListener = new DrawerListener() {

		@Override
		public void onDrawerStateChanged(int arg0) {

		}

		@Override
		public void onDrawerSlide(View arg0, float arg1) {

		}

		@Override
		public void onDrawerOpened(View arg0) {
			mContentFrame.setBlockTouchEvent(true);
		}

		@Override
		public void onDrawerClosed(View arg0) {
			mContentFrame.setBlockTouchEvent(false);
		}
	};

	private OnItemClickListener genreListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selecteGenre(position);
		}
	};

	private OnEditorActionListener searchListener = new OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				String keywords = v.getText().toString();
				if (currentVideoData != null) {
					currentVideoData.setPage(1);
					currentVideoData.setKeywords(keywords);
					restartVideosLoader();
				}
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				v.clearFocus();

				return true;
			}

			return false;
		}
	};
}