package com.heliocratic.imovies.controller;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.heliocratic.imovies.R;
import com.heliocratic.imovies.database.tables.Favorites;
import com.heliocratic.imovies.model.videodata.VideoData;
import com.heliocratic.imovies.model.videoinfo.VideoInfo;
import com.heliocratic.imovies.model.videoinfo.movie.MovieInfo;
import com.heliocratic.imovies.model.videoinfo.tvshow.TVShowInfo;
import com.heliocratic.imovies.utils.RoundDisplayer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FavoritesAdapter extends CursorAdapter {

	private Activity activity;
	private LayoutInflater inflater;
	private DisplayImageOptions options;

	public FavoritesAdapter(Activity context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		this.activity = context;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).displayer(new RoundDisplayer(6))
				.showImageOnLoading(android.R.color.transparent).build();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		VideoHolder holder = (VideoHolder) view.getTag();
		String type = cursor.getString(cursor
				.getColumnIndexOrThrow(Favorites._TYPE));
		VideoInfo info = null;
		if (VideoData.Type.MOVIES.equals(type)) {
			info = new MovieInfo();
		} else if (VideoData.Type.TV_SHOWS.equals(type)) {
			info = new TVShowInfo();
		}
		try {
			info.populate(cursor);
		} catch (Exception e) {
			e.printStackTrace();
		}

		holder.poster.setOnClickListener(new VideoItemListener(context, info));
		holder.poster.setOnLongClickListener(new FavoritesListener(activity,
				info));
		ImageLoader.getInstance().displayImage(info.posterMediumUrl,
				holder.poster, options);
		holder.name.setText(Html.fromHtml("<b>" + info.title + "</b>"));
		holder.year.setText(info.year);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		VideoHolder holder = new VideoHolder();
		View view = inflater.inflate(R.layout.item_grid_video, parent, false);
		holder.poster = (ImageView) view.findViewById(R.id.video_poster);
		holder.name = (TextView) view.findViewById(R.id.video_name);
		holder.year = (TextView) view.findViewById(R.id.video_year);
		view.setTag(holder);
		return view;
	}

}