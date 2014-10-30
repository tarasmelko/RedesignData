package dp.ws.popcorntime.controller;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.model.videodata.Movie;
import dp.ws.popcorntime.ui.DescriptionFragment;
import dp.ws.popcorntime.ui.MainActivity;
import dp.ws.popcorntime.utils.Constants;
import dp.ws.popcorntime.utils.RoundDisplayer;

public class MoviesGridAdapter extends BaseAdapter {

	private Context mContext;

	private List<Movie> mData;

	private LayoutInflater inflater;
	private DisplayImageOptions options;

	public MoviesGridAdapter(Context context, List<Movie> in) {
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mContext = context;
		mData = in;
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).displayer(new RoundDisplayer(6))
				.showImageOnLoading(android.R.color.transparent).build();
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
		VideoHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.item_grid_video, parent,
					false);
			holder = new VideoHolder();
			holder.poster = (ImageView) convertView
					.findViewById(R.id.video_poster);
			holder.name = (TextView) convertView.findViewById(R.id.video_name);
			holder.year = (TextView) convertView.findViewById(R.id.video_year);
			convertView.setTag(holder);
		} else {
			holder = (VideoHolder) convertView.getTag();
		}

		final Movie item = (Movie) mData.get(position);
		ImageLoader.getInstance()
				.displayImage(Constants.ICON_PREFIX + item.getImage(),
						holder.poster, options);
		holder.name.setText(Html.fromHtml("<b>" + item.getName() + "</b>"));
		holder.year.setText("");

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((MainActivity) mContext).replaceFragment(DescriptionFragment
						.instance(Constants.ICON_PREFIX + item.getImage(),
								Constants.VIDEO_PREFIX + item.getFiles(),
								item.getDescription(), item.getName()));

			}
		});

		return convertView;
	}
}