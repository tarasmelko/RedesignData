package dp.ws.popcorntime.controller;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dp.ws.popcorntime.R;
import dp.ws.popcorntime.model.videodata.Movie;
import dp.ws.popcorntime.ui.DescriptionActivity;
import dp.ws.popcorntime.ui.MainActivity;
import dp.ws.popcorntime.utils.Constants;
import dp.ws.popcorntime.utils.LoadImage;

public class MoviesGridAdapter extends BaseAdapter {

	private Context mContext;

	private List<Movie> mData;

	private LayoutInflater mInflater;
	private static final String NO_INFORMATION = "No information";
	private LoadImage mLoader;

	public MoviesGridAdapter(Activity context, List<Movie> in) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mData = in;
		mLoader = new LoadImage(mContext);
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class ViewHolder {
		TextView name;
		ImageView image;
		TextView descpription;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.item_movie, parent, false);

			holder.name = (TextView) convertView
					.findViewById(R.id.item_movie_name_tv);
			holder.image = (ImageView) convertView
					.findViewById(R.id.item_movie_image_iv);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();

		}

		final Movie item = (Movie) mData.get(position);

		mLoader.loadImageRoundedCache(Constants.ICON_PREFIX + item.getImage(),
				holder.image, 10);
		holder.name.setText(item.getName() != null ? item.getName()
				: NO_INFORMATION);

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent description = new Intent(mContext,
						DescriptionActivity.class);
				description.putExtra(Constants.DESCRIPTION,
						item.getDescription());
				description.putExtra(Constants.ICON, Constants.ICON_PREFIX
						+ item.getImage());
				description.putExtra(Constants.NAME, item.getName());
				description.putExtra(Constants.MEDIA, Constants.VIDEO_PREFIX
						+ item.getFiles());
				((MainActivity) mContext).startActivity(description);
			}
		});

		return convertView;
	}
}