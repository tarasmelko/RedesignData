package com.heliocratic.imovies.torrent;

public interface VideoTaskCallbacks {
	
	public void onVideoPreExecute();
	
	public void onVideoPostExecute(VideoResult result);
	
	public void onVideoProgressUpdate(int progress, String status);
}