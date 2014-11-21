package com.heliocratic.imovies.googlecast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.media.MediaRouter.RouteInfo;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.cast.RemoteMediaPlayer.MediaChannelResult;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.heliocratic.imovies.subtitles.Subtitles;

import eu.sesma.castania.castserver.CastServerService;

public class GoogleCast {

	private Activity mActivity;

	private MediaRouter mRouter;
	private MediaRouteSelector mSelector;
	private MediaRouter.Callback mCallback;
	private CastDevice mSelectedDevice;
	private Cast.Listener mCastListener;
	private GoogleApiClient mApiClient;
	private ConnectionCallbacks mConnectionCallbacks;
	private OnConnectionFailedListener mConnectionFailedListener;
	private boolean mWaitingForReconnect = false;
	private boolean mApplicationStarted;
	private RemoteMediaPlayer mRemoteMediaPlayer;

	private TextTrackStyle trackStyle;
	private CastPopcornListener mPopcornListener;
	private SubtitleSender mSubtitleSender = new SubtitleSender();

	// App id obtained from google developer console app registration
	private final String APP_ID_DEFAULT = CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID;
	private final String APP_ID_CURRENT = APP_ID_DEFAULT;

	private final String TAG = "tag";

	public GoogleCast(Activity activity, CastPopcornListener popcornListener) {
		mActivity = activity;
		mPopcornListener = popcornListener;
		trackStyle = TextTrackStyle.fromSystemSettings(activity);
		trackStyle.setBackgroundColor(Color.parseColor("#00ffffff"));
		trackStyle.setForegroundColor(Color.parseColor("#ffffffff"));
		trackStyle.setEdgeType(TextTrackStyle.EDGE_TYPE_DROP_SHADOW);
		trackStyle.setEdgeColor(Color.parseColor("#bb000000"));
		trackStyle.setFontScale(Subtitles.getFontScale(activity));
	}

