package com.heliocratic.imovies.controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.heliocratic.imovies.R;

public class EpisodeAdapter extends BaseAdapter {

	private int mPosition = 0;
	private Context context;
	private List<String> data;
	private LayoutInflater inflater;

	public EpisodeAdapter(Context context, List<String> data) {
		this.context = context;
		this.data = data;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public EpisodeAdapter(Context context) {
		this(context, new ArrayList<String>());
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public String getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		EpisodeHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_episode, parent, false);
			holder = new EpisodeHolder();
			holder.name = (TextView) convertView.findViewById(R.id.episode_name);
			convertView.setTag(holder);
		} else {
			holder = (EpisodeHolder) convertView.getTag();
		}

		String name = context.getResources().getString(R.string.episode) + " " + getItem(position);

		if (mPosition == position) {
			holder.name.setText(Html.fromHtml("<b>" + name + "</b>"));
		} else {
			holder.name.setText(name);
		}

		return convertView;
	}

	public void setSelectedItem(int position) {
		mPosition = position;
		notifyDataSetInvalidated();
	}

	public void replaceData(List<String> data) {
		this.data = data;
		mPosition = 0;
		notifyDataSetChanged();
	}

	private class EpisodeHolder {
		public TextView name;
	}
}