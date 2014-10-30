package dp.ws.popcorntime.torrent;

import java.io.File;

import org.apache.commons.io.FileUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.softwarrior.libtorrent.LibTorrent;
import com.softwarrior.libtorrent.Priority;
import com.softwarrior.libtorrent.ProxyType;
import com.softwarrior.libtorrent.StorageMode;
import com.softwarrior.libtorrent.TorrentState;

import dp.ws.popcorntime.PopcornApplication;
import dp.ws.popcorntime.config.Configuration;
import dp.ws.popcorntime.utils.ExtGenericFilter;
import dp.ws.popcorntime.utils.StorageHelper;

public class PopcornTorrent {

	private static final PopcornTorrent INSTANCE = new PopcornTorrent();

	public static final String IS_PROXY_ENABLE_KEY = "is-proxy-enable";
	private final String LAST_CONTENT_KEY = "last-content";
	private final String LAST_CONTENT_DIR_KEY = "last-content-dir";

	private Prioritizer prioritizer;

	private LibTorrent libTorrent;
	private Context mContext;
	private VideoTask mVideoTask;
	private String mContentName;
	private String mLocation;
	private long mFileSize;
	private int mFirstPieceIndex;
	private int mLastPieceIndex;

	private VideoTaskCallbacks mVideoTaskCallbacks;

	private PopcornTorrent() {
		libTorrent = new LibTorrent();
		libTorrent.SetSession(54321, 0, 0, true);
		prioritizer = new Prioritizer(libTorrent);
	}

	public static PopcornTorrent getInstance() {
		return INSTANCE;
	}

	public void setVideoTaskCallbacs(VideoTaskCallbacks callbacks) {
		mVideoTaskCallbacks = callbacks;
	}

	public void onCreate(Context context) {
		mContext = context;
		SharedPreferences preferences = context.getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		if (preferences.getBoolean(IS_PROXY_ENABLE_KEY, false)) {
			libTorrent.SetProxy(ProxyType.SOCKS_5_PW, Configuration.VPN.host, Configuration.VPN.port, Configuration.VPN.user, Configuration.VPN.pass);
		} else {
			libTorrent.SetProxy(ProxyType.NONE, "", 0, "", "");
		}
	}

	public void onDestroy() {
		try {
			if (mVideoTask != null) {
				mVideoTask.cancel(true);
			}
			prioritizer.cancel();
			if (mContentName != null && !"".equals(mContentName)) {
				libTorrent.RemoveTorrent(mContentName);
			}
			libTorrent.AbortSession();
		} catch (Exception ex) {
		}
	}

	public void loadVideo(String torrentFilePath, String fileName) {
		if (TextUtils.isEmpty(torrentFilePath) || !torrentFilePath.endsWith("torrent")) {
			return;
		}

		if (mVideoTask == null || AsyncTask.Status.FINISHED == mVideoTask.getStatus()) {
			mVideoTask = new VideoTask();
			mVideoTask.execute(VideoTask.LOAD, torrentFilePath, fileName);
		}
	}

	public void reloadVideo() {
		if (mVideoTask != null && AsyncTask.Status.FINISHED != mVideoTask.getStatus()) {
			mVideoTask.cancel(true);
		}
		mVideoTask = new VideoTask();
		mVideoTask.execute(VideoTask.RELOAD);
	}

	public String getContentName() {
		return mContentName;
	}

	public String getFileLocation() {
		return mLocation;
	}

	public long getFileSize() {
		return mFileSize;
	}

	public int getTorrentState() {
		return libTorrent.GetTorrentState(mContentName);
	}

	public long getProgressSize() {
		return libTorrent.GetTorrentProgressSize(mContentName);
	}

	public boolean seekToPosition(long length, long time) {
		try {
			if (TorrentState.DOWNLOADING == libTorrent.GetTorrentState(mContentName)) {
				long deltaTime = length / (mLastPieceIndex - mFirstPieceIndex + 1);
				int pieceIndex = mFirstPieceIndex + ((int) (time / deltaTime));

				prioritizer.seekTo(pieceIndex);
				if (!libTorrent.HavePiece(mContentName, pieceIndex)) {
					return false;
				}
			}
		} catch (Exception ex) {

		}

		return true;
	}

