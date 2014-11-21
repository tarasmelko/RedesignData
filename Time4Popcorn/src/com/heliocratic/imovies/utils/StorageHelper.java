package com.heliocratic.imovies.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.heliocratic.imovies.IMoviesApplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;

public class StorageHelper {

	private static final StorageHelper INSTANCE = new StorageHelper();

	public static final String CHACHE_FOLDER_NAME = "imovies";

	public final static long SIZE_KB = 1024L;
	public final static long SIZE_MB = SIZE_KB * SIZE_KB;
	public final static long SIZE_GB = SIZE_KB * SIZE_KB * SIZE_KB;

	private final String CHACHE_FOLDER_PATH = "chache-folder-path";

	private File chacheDirectory = null;

	private StorageHelper() {

	}

	public static StorageHelper getInstance() {
		return INSTANCE;
	}

	/*
	 * Static methods
	 */

	public static void clearDirectory(File parent) {
		if (parent != null && parent.isDirectory()) {
			try {
				FileUtils.cleanDirectory(parent);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getDownloadFolderPath() {
		File file = null;
		if (Environment.getExternalStorageState() != null) {
			file = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		} else {
			file = new File(Environment.getDataDirectory() + "/Download/");
		}

		if (!file.exists()) {
			file.mkdir();
		}

		return file.getAbsolutePath();
	}

	public static void deleteRecursive(File path, ExtGenericFilter filter) {
		if (path.exists() && path.isDirectory()) {
			if (filter != null) {
				for (File f : path.listFiles(filter)) {
					deleteRecursive(f, null);
				}
			} else {
				for (File f : path.listFiles()) {
					deleteRecursive(f, null);
				}
			}
		}
		path.delete();
	}

	@SuppressWarnings("deprecation")
	public static long getAvailableSpaceInBytes(String path) {
		long availableSpace = -1L;
		try {
			StatFs stat = new StatFs(path);
			availableSpace = (long) stat.getAvailableBlocks()
					* (long) stat.getBlockSize();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return availableSpace;
	}

	public static long getAvailableSpaceInKB(String path) {
		return getAvailableSpaceInBytes(path) / SIZE_KB;
	}

	public static long getAvailableSpaceInMB(String path) {
		return getAvailableSpaceInBytes(path) / SIZE_MB;
	}

	public static long getAvailableSpaceInGB(String path) {
		return getAvailableSpaceInBytes(path) / SIZE_GB;
	}

	public static String getSizeText(long size) {
		String text;
		if (size >= SIZE_GB) {
			text = String.format("%.2f", ((float) size / SIZE_GB)) + " GB";
		} else if (size >= SIZE_MB) {
			text = String.format("%.2f", ((float) size / SIZE_MB)) + " MB";
		} else if (size >= SIZE_KB) {
			text = String.format("%.2f", ((float) size / SIZE_KB)) + " KB";
		} else {
			text = size + " B";
		}

		return text;
	}

	/*
	 * Methods
	 */

	public void init(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(
				IMoviesApplication.POPCORN_PREFERENCES, Activity.MODE_PRIVATE);
		String path = preferences.getString(CHACHE_FOLDER_PATH, "");
		if ("".equals(path)) {
			setChacheDirectory(context, getDefaultChacheFolder(context));
			clearChacheDirectory();
		} else {
			setChacheDirectory(context, path);
		}
	}

	public File getChacheDirectory() {
		return chacheDirectory;
	}

	public String getChacheDirectoryPath() {
		return chacheDirectory.getAbsolutePath();
	}

	public void setChacheDirectory(Context context, String path) {
		setChacheDirectory(context, new File(path));
	}

	public void setChacheDirectory(Context context, File directory) {
		if (chacheDirectory != directory) {
			if (chacheDirectory != null && chacheDirectory.exists()) {
				try {
					FileUtils.deleteDirectory(chacheDirectory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			chacheDirectory = directory;
			if (!chacheDirectory.exists()) {
				chacheDirectory.mkdirs();
			}

			SharedPreferences preferences = context.getSharedPreferences(
					IMoviesApplication.POPCORN_PREFERENCES,
					Activity.MODE_PRIVATE);
			preferences
					.edit()
					.putString(CHACHE_FOLDER_PATH,
							chacheDirectory.getAbsolutePath()).commit();
		}
	}

	public void clearChacheDirectory() {
		clearDirectory(chacheDirectory);
	}

	private File getDefaultChacheFolder(Context context) {
		String tempFolderPath = "";

		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			tempFolderPath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			tempFolderPath = context.getExternalCacheDir().getAbsolutePath();
		}

		tempFolderPath += "/" + CHACHE_FOLDER_NAME;

		return new File(tempFolderPath);
	}
}