package dp.ws.popcorntime.ui;

import org.videolan.libvlc.LibVLC;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import dp.ws.popcorntime.PopcornApplication;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.subtitles.Subtitles;
import dp.ws.popcorntime.torrent.PopcornTorrent;
import dp.ws.popcorntime.ui.base.PlayerBaseActivity;
import dp.ws.popcorntime.ui.base.PopcornBaseActivity;
import dp.ws.popcorntime.ui.locale.LocaleDialogFragment;
import dp.ws.popcorntime.utils.LanguageUtil;
import dp.ws.popcorntime.utils.StorageHelper;

public class SettingsActivity extends PopcornBaseActivity {

	private final int REQUEST_DIRECTORY = 3457;

	private PopcornApplication mApplication;
	private SharedPreferences preferences;

	// language
	private LanguageDialog languageDialog;

	// theme
	private String[] themes;
	private ThemeDialog themeDialog;

	// hardware acceleration
	private String[] accelerations;
	private final int[] accelerationCode = new int[] { LibVLC.HW_ACCELERATION_AUTOMATIC, LibVLC.HW_ACCELERATION_DISABLED, LibVLC.HW_ACCELERATION_DECODING,
			LibVLC.HW_ACCELERATION_FULL };
	private HwAccelerationDialog accelerationDialog;

	// subtitles
	private String[] fontSizeNames;
	private SubtitleLanguageDialog subtitleLanguageDialog;
	private SubtitleFontSizeDialog subtitleFontSizeDialog;

