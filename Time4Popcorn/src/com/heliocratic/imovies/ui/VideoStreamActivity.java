package com.heliocratic.imovies.ui;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.heliocratic.imovies.R;
import com.heliocratic.imovies.utils.Constants;
import com.heliocratic.imovies.utils.Preference;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

public class VideoStreamActivity extends Activity {

	private VideoView mVideoView;
	private ImageView mPlayStopBtn;
	private ImageView mImage;
	private SeekBar mProgressSeekBar;
	private TextView mCurrnetTime;
	private TextView mFullTime;
	private RelativeLayout mTopContent;
	private RelativeLayout mBottomContetn;
	private Timer timer;
	private boolean barsVisibility = true;
	private int time = 6000;

	private static final String CONFIG_ENVIRONMENT = PayPalConfiguration.ENVIRONMENT_PRODUCTION;

	private static final String CONFIG_CLIENT_ID = "ARAEHBBZ_2uPrsCkO53bKeZb9taA1Y4adbJ4vGha5eI_2WvSw763bBdY6bS1";

	private static PayPalConfiguration config = new PayPalConfiguration()
			.environment(CONFIG_ENVIRONMENT).clientId(CONFIG_CLIENT_ID);

	private static final int REQUEST_CODE_PAYMENT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_stream);
		setupView();
	}

	private void setupView() {

		findViewById(R.id.subscribe).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE);
				Intent intent = new Intent(VideoStreamActivity.this,
						PaymentActivity.class);

				intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

				startActivityForResult(intent, REQUEST_CODE_PAYMENT);
			}
		});
		showVideoProgress();

		mVideoView = (VideoView) findViewById(R.id.fragment_video_streaming_vv);
		mProgressSeekBar = (SeekBar) findViewById(R.id.fragment_video_streaming_progress_sb);

		mCurrnetTime = (TextView) findViewById(R.id.fragment_video_streaming_time_progress_tv);
		mFullTime = (TextView) findViewById(R.id.fragment_video_streaming_full_time_tv);

		mTopContent = (RelativeLayout) findViewById(R.id.fragment_video_streaming_top_content_rl);
		mBottomContetn = (RelativeLayout) findViewById(R.id.fragment_video_streaming_bottom_content_rl);

		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				if (!Preference.getUserPaypal())
					runPaypalSubscribe();
				stopVideoProgress();
				mProgressSeekBar.setMax(mVideoView.getDuration());
				mProgressSeekBar.postDelayed(onEverySecond, 1000);
				mFullTime.setText(countTime(mVideoView.getDuration()));
				timer = new Timer();
				timer.schedule(new HideBarsTask(), time);

			}
		});

		mVideoView.requestFocus();

		mPlayStopBtn = (ImageView) findViewById(R.id.fragment_video_streaming_play_pause_iv);
		mImage = (ImageView) findViewById(R.id.fragment_video_streaming_icon_iv);

		mProgressSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (fromUser) {
							mVideoView.seekTo(progress);
						}
					}
				});

		setupDataAndPlay(getIntent().getExtras().getString(Constants.MEDIA),
				getIntent().getExtras().getString(Constants.NAME));

		mPlayStopBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (((Integer) mPlayStopBtn.getTag()) == R.drawable.play) {
					mPlayStopBtn.setImageResource(R.drawable.pause);
					mPlayStopBtn.setTag(R.drawable.pause);
					mVideoView.start();
					mProgressSeekBar.postDelayed(onEverySecond, 1000);
				} else {
					mPlayStopBtn.setImageResource(R.drawable.play);
					mPlayStopBtn.setTag(R.drawable.play);
					mVideoView.pause();
				}
			}
		});

	}

	Handler payPalDelay;
	Runnable run;

	private void runPaypalSubscribe() {
		int time = 15 * 60000;
		Intent intent = new Intent(this, PayPalService.class);
		intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
		startService(intent);
		payPalDelay = new Handler();
		run = new Runnable() {

			@Override
			public void run() {
				findViewById(R.id.paypal_layout).setVisibility(View.VISIBLE);
			}
		};

		payPalDelay.postDelayed(run, time);

	}

	private PayPalPayment getThingToBuy(String paymentIntent) {
		return new PayPalPayment(new BigDecimal("9.99"), "USD",
				"Subscription for one year", paymentIntent);
	}

	public void setupDataAndPlay(String url, String title) {

		mPlayStopBtn.setImageResource(R.drawable.pause);
		mPlayStopBtn.setTag(R.drawable.pause);

		TextView titleIcon = (TextView) findViewById(R.id.fragment_video_streaming_title_tv);
		titleIcon.setText(title + "");

		mVideoView.setVideoURI(Uri.parse(url));
		showVideoProgress();
		mVideoView.start();
		mProgressSeekBar.postDelayed(onEverySecond, 1000);
	}

	private void showVideoProgress() {
		findViewById(R.id.fragment_video_streaming_shadow_iv).setVisibility(
				View.VISIBLE);
		findViewById(R.id.loading).setVisibility(View.VISIBLE);
		findViewById(R.id.fragment_video_streaming_pb).setVisibility(
				View.VISIBLE);
	}

	private void stopVideoProgress() {
		findViewById(R.id.fragment_video_streaming_shadow_iv).setVisibility(
				View.GONE);
		findViewById(R.id.loading).setVisibility(View.GONE);
		findViewById(R.id.fragment_video_streaming_pb).setVisibility(View.GONE);
	}

	private Runnable onEverySecond = new Runnable() {

		@Override
		public void run() {

			if (mProgressSeekBar != null) {
				mProgressSeekBar.setProgress(mVideoView.getCurrentPosition());
			}

			if (mVideoView.isPlaying()) {
				mProgressSeekBar.postDelayed(onEverySecond, 1000);
				mCurrnetTime
						.setText(countTime(mVideoView.getCurrentPosition()));

				if (mVideoView.getCurrentPosition() > (15 * 60000)
						&& !Preference.getUserPaypal()) {
					if (payPalDelay != null && run != null)
						payPalDelay.removeCallbacks(run);
					if (findViewById(R.id.paypal_layout).getVisibility() == View.GONE)
						findViewById(R.id.paypal_layout).setVisibility(
								View.VISIBLE);
				}

			}

		}
	};

	private void showBars() {
		AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
		alpha.setDuration(500);
		alpha.setFillAfter(true);
		mTopContent.clearAnimation();
		mTopContent.startAnimation(alpha);
		mBottomContetn.clearAnimation();
		mBottomContetn.startAnimation(alpha);
		barsVisibility = true;
		timer = new Timer();
		timer.schedule(new HideBarsTask(), time);
	}

	public String countTime(int miliseconds) {
		String timeInMinutes = new String();
		int minutes = miliseconds / 60000;
		int seconds = (miliseconds % 60000) / 1000;
		timeInMinutes = minutes + ":"
				+ (seconds < 10 ? "0" + seconds : seconds);

		return timeInMinutes;
	}

	@Override
	protected void onResume() {
		if (!barsVisibility)
			showBars();
		super.onResume();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!barsVisibility)
			showBars();
		return super.onTouchEvent(event);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_PAYMENT) {
			if (resultCode == Activity.RESULT_OK) {
				PaymentConfirmation confirm = data
						.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
				if (confirm != null) {
					findViewById(R.id.paypal_layout).setVisibility(View.GONE);
					Preference.saveUserPaypal(true);

					Toast.makeText(getApplicationContext(),
							"Payment has been received", Toast.LENGTH_LONG)
							.show();
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(getApplicationContext(), "Canceled",
						Toast.LENGTH_LONG).show();
			} else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
				Toast.makeText(getApplicationContext(),
						"Some problem has been occured", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private class HideBarsTask extends TimerTask {

		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlphaAnimation alpha = new AlphaAnimation(1f, 0f);
					alpha.setDuration(500);
					alpha.setFillAfter(true);
					mTopContent.clearAnimation();
					mTopContent.startAnimation(alpha);
					mBottomContetn.clearAnimation();
					mBottomContetn.startAnimation(alpha);
					barsVisibility = false;
					cancel();
				}
			});
		}
	}

	@Override
	protected void onDestroy() {
		stopService(new Intent(this, PayPalService.class));
		super.onDestroy();
	}
}