	public void onCreate(MediaRouteButton routeButton) {
		// Setup the router selector
		mRouter = MediaRouter.getInstance(mActivity);
		// The MediaRouter needs to filter discovery for Cast devices that can
		// launch the receiver application
		// associated with the sender app. In this case we search for ChromeCast
		// devices
		mSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID_CURRENT)).build();
		routeButton.setRouteSelector(mSelector);
		mCallback = new MyMediaRouterCallback();
		stopCastServer();
	}

	public void onResume() {
		mRouter.addCallback(mSelector, mCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
	}

	public void onPause() {
		// Remove the selector on stop to tell the media router that it no
		// longer
		// needs to invest effort trying to discover routes of these kinds for
		// now.
		if (mActivity.isFinishing()) {
			mRouter.removeCallback(mCallback);
		}
	}

	public void onDestroy() {
		try {
			teardown();
			mRouter.selectRoute(mRouter.getDefaultRoute());
		} catch (Exception ex) {
		}
	}

	public void play() {
		if (mRemoteMediaPlayer != null) {
			mRemoteMediaPlayer.play(mApiClient);
		}
	}

	public void pause() {
		if (mRemoteMediaPlayer != null) {
			mRemoteMediaPlayer.pause(mApiClient);
		}
	}

	public void seek(long delta) {
		if (mRemoteMediaPlayer != null) {
			try {
				// need check if app is connected. not connected = exception
				mRemoteMediaPlayer.seek(mApiClient, mRemoteMediaPlayer.getApproximateStreamPosition() + delta);
			} catch (Exception ex) {
			}
		}
	}

	public void setPosition(long position) {
		if (mRemoteMediaPlayer != null) {
			try {
				mRemoteMediaPlayer.seek(mApiClient, position);
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Callback for MediaRouter events
	 */
	private final class MyMediaRouterCallback extends MediaRouter.Callback {

		@Override
		public void onRouteSelected(final MediaRouter router, final RouteInfo info) {
			Log.d(TAG, "onRouteSelected");
			mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

			// Once the application knows which Cast device the user selected,
			// the sender application can launch the
			// receiver application on that device.
			launchReceiver();
			mPopcornListener.onCastRouteSelected();
		}

		@Override
		public void onRouteUnselected(final MediaRouter router, final RouteInfo info) {
			Log.d(TAG, "onRouteUnselected: info=" + info);
			if (mPopcornListener != null && mRemoteMediaPlayer != null) {
				mPopcornListener.onCastRouteUnselected(mRemoteMediaPlayer.getApproximateStreamPosition());
			}
			teardown();
			mSelectedDevice = null;
		}
	}

	/**
	 * Start the receiver app
	 */
	private void launchReceiver() {
		try {
			// The Cast.Listener callbacks are used to inform the sender
			// application about receiver application events:
			mCastListener = new Cast.Listener() {

				@Override
				public void onApplicationStatusChanged() {
					if (mApiClient != null) {
						Log.d(TAG, "onApplicationStatusChanged: " + Cast.CastApi.getApplicationStatus(mApiClient));
					}
				}

				@Override
				public void onVolumeChanged() {
					if (mApiClient != null) {
						Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(mApiClient));
					}
				}

				@Override
				public void onApplicationDisconnected(final int errorCode) {
					Log.d(TAG, "application has stopped");
					teardown();
				}

			};

			// Connect to Google Play services
			// The Cast SDK API’s are invoked using GoogleApiClient. A
			// GoogleApiClient instance is created using the
			// GoogleApiClient.Builder and requires various callbacks
			mConnectionCallbacks = new ConnectionCallbacks();
			mConnectionFailedListener = new ConnectionFailedListener();
			Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions.builder(mSelectedDevice, mCastListener);

			mApiClient = new GoogleApiClient.Builder(mActivity).addApi(Cast.API, apiOptionsBuilder.build()).addConnectionCallbacks(mConnectionCallbacks)
					.addOnConnectionFailedListener(mConnectionFailedListener).build();

			mApiClient.connect();
			mSubtitleSender.launchReceiver(mApiClient);
		} catch (Exception e) {
			Log.e(TAG, "Failed launchReceiver", e);
		}
	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionCallbacks implements GoogleApiClient.ConnectionCallbacks {
		// The application needs to declare GoogleApiClient.ConnectionCallbacks
		// and
		// GoogleApiClient.OnConnectionFailedListener callbacks to be informed
		// of the connection status. All of the
		// Google Play services callbacks run on the main UI thread.
		@Override
		public void onConnected(final Bundle connectionHint) {
			Log.d(TAG, "onConnected");

			if (mApiClient == null) {
				// We got disconnected while this runnable was pending
				// execution.
				return;
			}

			if (mWaitingForReconnect) {
				mWaitingForReconnect = false;

				// Check if the receiver app is still running
				if ((connectionHint != null) && connectionHint.getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
					teardown();
				} else {
					reattachMediaChannel();
				}
			} else {
				try {
					// Once the connection is confirmed, the application can
					// launch the receiver application by
					// specifying the application ID
					Cast.CastApi.launchApplication(mApiClient, APP_ID_CURRENT, false).setResultCallback(new ResultCallback<Cast.ApplicationConnectionResult>() {

						@Override
						public void onResult(final Cast.ApplicationConnectionResult result) {
							Status status = result.getStatus();
							Log.d(TAG, "ApplicationConnectionResultCallback.onResult: statusCode" + status.getStatusMessage());
							if (status.isSuccess()) {
								ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
								String sessionId = result.getSessionId();
								String applicationStatus = result.getApplicationStatus();
								boolean wasLaunched = result.getWasLaunched();

								Log.d(TAG, "application name: " + applicationMetadata.getName() + ", status: " + applicationStatus + ", sessionId: "
										+ sessionId + ", wasLaunched: " + wasLaunched);
								mApplicationStarted = true;

								mSubtitleSender.attachChannel();

								// Once the sender application is connected to
								// the receiver application, the
								// media channel can be created using
								// Cast.CastApi.setMessageReceivedCallbacks:
								attachMediaChannel();

								// popcorn
								mPopcornListener.onCastConnection();
							} else {
								Log.e(TAG, "application could not launch");
								teardown();
							}
						}
					});

				} catch (Exception e) {
					Log.e(TAG, "Failed to launch application", e);
				}
			}
		}

		@Override
		public void onConnectionSuspended(final int cause) {
			// If GoogleApiClient.ConnectionCallbacks.onConnectionSuspended is
			// invoked when the client is temporarily in
			// a disconnected state, the application needs to track the state,
			// so that if
			// GoogleApiClient.ConnectionCallbacks.onConnected is subsequently
			// invoked when the connection is
			// established again, the application should be able to distinguish
			// this from the initial connected state.
			// It is important to re-create any channels when the connection is
			// re-established.
			mWaitingForReconnect = true;
			stopCastServer();
		}

	}

	/**
	 * Google Play services callbacks
	 */
	private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
		@Override
		public void onConnectionFailed(final ConnectionResult result) {
			Log.e(TAG, "onConnectionFailed ");
			teardown();
		}
	}

	/**
	 * Tear down the connection to the receiver
	 */
	private void teardown() {
		if (mPopcornListener != null) {
			mPopcornListener.teardown();
		}
		stopCastServer();
		if (mApiClient != null) {
			if (mApplicationStarted) {
				if (mApiClient.isConnected()) {
					Cast.CastApi.stopApplication(mApiClient);
					// remove media channel:
					detachMediaChannel();
					mApiClient.disconnect();
				}
				mApplicationStarted = false;
			}
			mApiClient = null;
		}
		mSelectedDevice = null;
		mWaitingForReconnect = false;
	}

	/*
	 * Media Channel ===================
	 */

	// The Google Cast SDK supports a media channel to play media on a receiver
	// application. The media channel has a well-known namespace of
	// urn:x-cast:com.google.cast.media.
	// To use the media channel create an instance of RemoteMediaPlayer and set
	// the
	// update listeners to receive media status updates:
	private void attachMediaChannel() {
		Log.d(TAG, "attachMedia()");
		if (null == mRemoteMediaPlayer) {
			mRemoteMediaPlayer = new RemoteMediaPlayer();

			mRemoteMediaPlayer.setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {

				@Override
				public void onStatusUpdated() {
					MediaStatus mediaStatus = mRemoteMediaPlayer.getMediaStatus();
					if (mediaStatus != null) {
						switch (mediaStatus.getPlayerState()) {
						case MediaStatus.PLAYER_STATE_PLAYING:
							mPopcornListener.onCastStatePlaying();
							break;
						case MediaStatus.PLAYER_STATE_PAUSED:
							mPopcornListener.onCastStatePaused();
							break;
						case MediaStatus.PLAYER_STATE_IDLE:
							mPopcornListener.onCastStateIdle();
							break;
						case MediaStatus.PLAYER_STATE_BUFFERING:
							mPopcornListener.onCastStateBuffering();
							break;
						default:
							break;
						}
					}
				}
			});

			mRemoteMediaPlayer.setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
				@Override
				public void onMetadataUpdated() {
					// MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
				}
			});
		}
		try {
			Log.d(TAG, "Registering MediaChannel namespace");
			Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
		} catch (Exception e) {
			Log.e(TAG, "Failed to set up media channel", e);
		}

		// Call RemoteMediaPlayer.requestStatus() and wait for the
		// OnStatusUpdatedListener callback. This will update
		// the internal state of the RemoteMediaPlayer object with the current
		// state of the receiver, including the
		// current session ID.
		mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
			@Override
			public void onResult(final MediaChannelResult result) {
				if (!result.getStatus().isSuccess()) {
					Log.e(TAG, "Failed to request status.");
				}
			}
		});

	}

	private void reattachMediaChannel() {
		if (null != mRemoteMediaPlayer && null != mApiClient) {
			try {
				Log.d(TAG, "Registering MediaChannel namespace");
				Cast.CastApi.setMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace(), mRemoteMediaPlayer);
			} catch (IOException e) {
				Log.e(TAG, "Failed to setup media channel", e);
			} catch (IllegalStateException e) {
				Log.e(TAG, "Failed to setup media channel", e);
			}
		}
	}

	private void detachMediaChannel() {
		Log.d(TAG, "trying to detach media channel");
		if (null != mRemoteMediaPlayer) {
			if (null != mRemoteMediaPlayer && null != Cast.CastApi) {
				try {
					Cast.CastApi.removeMessageReceivedCallbacks(mApiClient, mRemoteMediaPlayer.getNamespace());
				} catch (Exception e) {
					Log.e(TAG, "Failed to detach media channel", e);
				}
			}
			mRemoteMediaPlayer = null;
		}
	}

	// To load media, the sender application needs to create a MediaInfo
	// instance using MediaInfo.Builder. The MediaInfo
	// instance is then used to load the media with the RemoteMediaPlayer
	// instance:
	public void loadMovieMedia(String mediaName, long playPosition, String title, String subPath) {
		WifiManager wm = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
		@SuppressWarnings("deprecation")
		String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

		int slash = mediaName.lastIndexOf('/');
		String filename = mediaName.substring(slash + 1);
		String rootDir = mediaName.substring(0, slash);

		startCastServer(ip, rootDir);

		List<MediaTrack> tracks = new ArrayList<MediaTrack>();

		if (subPath != null) {
			String subname = subPath.substring(subPath.lastIndexOf('/') + 1);
			MediaTrack subtitleTrack = new MediaTrack.Builder(1, MediaTrack.TYPE_TEXT).setName("Chromecast Subtitle").setSubtype(MediaTrack.SUBTYPE_SUBTITLES)
					.setContentId("http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + subname).build();
			tracks.add(subtitleTrack);
		}

		MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
		mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);

		MediaInfo mediaInfo = new MediaInfo.Builder("http://" + ip + ":" + CastServerService.SERVER_PORT + "/" + filename).setContentType("video/mp4")
				.setStreamType(MediaInfo.STREAM_TYPE_BUFFERED).setMetadata(mediaMetadata).setMediaTracks(tracks).build();

		try {
			mRemoteMediaPlayer.load(mApiClient, mediaInfo, true, playPosition).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
				@Override
				public void onResult(final MediaChannelResult result) {
					if (result.getStatus().isSuccess()) {
						mRemoteMediaPlayer.setTextTrackStyle(mApiClient, trackStyle);
						mPopcornListener.onCastMediaLoadSuccess();
					} else {
						mPopcornListener.onCastMediaLoadCancelInterrupt();
						mRouter.selectRoute(mRouter.getDefaultRoute());
						teardown();
					}
				}
			});
		} catch (IllegalStateException e) {
			Log.e(TAG, "Problem occurred with media during loading", e);
		} catch (Exception e) {
			Log.e(TAG, "Problem opening media during loading", e);
		}
	}

	public void reloadMovie(final String mediaName, final String title, final String subPath) {
		final long time = mRemoteMediaPlayer.getApproximateStreamPosition();
		mRemoteMediaPlayer.stop(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

			@Override
			public void onResult(MediaChannelResult result) {
				if (result.getStatus().isSuccess()) {
					loadMovieMedia(mediaName, time, title, subPath);
				} else {
					mRouter.selectRoute(mRouter.getDefaultRoute());
					teardown();
				}
			}
		});

	}

	public void sendSubtitleJson(ArrayList<JSONArray> data) {
		mSubtitleSender.send(data);
	}

	public void sendSubtitleVtt(boolean enable) {
		long[] tracks = null;
		if (enable) {
			tracks = new long[] { 1 };
		} else {
			tracks = new long[0];
		}

		mRemoteMediaPlayer.setActiveMediaTracks(mApiClient, tracks).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

			@Override
			public void onResult(MediaChannelResult mediaChannelResult) {
				if (mediaChannelResult.getStatus().isSuccess()) {
					Log.d(TAG, "sendSubtitleVtt: isSuccess");
				} else {
					Log.e(TAG, "sendSubtitleVtt: error");
				}
			}
		});
		;
	}

	// WEB SERVER
	private void startCastServer(final String ip, final String rootDir) {
		Intent castServerService = new Intent(mActivity, CastServerService.class);
		castServerService.putExtra(CastServerService.IP_ADDRESS, ip);
		castServerService.putExtra(CastServerService.ROOT_DIR, rootDir);
		mActivity.startService(castServerService);
	}

	private void stopCastServer() {
		Intent castServerService = new Intent(mActivity, CastServerService.class);
		mActivity.stopService(castServerService);
	}
}