	// view
	private TextView headerTitle;
	private TextView interfaceTitle;
	private TextView languageTitle;
	private TextView languageSummary;
	private TextView themeTitle;
	private TextView themeSummary;
	private TextView playerTitle;
	private TextView hwAccelerationTitle;
	private TextView hwAccelerationSummary;
	private TextView subtitlesTitle;
	private TextView subtitlesLanguageTitle;
	private TextView subtitlesLanguageSummary;
	private TextView subtitlesFontSizeTitle;
	private TextView subtitlesFontSizeSummary;
	private TextView downloadsTitle;
	private TextView vpnTitle;
	private TextView vpnSummary;
	private CheckBox vpnCheckBox;
	private TextView chacheFolderTitle;
	private TextView chacheFolderSummary;
	private TextView clearChacheFolderTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);

		mApplication = (PopcornApplication) getApplication();
		preferences = getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);

		// Header
		getPopcornLogoView().setVisibility(View.GONE);
		View header = setPopcornHeaderView(R.layout.header_settings);
		header.findViewById(R.id.header_action_back).setOnClickListener(backListener);
		headerTitle = (TextView) header.findViewById(R.id.header_title);

		// Content
		View content = setPopcornContentView(R.layout.activity_settings);

		/*
		 * INTERFACE
		 */
		interfaceTitle = (TextView) content.findViewById(R.id.settings_interface_title);

		View language = content.findViewById(R.id.settings_language);
		language.setOnClickListener(languageListener);
		languageTitle = (TextView) language.findViewById(R.id.settings_language_title);
		languageSummary = (TextView) language.findViewById(R.id.settings_language_summary);

		View theme = content.findViewById(R.id.settings_theme);
		theme.setOnClickListener(themeListener);
		themeTitle = (TextView) theme.findViewById(R.id.settings_theme_title);
		themeSummary = (TextView) theme.findViewById(R.id.settings_theme_summary);

		/*
		 * PLAYER
		 */
		playerTitle = (TextView) content.findViewById(R.id.settings_player_title);

		View hwAcceleration = content.findViewById(R.id.settings_hw_acceleration);
		hwAcceleration.setOnClickListener(hwAccelerationListener);
		hwAccelerationTitle = (TextView) hwAcceleration.findViewById(R.id.settings_hw_acceleration_title);
		hwAccelerationSummary = (TextView) hwAcceleration.findViewById(R.id.settings_hw_acceleration_summary);

		/*
		 * Subtitles
		 */
		subtitlesTitle = (TextView) content.findViewById(R.id.settings_subtitles_title);

		View subtatlesLanguage = content.findViewById(R.id.settings_subtitles_language);
		subtatlesLanguage.setOnClickListener(subtitleLanguageListener);
		subtitlesLanguageTitle = (TextView) subtatlesLanguage.findViewById(R.id.settings_subtitles_language_title);
		subtitlesLanguageSummary = (TextView) subtatlesLanguage.findViewById(R.id.settings_subtitles_language_summary);

		View subtitlesFontSize = content.findViewById(R.id.settings_subtitles_font_size);
		subtitlesFontSize.setOnClickListener(subtitleFontSizeListener);
		subtitlesFontSizeTitle = (TextView) subtitlesFontSize.findViewById(R.id.settings_subtitles_font_size_title);
		subtitlesFontSizeSummary = (TextView) subtitlesFontSize.findViewById(R.id.settings_subtitles_font_size_summary);

		/*
		 * DWNLOADS
		 */
		downloadsTitle = (TextView) content.findViewById(R.id.settings_downloads_title);

		View vpn = content.findViewById(R.id.settings_vpn);
		vpn.setOnClickListener(vpnListener);
		vpnTitle = (TextView) vpn.findViewById(R.id.settings_vpn_title);
		vpnSummary = (TextView) vpn.findViewById(R.id.settings_vpn_summary);
		View vpnSponsor = vpn.findViewById(R.id.settings_vpn_sponsor);
		vpnSponsor.setOnClickListener(vpnSponsorListener);
		vpnCheckBox = (CheckBox) vpn.findViewById(R.id.settings_vpn_checkbox);
		vpnCheckBox.setChecked(preferences.getBoolean(PopcornTorrent.IS_PROXY_ENABLE_KEY, false));
		vpnCheckBox.setOnCheckedChangeListener(vpnCheckedListener);

		View chacheFolder = content.findViewById(R.id.settings_cache_folder);
		chacheFolder.setOnClickListener(chacheListener);
		chacheFolderTitle = (TextView) chacheFolder.findViewById(R.id.settings_cache_folder_title);
		chacheFolderSummary = (TextView) chacheFolder.findViewById(R.id.settings_cache_folder_summary);
		chacheFolderSummary.setText(StorageHelper.getInstance().getChacheDirectoryPath());

		View clearChacheFolder = content.findViewById(R.id.settings_clear_cache_folder);
		clearChacheFolder.setOnClickListener(clearChacheListener);
		clearChacheFolderTitle = (TextView) clearChacheFolder.findViewById(R.id.settings_clear_cache_folder_title);

		updateLocaleText();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		themes = getResources().getStringArray(R.array.themes);
		accelerations = getResources().getStringArray(R.array.accelerations);
		fontSizeNames = getResources().getStringArray(R.array.font_size_names);
		LanguageUtil.SUBTITLE_NATIVE_LANGUAGES[0] = getString(R.string.without_subtitle);
		headerTitle.setText(R.string.settings);
		interfaceTitle.setText(R.string.interface_);
		languageTitle.setText(R.string.language);
		languageSummary.setText(LanguageUtil.isoToNativeLanguage(mApplication.getAppLocale().getLanguage()));
		themeTitle.setText(R.string.theme);
		themeSummary.setText(getCurrentTheme());
		playerTitle.setText(R.string.player);
		hwAccelerationTitle.setText(R.string.hardware_acceleration);
		hwAccelerationSummary.setText(getCurrentAccelerationDesc());
		subtitlesTitle.setText(R.string.subtitles);
		subtitlesLanguageTitle.setText(R.string.default_subtitle);
		String subLang = mApplication.getSubtitleLanguage();
		if ("".equals(subLang)) {
			subtitlesLanguageSummary.setText(R.string.without_subtitle);
		} else {
			subtitlesLanguageSummary.setText(LanguageUtil.languageToNativeLanguage(subLang));
		}
		subtitlesFontSizeTitle.setText(R.string.font_size);
		subtitlesFontSizeSummary.setText(getCurrentFontSizeName());
		downloadsTitle.setText(R.string.downloads);
		vpnTitle.setText(R.string.vpn_connection);
		vpnSummary.setText(R.string.sponsored_by);
		chacheFolderTitle.setText(R.string.cache_folder);
		clearChacheFolderTitle.setText(R.string.clear_cache_folder);
	}

	private String getCurrentAccelerationDesc() {
		int hw_acc = preferences.getInt(PlayerBaseActivity.SETTINGS_HW_ACCELERATION, LibVLC.HW_ACCELERATION_AUTOMATIC);
		return getAccelerationDesc(hw_acc);
	}

	private String getAccelerationDesc(int code) {
		switch (code) {
		case LibVLC.HW_ACCELERATION_AUTOMATIC:
			return getString(R.string.automatic);
		case LibVLC.HW_ACCELERATION_DECODING:
			return getString(R.string.hardware_acceleration_decoding);
		case LibVLC.HW_ACCELERATION_DISABLED:
			return getString(R.string.hardware_acceleration_disabled);
		case LibVLC.HW_ACCELERATION_FULL:
			return getString(R.string.hardware_acceleration_full);
		default:
			return "none";
		}
	}

	private String getCurrentTheme() {
		return getString(R.string.theme_classic);
	}

	private String getCurrentFontSizeName() {
		int pos = preferences.getInt(Subtitles.FONT_SIZE_PREF, Subtitles.FontSize.DEFAULT_POSITION);
		if (pos < fontSizeNames.length) {
			return fontSizeNames[pos];
		} else {
			return fontSizeNames[Subtitles.FontSize.DEFAULT_POSITION];
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (REQUEST_DIRECTORY == requestCode) {
				String path = data.getStringExtra(FolderChooserActivity.SELECTED_DIR);
				StorageHelper.getInstance().setChacheDirectory(SettingsActivity.this, path);
				chacheFolderSummary.setText(StorageHelper.getInstance().getChacheDirectoryPath());
			}
		}
	}

	/*
	 * Listeners
	 */

	private OnClickListener backListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	private OnClickListener languageListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (languageDialog == null) {
				languageDialog = new LanguageDialog();
			}
			if (!languageDialog.isAdded()) {
				languageDialog.show(getFragmentManager(), "language_dialog");
			}
		}
	};

	private OnClickListener themeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (themeDialog == null) {
				themeDialog = new ThemeDialog();
			}
			if (!themeDialog.isAdded()) {
				themeDialog.show(getFragmentManager(), "theme_dialog");
			}
		}
	};

	private OnClickListener hwAccelerationListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (accelerationDialog == null) {
				accelerationDialog = new HwAccelerationDialog();
			}
			if (!accelerationDialog.isAdded()) {
				accelerationDialog.show(getFragmentManager(), "hw_acceleration_dialog");
			}
		}
	};

	private OnClickListener subtitleLanguageListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (subtitleLanguageDialog == null) {
				subtitleLanguageDialog = new SubtitleLanguageDialog();
			}
			if (!subtitleLanguageDialog.isAdded()) {
				subtitleLanguageDialog.show(getFragmentManager(), "subtitles_lang_dialog");
			}
		}
	};

	private OnClickListener subtitleFontSizeListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (subtitleFontSizeDialog == null) {
				subtitleFontSizeDialog = new SubtitleFontSizeDialog();
			}
			if (!subtitleFontSizeDialog.isAdded()) {
				subtitleFontSizeDialog.show(getFragmentManager(), "subtitles_font_size_dialog");
			}
		}
	};

	private OnClickListener vpnListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (vpnCheckBox.isChecked()) {
				vpnCheckBox.setChecked(false);
			} else {
				vpnCheckBox.setChecked(true);
			}
		}
	};

	private OnClickListener vpnSponsorListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://kebrum.com/popcorntime")));
		}
	};

	private OnCheckedChangeListener vpnCheckedListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			preferences.edit().putBoolean(PopcornTorrent.IS_PROXY_ENABLE_KEY, isChecked).commit();
		}
	};

	private OnClickListener chacheListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent chooserIntent = new Intent(SettingsActivity.this, FolderChooserActivity.class);
			startActivityForResult(chooserIntent, REQUEST_DIRECTORY);
		}
	};

	private OnClickListener clearChacheListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			clearHandler.sendEmptyMessage(123);
		}
	};

	private Handler clearHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			StorageHelper.getInstance().clearChacheDirectory();
		}
	};

	/*
	 * Dialogs
	 */

	private class LanguageDialog extends LocaleDialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.language));
			builder.setItems(LanguageUtil.INTERFACE_NATIVE_LANGUAGES, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mApplication.changeLanguage(LanguageUtil.INTERFACE_ISO_LANGUAGES[which]);
					SettingsActivity.this.mLocaleHelper.checkLanguage();
				}
			});
			return builder.create();
		}
	}

	private class ThemeDialog extends LocaleDialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.theme));
			builder.setItems(themes, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			return builder.create();
		}
	}

	private class HwAccelerationDialog extends LocaleDialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.hardware_acceleration));
			builder.setItems(accelerations, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					preferences.edit().putInt(PlayerBaseActivity.SETTINGS_HW_ACCELERATION, accelerationCode[which]).commit();
					hwAccelerationSummary.setText(getAccelerationDesc(accelerationCode[which]));
				}
			});
			return builder.create();
		}
	}

	private class SubtitleLanguageDialog extends LocaleDialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.subtitles));
			String subLang = mApplication.getSubtitleLanguage();
			int pos = 0;
			for (int i = 0; i < LanguageUtil.SUBTITLE_LANGUAGES.length; i++) {
				if (subLang.equals(LanguageUtil.SUBTITLE_LANGUAGES[i])) {
					pos = i;
					break;
				}
			}
			builder.setSingleChoiceItems(LanguageUtil.SUBTITLE_NATIVE_LANGUAGES, pos, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					subtitlesLanguageSummary.setText(LanguageUtil.SUBTITLE_NATIVE_LANGUAGES[which]);
					mApplication.setSubtitleLanguage(LanguageUtil.SUBTITLE_LANGUAGES[which]);
					dialog.dismiss();
				}
			});
			return builder.create();
		}
	}

	private class SubtitleFontSizeDialog extends LocaleDialogFragment {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getResources().getString(R.string.font_size));
			builder.setItems(fontSizeNames, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					preferences.edit().putInt(Subtitles.FONT_SIZE_PREF, which).commit();
					subtitlesFontSizeSummary.setText(fontSizeNames[which]);
				}
			});
			return builder.create();
		}
	}

}