	private class VideoTask extends AsyncTask<String, Integer, VideoResult> {
		public static final String LOAD = "load";
		public static final String RELOAD = "reload";

		private int checkSizeMB;

		@Override
		protected void onPreExecute() {
			mVideoTaskCallbacks.onVideoPreExecute();
		}

		@Override
		protected VideoResult doInBackground(String... params) {
			if (LOAD == params[0]) {
				String torrentFilePath = params[1];
				if (torrentFilePath.startsWith("file://")) {
					torrentFilePath = torrentFilePath.substring(7);
				}
				boolean isAdded = libTorrent.AddTorrent(StorageHelper.getInstance().getChacheDirectoryPath(), torrentFilePath, StorageMode.ALLOCATE, false);
				if (!isAdded) {
					return VideoResult.TORRENT_NOT_ADDED;
				}
				mContentName = libTorrent.GetTorrentName(torrentFilePath);
				deletePreviousTorrent();
				setFilesPriorities(params[2]);
				if (mFileSize == -1) {
					return VideoResult.NO_VIDEO_FILE;
				}
				long freeSpace = StorageHelper.getAvailableSpaceInBytes(StorageHelper.getInstance().getChacheDirectoryPath());
				if (freeSpace < mFileSize) {
					return VideoResult.NO_FREE_SPACE;
				}
				setPiecePriorities();
				checkSizeMB = prioritizer.load(mContentName, mFirstPieceIndex, mLastPieceIndex);
			} else if (RELOAD == params[0]) {
				Log.w("tag", "RELOAD");
				prioritizer.reload();
			} else {
				return VideoResult.ERROR;
			}

			// load video file
			int progressSize = 0;
			int progressPercentage = 0;
			while (true) {
				prioritizer.checkToStart();

				progressSize = (int) libTorrent.GetTorrentProgressSize(mContentName);
				if (prioritizer.isPrepare()) {
					return VideoResult.SUCCESS;
				}

				progressPercentage = (int) (((double) progressSize / (double) checkSizeMB) * 100);
				if (progressPercentage <= 90) {
					publishProgress(progressPercentage);
				} else {
					publishProgress(((int) (90 + (0.03 * progressPercentage)))); // cheat
				}

				if (isCancelled()) {
					return VideoResult.CANCELED;
				}

				try {
					Thread.sleep(300);
				} catch (InterruptedException ie) {
					return VideoResult.CANCELED;
				}
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			int progress = values[0];
			if (progress >= 100) {
				progress = 99;
			}

			String peers = "0/0";
			String speed = "0kB/s";
			String info = libTorrent.GetTorrentStatusText(mContentName);
			if (info != null) {
				String[] fields = info.split("\n");
				for (int i = 0; i < fields.length; i++) {
					String[] key_value = fields[i].split(":");
					if (key_value.length == 2) {
						String key = key_value[0].toLowerCase().trim();
						String value = key_value[1].trim();
						if ("peers/cand".equals(key) && !"".equals(value)) {
							peers = value;
						} else if ("speed".equals(key) && !"".equals(value)) {
							speed = value;
						}
					}
				}

				mVideoTaskCallbacks.onVideoProgressUpdate(progress, peers + "\t\t\t" + speed + "\t\t\t" + progress + "%");
			}
		}

		@Override
		protected void onPostExecute(VideoResult result) {
			mVideoTaskCallbacks.onVideoPostExecute(result);
		}

	}

