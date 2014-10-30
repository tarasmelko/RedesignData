package dp.ws.popcorntime.utils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dp.ws.popcorntime.model.videodata.Movie;

public class Parser {

	public static List<Movie> feeds(String response) {

		List<Movie> feeds = new ArrayList<Movie>();
		if (response != null) {
			Type type = new TypeToken<List<Movie>>() {
			}.getType();
			Gson parser = new Gson();
			feeds = parser.fromJson(response, type);
		}
		return feeds;
	}

}
