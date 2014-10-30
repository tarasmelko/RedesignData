package dp.ws.popcorntime.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import dp.ws.popcorntime.PopcornApplication;

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
	private static SharedPreferences sharedPreferences = null;

	public static SharedPreferences getSharedPreferences() {
		if (sharedPreferences == null) {
			sharedPreferences = PopcornApplication.instance()
					.getSharedPreferences(PREF, Activity.MODE_PRIVATE);
		}
		return sharedPreferences;
	}

	public synchronized static void saveUserFilms(String films) {
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		editor.putString("FILMS", films);
		editor.commit();
	}

	public synchronized static String getUserFilms() {
		return getSharedPreferences().getString("FILMS", "");
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

	//
}