	private void deletePreviousTorrent() {
		SharedPreferences prefs = mContext.getSharedPreferences(PopcornApplication.POPCORN_PREFERENCES, Context.MODE_PRIVATE);
		String latestContent = prefs.getString(LAST_CONTENT_KEY, "");

		if (!latestContent.equals(mContentName)) {
			Editor e = prefs.edit();
			e.putString(LAST_CONTENT_KEY, mContentName);
			e.putString(LAST_CONTENT_DIR_KEY, StorageHelper.getInstance().getChacheDirectoryPath() + "/" + mContentName);

			String latestContentDir = prefs.getString(LAST_CONTENT_DIR_KEY, "");
			File last = new File(latestContentDir);
			if (last.isDirectory()) {
				try {
					FileUtils.deleteDirectory(last);
				} catch (Exception e2) {
				}
			} else {
				last.delete();
			}
			StorageHelper.deleteRecursive(StorageHelper.getInstance().getChacheDirectory(), new ExtGenericFilter(".resume"));

			e.commit();
		}
	}

	private void setFilesPriorities(String fileName) {
		mLocation = "";
		mFileSize = -1;

		String torrentFiles = libTorrent.GetTorrentFiles(mContentName);
		if (TextUtils.isEmpty(torrentFiles)) {
			return;
		}
		String[] files = torrentFiles.split("\\r?\\n");
		long[] sizes = new long[files.length];
		byte[] priorities = libTorrent.GetTorrentFilesPriority(mContentName);

		for (int i = 0; i < files.length; i++) {
			String file = files[i];
			int spaceIndex = file.lastIndexOf(" ");
			files[i] = file.substring(0, spaceIndex);
			String sizeString = file.substring(spaceIndex + 1);
			sizes[i] = TorrentUtils.getSize(sizeString);
		}

		if (fileName != null && !"".equals(fileName)) {
			for (int i = 0; i < files.length; i++) {
				int index = files[i].lastIndexOf("/") + 1;
				String currentFileName = files[i].substring(index);
				if (currentFileName.equals(fileName)) {
					priorities[i] = Priority.NORMAL;
					mFileSize = sizes[i];
					mLocation = "file://" + StorageHelper.getInstance().getChacheDirectoryPath() + "/" + files[i];
				} else {
					priorities[i] = Priority.DONT_DOWNLOAD;
				}
			}
		}

		if ("".equals(mLocation) && sizes.length > 0) {
			int biggestFileIndex = 0;
			for (int i = 0; i < sizes.length; i++) {
				if (sizes[i] > mFileSize) {
					priorities[biggestFileIndex] = Priority.DONT_DOWNLOAD;
					mFileSize = sizes[i];
					biggestFileIndex = i;
					priorities[biggestFileIndex] = Priority.NORMAL;
				} else {
					priorities[i] = Priority.DONT_DOWNLOAD;
				}
			}
			mLocation = "file://" + StorageHelper.getInstance().getChacheDirectoryPath() + "/" + files[biggestFileIndex];
		}

		libTorrent.SetTorrentFilesPriority(priorities, mContentName);
	}

	private void setPiecePriorities() {
		int[] piecePriorities = libTorrent.GetPiecePriorities(mContentName);
		mFirstPieceIndex = -1;
		mLastPieceIndex = -1;

		for (int i = 0; i < piecePriorities.length; i++) {
			if (piecePriorities[i] != Priority.DONT_DOWNLOAD) {
				if (mFirstPieceIndex == -1) {
					mFirstPieceIndex = i;
				}
				setPiecePriority(piecePriorities, i, Priority.DONT_DOWNLOAD);
			} else {
				if (mFirstPieceIndex != -1) {
					if (mLastPieceIndex == -1) {
						mLastPieceIndex = i - 1;
					}
				}
			}
		}

		if (mLastPieceIndex == -1) {
			mLastPieceIndex = piecePriorities.length - 1;
		}

		libTorrent.SetPiecePriorities(mContentName, piecePriorities);
	}

	private void setPiecePriority(int[] piecePriorities, int pieceIndex, int priority) {
		if (pieceIndex > -1 && pieceIndex < piecePriorities.length) {
			if (!libTorrent.HavePiece(mContentName, pieceIndex)) {
				piecePriorities[pieceIndex] = priority;
			}
		}
	}
}