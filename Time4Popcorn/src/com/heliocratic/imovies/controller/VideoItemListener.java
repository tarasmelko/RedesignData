package com.heliocratic.imovies.controller;

import com.heliocratic.imovies.model.videoinfo.VideoInfo;
import com.heliocratic.imovies.ui.VideoActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class VideoItemListener implements OnClickListener {

	private Context context;
	private VideoInfo info;

	public VideoItemListener(Context context, VideoInfo info) {
		this.context = context;
		this.info = info;
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(context, VideoActivity.class);
		Bundle extras = new Bundle();
		extras.putParcelable(VideoActivity.VIDEO_INFO_KEY, info);
		intent.putExtras(extras);
		context.startActivity(intent);
	}
}