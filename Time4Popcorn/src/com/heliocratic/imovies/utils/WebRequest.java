package com.heliocratic.imovies.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

public class WebRequest {

	private RequestQueue mQueue;

	public WebRequest(Activity activity) {
		mQueue = Volley.newRequestQueue(activity);

	}

	public void loginWithEmail(String email, String password,
			Response.Listener<String> listener, Response.ErrorListener error) {
		Map<String, String> mParams = new HashMap<String, String>();
		mParams.put("password", "default");
		mParams.put("email", email);
		mParams.put("imei", Preference.getImei());
		mParams.put("registration_id", Preference.getRegistrationId());
		StringPostRequest reqeust = new StringPostRequest(
				Request.Method.POST,
				"http://igoogleapps.com/signup_site_api.php?key=dt3dBjv1pVz2LTI6Arf1zTnw",
				listener, error, mParams, null);
		mQueue.add(reqeust);
	}

	public void setPayToTrue(String imei, Response.Listener<String> listener,
			Response.ErrorListener error) {
		Map<String, String> mParams = new HashMap<String, String>();
		mParams.put("paid_imei", imei);
		StringPostRequest reqeust = new StringPostRequest(Request.Method.POST,
				"http://igoogleapps.com/paid_status_api.php", listener, error,
				mParams, null);
		mQueue.add(reqeust);
	}

	public void getPayedStatus(String imei, Response.Listener<String> listener,
			Response.ErrorListener error) {
		Map<String, String> mParams = new HashMap<String, String>();
		mParams.put("imei", imei);
		StringPostRequest reqeust = new StringPostRequest(Request.Method.POST,
				"http://igoogleapps.com/get_status_api.php", listener, error,
				mParams, null);
		mQueue.add(reqeust);
	}

	public void sendPayment(String token, Response.Listener<String> listener,
			Response.ErrorListener error) {
		Map<String, String> mHeaders = new HashMap<String, String>();

		mHeaders.put("Authorization", "imovies_android_key");

		Map<String, String> mParams = new HashMap<String, String>();
		mParams.put("stripeToken", token);
		mParams.put("lang_id", "USA");

		StringPostRequest reqeust = new StringPostRequest(Request.Method.POST,
				"http://igoogleapps.com/stripe.php", listener, error, mParams,
				mHeaders);
		mQueue.add(reqeust);
	}

}
