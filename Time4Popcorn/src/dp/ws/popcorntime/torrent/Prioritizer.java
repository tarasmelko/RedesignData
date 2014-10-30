package dp.ws.popcorntime.torrent;

import android.os.Handler;
import android.util.Log;

import com.softwarrior.libtorrent.LibTorrent;
import com.softwarrior.libtorrent.Priority;

import dp.ws.popcorntime.utils.StorageHelper;

public class Prioritizer {

	private final int ACTIVE_PIECE_COUNT = 5;
	private final int PREPARE_PIECE_COUNT = 3;
	private final int UPDATE_TIME = 500;

	private LibTorrent libTorrent;
	private String contentName;
	private int pieceIndex;
	private int firstPieceIndex;
	private int lastPieceIndex;
	private Handler handler;
	private boolean isHaveAllPieces;

	private boolean isStart;
	private int cPreparePieceCount;

	public Prioritizer(LibTorrent libTorrent) {
		this.libTorrent = libTorrent;
		handler = new Handler();
	}

	/**
	 * Return prepare MB
	 * */
	public int load(String contentName, int firstPieceIndex, int lastPieceIndex) {
		this.contentName = contentName;
		this.pieceIndex = firstPieceIndex;
		this.firstPieceIndex = firstPieceIndex;
		this.lastPieceIndex = lastPieceIndex;

		isStart = false;
		cPreparePieceCount = PREPARE_PIECE_COUNT;

		int[] priorities = libTorrent.GetPiecePriorities(contentName);
		for (int i = 0; i < cPreparePieceCount; i++) {
			priorities[lastPieceIndex - i] = Priority.MAXIMAL;
		}
		for (int i = 0; i < cPreparePieceCount + 2; i++) {
			priorities[firstPieceIndex + i] = Priority.NORMAL;
		}
		libTorrent.SetPiecePriorities(contentName, priorities);

		return (int) (2 * cPreparePieceCount * libTorrent.GetPieceSize(contentName, firstPieceIndex) / StorageHelper.SIZE_MB);
	}

	public void reload() {
		cancel();
		isStart = false;

		int[] priorities = libTorrent.GetPiecePriorities(contentName);
		if (priorities != null && priorities.length > 0) {
			priorities[lastPieceIndex - cPreparePieceCount] = Priority.MAXIMAL;
			priorities[firstPieceIndex + cPreparePieceCount] = Priority.NORMAL;
			libTorrent.SetPiecePriorities(contentName, priorities);
			cPreparePieceCount += 1;
		}
	}

	public boolean isPrepare() {
		for (int i = 0; i < cPreparePieceCount; i++) {
			if (!libTorrent.HavePiece(contentName, firstPieceIndex + i)) {
				return false;
			}
			if (!libTorrent.HavePiece(contentName, lastPieceIndex - i)) {
				return false;
			}
		}
		return true;
	}

	public void checkToStart() {
		if (isStart) {
			return;
		}

		for (int i = 0; i < cPreparePieceCount; i++) {
			if (!libTorrent.HavePiece(contentName, lastPieceIndex - i)) {
				return;
			}
		}

		isStart = true;
		Log.d("tag", "Start prioritizer!");

		updater.run();
	}

	public void cancel() {
		handler.removeCallbacks(updater);
	}

	public void seekTo(int pieceIndex) {
		this.pieceIndex = pieceIndex;
		updatePiecePriority();
	}

	private synchronized void updatePiecePriority() {
		int[] priorities = libTorrent.GetPiecePriorities(contentName);
		if (priorities != null && priorities.length > 0) {
			isHaveAllPieces = true;
			int count = 0;
			int newIndex = -1;
			// String zzz = "| ";
			for (int i = pieceIndex; i <= lastPieceIndex; i++) {
				if (!libTorrent.HavePiece(contentName, i)) {
					isHaveAllPieces = false;
					if (newIndex == -1) {
						newIndex = i;
					}
					if (Priority.DONT_DOWNLOAD == priorities[i]) {
						priorities[i] = Priority.MAXIMAL;
					}
					// zzz += i + " | ";
					count++;
				}

				if (count >= ACTIVE_PIECE_COUNT) {
					break;
				}
			}
			// Log.d("tag", zzz);
			if (newIndex != -1) {
				pieceIndex = newIndex;
			}
			libTorrent.SetPiecePriorities(contentName, priorities);
		}
	}

	private Runnable updater = new Runnable() {

		@Override
		public void run() {
			updatePiecePriority();
			if (isHaveAllPieces == false) {
				handler.postDelayed(updater, UPDATE_TIME);
			}
		}
	};
}