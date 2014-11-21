package com.heliocratic.imovies.utils;

import com.heliocratic.imovies.PopcornApplication;

import android.app.Activity;
import android.content.SharedPreferences;

public class Preference {

	// private static final String TAG = "Preference";
	private static final String PREF = "igoogle";
	private static final String USER_ID = "user_id";
	private static final String USER_NAME = "name";
	private static final String USER_PIC = "pic";
	private static final String USER_EMAIL = "email";
	private static final String USER_GENDER = "gender";
	private static final String USER_LASTNAME = "lastname";
	private static final String REG_ID = "reg_id";
	private static final String PASS = "pass";
	private static final String TIME = "time";
	private static final String FIRST_TIME = "first_time";
	private static final String COUNTRY_SETTINGS = "country";

	private static final String FLAG = "flag";

	private static SharedPreferences sharedPreferences = null;

	public static SharedPreferences getSharedPreferences() {
		if (sharedPreferences == null) {
			sharedPreferences = PopcornApplication.instance()
					.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
		}
		return sharedPreferences;
	}

	public synchronized static void saveCountries(String data) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(COUNTRY_SETTINGS, data);
		editor.commit();
	}

	public synchronized static String getCountries() {
		return getSharedPreferences().getString(COUNTRY_SETTINGS, "");
	}

	public synchronized static void saveUserFilms(String films) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString("FILMS", films);
		editor.commit();
	}

	public synchronized static String getUserFilms() {
		return getSharedPreferences().getString("FILMS", "");
	}

	public synchronized static void saveFlag(String flag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(FLAG, flag);
		editor.commit();
	}

	public synchronized static String getFlag() {
		return getSharedPreferences().getString(FLAG, "1");
	}

	public synchronized static void saveUserPassword(String userId) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(PASS, userId);
		editor.commit();
	}

	public synchronized static String getUserPassword() {
		return getSharedPreferences().getString(PASS, "");
	}

	public synchronized static void saveUserEmail(String email) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(USER_EMAIL, email);
		editor.commit();
	}

	public synchronized static String getUserEmail() {
		return getSharedPreferences().getString(USER_EMAIL, "");
	}

	public synchronized static void saveTime(long time) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putLong(TIME, time);
		editor.commit();
	}

	public synchronized static long getTime() {
		return getSharedPreferences().getLong(TIME, 0);
	}

	public synchronized static void saveFTime(long time) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putLong(FIRST_TIME, time);
		editor.commit();
	}

	public synchronized static long getFTime() {
		return getSharedPreferences().getLong(FIRST_TIME, 0);
	}

	public synchronized static void saveUserRegistrationId(String userId) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(REG_ID, userId);
		editor.commit();
	}

	public synchronized static String getRegistrationId() {
		return getSharedPreferences().getString(REG_ID, "");
	}

	public synchronized static void saveUserRegistered(String userId) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString(USER_ID, userId);
		editor.commit();
	}

	public synchronized static String getUserRegistered() {
		return getSharedPreferences().getString(USER_ID, "");
	}

	public synchronized static void saveUserPaypal(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean(USER_PIC, tag);
		editor.commit();
	}

	public synchronized static boolean getUserPaypal() {
		return getSharedPreferences().getBoolean(USER_PIC, false);
	}

	// usa
	public synchronized static void saveUSA(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean("USA", tag);
		editor.commit();
	}

	public synchronized static boolean getUSA() {
		return getSharedPreferences().getBoolean("USA", false);
	}

	// italy
	public synchronized static void saveItaly(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean("ITALY", tag);
		editor.commit();
	}

	public synchronized static boolean getItaly() {
		return getSharedPreferences().getBoolean("ITALY", false);
	}

	// germany
	public synchronized static void saveGermany(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean("GERMANY", tag);
		editor.commit();
	}

	public synchronized static boolean getGermany() {
		return getSharedPreferences().getBoolean("GERMANY", false);
	}

	// india
	public synchronized static void saveIndia(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean("INDIA", tag);
		editor.commit();
	}

	public synchronized static boolean getIndia() {
		return getSharedPreferences().getBoolean("INDIA", false);
	}

	// spain
	public synchronized static void saveSpain(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean("SPAIN", tag);
		editor.commit();
	}

	public synchronized static boolean getSpain() {
		return getSharedPreferences().getBoolean("SPAIN", false);
	}

	// china
	public synchronized static void saveChina(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean("CHINA", tag);
		editor.commit();
	}

	public synchronized static boolean getChina() {
		return getSharedPreferences().getBoolean("CHINA", false);
	}

	// france
	public synchronized static void saveFrance(boolean tag) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putBoolean("FRANCE", tag);
		editor.commit();
	}

	public synchronized static boolean getFrance() {
		return getSharedPreferences().getBoolean("FRANCE", false);
	}
}
