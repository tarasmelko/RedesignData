package com.heliocratic.imovies.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.heliocratic.imovies.model.videoinfo.VideoInfo;
import com.heliocratic.imovies.model.videoinfo.movie.MovieInfo;
import com.heliocratic.imovies.model.videoinfo.tvshow.TVShowInfo;


public class JSONHelper {

	public static ArrayList<VideoInfo> parseMovies(String json) throws Exception {
		ArrayList<VideoInfo> info = new ArrayList<VideoInfo>();

		JSONArray videos = new JSONObject(json).getJSONArray("MovieList");
		for (int i = 0; i < videos.length(); i++) {
			JSONObject video = videos.getJSONObject(i);
			MovieInfo movieInfo = new MovieInfo();
			movieInfo.populate(video);
			info.add(movieInfo);
		}

		return info;
	}

	public static ArrayList<VideoInfo> parseTVShows(String json) throws Exception {
		ArrayList<VideoInfo> info = new ArrayList<VideoInfo>();

		JSONArray videos = new JSONObject(json).getJSONArray("MovieList");
		for (int i = 0; i < videos.length(); i++) {
			JSONObject video = videos.getJSONObject(i);
			TVShowInfo tvShowInfo = new TVShowInfo();
			tvShowInfo.populate(video);
			info.add(tvShowInfo);
		}

		return info;
	}
}