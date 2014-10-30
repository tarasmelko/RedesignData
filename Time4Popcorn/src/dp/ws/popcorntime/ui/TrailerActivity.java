package dp.ws.popcorntime.ui;

import dp.ws.popcorntime.R;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class TrailerActivity extends Activity {

	public static final String TRAILER_URL_KEY = "popcorn_trailer";

	private WebView trailer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String url = getIntent().getStringExtra(TRAILER_URL_KEY);

		setContentView(R.layout.activity_trailer);
		trailer = (WebView) findViewById(R.id.trailer_view);
		trailer.getSettings().setJavaScriptEnabled(true);
		trailer.getSettings().setDomStorageEnabled(true);
		trailer.loadUrl(url);
	}

	@Override
	protected void onDestroy() {
		trailer.loadUrl("about:blank");
		super.onDestroy();
	}
}