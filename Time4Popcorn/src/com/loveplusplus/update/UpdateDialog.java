package com.loveplusplus.update;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.heliocratic.imovies.R;

public class UpdateDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.newUpdateAvailable);
		builder.setPositiveButton(R.string.dialogPositiveButton,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						goToDownload();
						dismiss();
					}
				}).setNegativeButton(R.string.dialogNegativeButton,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						dismiss();
					}
				});

		return builder.create();
	}

	private void goToDownload() {
		Intent intent = new Intent(getActivity().getApplicationContext(),
				DownloadService.class);
		intent.putExtra(Constants.APK_DOWNLOAD_URL,
				getArguments().getString(Constants.APK_DOWNLOAD_URL));
		getActivity().startService(intent);
	}
}