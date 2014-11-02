package dp.ws.popcorntime.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.utils.Constants;
import dp.ws.popcorntime.utils.RoundDisplayer;

public class DescriptionActivity extends Activity implements OnClickListener {

	private DisplayImageOptions options;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.description_fragment);
		((TextView) findViewById(R.id.movie_name)).setText(getIntent()
				.getExtras().getString(Constants.NAME));
		((TextView) findViewById(R.id.description_text)).setText(getIntent()
				.getExtras().getString(Constants.DESCRIPTION));
		options = new DisplayImageOptions.Builder().cacheInMemory(true)
				.cacheOnDisk(true).displayer(new RoundDisplayer(0))
				.showImageOnLoading(android.R.color.transparent).build();
		ImageLoader.getInstance().displayImage(
				getIntent().getExtras().getString(Constants.ICON),
				((ImageView) findViewById(R.id.movie_icon_iv)), options);

		findViewById(R.id.description_action_back).setOnClickListener(this);
		findViewById(R.id.description_action_settings).setOnClickListener(this);

		findViewById(R.id.watch_btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent movie = new Intent(DescriptionActivity.this,
						VideoStreamActivity.class);
				movie.putExtra(Constants.MEDIA, getIntent().getExtras()
						.getString(Constants.MEDIA));
				movie.putExtra(Constants.NAME, getIntent().getExtras()
						.getString(Constants.NAME));
				movie.putExtra(Constants.ICON, getIntent().getExtras()
						.getString(Constants.ICON));
				startActivity(movie);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.description_action_back:
			finish();
			break;
		case R.id.description_action_settings:
			startActivity(new Intent(DescriptionActivity.this,
					SettingsActivity.class));
			break;
		default:
			break;
		}
	}
}
