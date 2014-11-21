package com.heliocratic.imovies.ui.locale;

import java.util.Locale;

import com.heliocratic.imovies.PopcornApplication;

import android.content.res.Configuration;

public class LocaleHelper {

	private PopcornApplication mApplication;
	private LocaleListener mListener;
	private Locale mLocale;

	public LocaleHelper(PopcornApplication application, LocaleListener localeListener) {
		mApplication = application;
		mListener = localeListener;
		updateLocale();
	}

	public void checkLanguage() {
		if (!mLocale.getLanguage().equals(mApplication.getAppLocale().getLanguage())) {
			updateLocale();
			mListener.updateLocaleText();
		}
	}

	public void updateLocale() {
		mLocale = new Locale(mApplication.getAppLocale().getLanguage());
		Locale.setDefault(mLocale);
		Configuration config = mApplication.getResources().getConfiguration();
		config.locale = mLocale;
		mApplication.getResources().updateConfiguration(config, mApplication.getResources().getDisplayMetrics());
	}

}
