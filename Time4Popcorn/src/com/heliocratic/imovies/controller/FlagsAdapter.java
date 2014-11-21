package com.heliocratic.imovies.controller;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.heliocratic.imovies.R;

public class FlagsAdapter extends BaseAdapter {

	public String[] FLAGS_NAME;
	public TypedArray icons;
	private LayoutInflater inflater;

	public FlagsAdapter(Context context) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		FLAGS_NAME = context.getResources().getStringArray(R.array.flag_names);
		icons = context.getResources().obtainTypedArray(R.array.icons_flags);
	}

	@Override
	public int getCount() {
		return FLAGS_NAME.length;
	}

	@Override
	public String getItem(int position) {
		return FLAGS_NAME[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GenreHolder holder;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_list_flags, parent,
					false);
			holder = new GenreHolder();
			holder.name = (TextView) convertView.findViewById(R.id.flag_name);
			holder.icon = (ImageView) convertView.findViewById(R.id.flag_icon);
			convertView.setTag(holder);
		} else {
			holder = (GenreHolder) convertView.getTag();
		}

		holder.name.setText(FLAGS_NAME[position]);
		holder.icon.setImageDrawable(icons.getDrawable(position));

		return convertView;
	}

	private class GenreHolder {
		public TextView name;
		public ImageView icon;
	}

}