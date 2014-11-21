package com.heliocratic.imovies.subtitles.format;


public abstract class Format {

	protected static boolean isNumber(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

}