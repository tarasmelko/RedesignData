package dp.ws.popcorntime.ui.locale;

import dp.ws.popcorntime.PopcornApplication;
import android.app.DialogFragment;
import android.os.Bundle;

public class LocaleDialogFragment extends DialogFragment implements LocaleListener {

	protected LocaleHelper mLocaleHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mLocaleHelper = new LocaleHelper((PopcornApplication) getActivity().getApplication(), LocaleDialogFragment.this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void updateLocaleText() {

	}
}