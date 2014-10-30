package dp.ws.popcorntime;

import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.videolan.vlc.VLCApplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import dp.ws.popcorntime.subtitles.Subtitles;
import dp.ws.popcorntime.utils.LanguageUtil;
import dp.ws.popcorntime.utils.StorageHelper;

@ReportsCrashes(formKey = "", mode = ReportingInteractionMode.SILENT, mailTo = "support.popcorn@yandex.ru")
public class PopcornApplication extends VLCApplication {

	public static final String LOG_TAG = "tag";
	public static final String POPCORN_PREFERENCES = "PopcornPreferences";

	private final String IS_SHORTCUT_CREATED = "is-shortcut-created";
	private final String APP_LOCALE = "app-locale";
	private static PopcornApplication mApp;
	private SharedPreferences mPrefs;
	private Locale mLocale;

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(PopcornApplication.this);
		mApp = this;

		mPrefs = getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES,
				Activity.MODE_PRIVATE);

		initSubtitleLanguage();
		initLocale();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				getApplicationContext()).build();
		ImageLoader.getInstance().init(config);

		// initImageLoader(this);

		StorageHelper.getInstance().init(PopcornApplication.this);

		addShortcut();
	}

	public static PopcornApplication instance() {
		return mApp;
	}

	public static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				context).threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs().build();
		ImageLoader.getInstance().init(config);
	}

	public Locale getAppLocale() {
		return mLocale;
	}

	public void changeLanguage(String lang) {
		if (mLocale.getLanguage().equals(lang)) {
			return;
		}

		mLocale = new Locale(lang);
		mPrefs.edit().putString(APP_LOCALE, lang).commit();
	}

	public void setSubtitleLanguage(String lang) {
		mPrefs.edit().putString(Subtitles.LANGUAGE, lang).commit();
	}

	public String getSubtitleLanguage() {
		return mPrefs.getString(Subtitles.LANGUAGE, "");
	}

	private void addShortcut() {
		if (!mPrefs.getBoolean(IS_SHORTCUT_CREATED, false)) {
			Intent shortcutIntent = new Intent(getApplicationContext(),
					dp.ws.popcorntime.ui.SplashActivity.class);
			shortcutIntent.setAction(Intent.ACTION_MAIN);

			Intent addIntent = new Intent();
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources()
					.getString(R.string.app_name));
			addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
					Intent.ShortcutIconResource.fromContext(
							getApplicationContext(), R.drawable.ic_launcher));
			addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

			getApplicationContext().sendBroadcast(addIntent);
			mPrefs.edit().putBoolean(IS_SHORTCUT_CREATED, true).commit();
		}
	}

	private void initLocale() {
		String lang = LanguageUtil.getInterfaceSupportedIso(Locale.getDefault()
				.getLanguage());
		if (mPrefs.contains(APP_LOCALE)) {
			lang = mPrefs.getString(APP_LOCALE, lang);
		} else {
			mPrefs.edit().putString(APP_LOCALE, lang).commit();
		}
		mLocale = new Locale(lang);
	}

	private void initSubtitleLanguage() {
		if (!mPrefs.contains(Subtitles.LANGUAGE)) {
			setSubtitleLanguage(LanguageUtil.isoToLanguage(Locale.getDefault()
					.getLanguage()));
		}
	}
}