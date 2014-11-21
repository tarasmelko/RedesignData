package com.heliocratic.imovies.ui.locale;

import java.util.Locale;

import com.heliocratic.imovies.IMoviesApplication;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.widget.PopupMenu;

public class LocalePopupMenu extends PopupMenu {

	public LocalePopupMenu(Activity activity, View anchor) {
		super(activity, anchor);

		Locale locale = new Locale(((IMoviesApplication) activity.getApplication()).getAppLocale().getLanguage());
		Locale.setDefault(locale);
		Configuration config = activity.getResources().getConfiguration();
		config.locale = locale;
		activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
	}

}