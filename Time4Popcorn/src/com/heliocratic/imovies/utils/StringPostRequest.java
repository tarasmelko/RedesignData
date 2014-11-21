package com.heliocratic.imovies.utils;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class StringPostRequest extends StringRequest {
	private Map<String, String> mParams;
	int i;

	public StringPostRequest(int method, String url, Listener<String> listener,
			ErrorListener errorListener, Map<String, String> params) {
		super(method, url, listener, errorListener);
		mParams = new HashMap<String, String>();
		mParams = params;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mParams;
	}
}
