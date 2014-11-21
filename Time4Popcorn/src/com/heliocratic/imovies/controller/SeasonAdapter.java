package com.heliocratic.imovies.controller;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.heliocratic.imovies.R;

public class SeasonAdapter extends BaseAdapter {

	private int mPosition = 0;
	private Context context;
	private List<String> data;
	private LayoutInflater inflater;

	public SeasonAdapter(Context context, List<String> data) {
		this.context = context;
		this.data = data;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		SeasonHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_season, parent, false);
			holder = new SeasonHolder();
			holder.name = (TextView) convertView.findViewById(R.id.season_name);
			convertView.setTag(holder);
		} else {
			holder = (SeasonHolder) convertView.getTag();
		}

		String name = context.getResources().getString(R.string.season) + " " + getItem(position);

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

	private class SeasonHolder {
		public TextView name;
	}
}