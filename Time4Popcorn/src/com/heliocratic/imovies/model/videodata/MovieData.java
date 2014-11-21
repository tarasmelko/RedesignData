package com.heliocratic.imovies.model.videodata;

import android.content.Context;

import com.heliocratic.imovies.R;

public class MovieData extends VideoData {

	private String quality = Quality.P_720 + "," + Quality.P_1080;

	public MovieData(Context context) {
		type = Type.MOVIES;
		sort = Sort.SEEDS;
		format = Format.MP4;
		requestGenres = context.getResources().getStringArray(R.array.request_genres);
	}

	@Override
	public String getRequestURl() {
		super.getRequestURl();

		sb.append("&quality=" + quality);

		return sb.toString();
	}
}