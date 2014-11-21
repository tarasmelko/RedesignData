package com.heliocratic.imovies.ui.locale;

import com.heliocratic.imovies.IMoviesApplication;

import android.app.DialogFragment;
import android.os.Bundle;

public class LocaleDialogFragment extends DialogFragment implements LocaleListener {

	protected LocaleHelper mLocaleHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mLocaleHelper = new LocaleHelper((IMoviesApplication) getActivity().getApplication(), LocaleDialogFragment.this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void updateLocaleText() {

	}
}