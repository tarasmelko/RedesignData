package com.heliocratic.imovies.utils;

import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class StringPostRequest extends StringRequest {
	private Map<String, String> mParams;
	private Map<String, String> mHeaders;

	public StringPostRequest(int method, String url, Listener<String> listener,
			ErrorListener errorListener, Map<String, String> params,
			Map<String, String> headers) {
		super(method, url, listener, errorListener);
		mParams = new HashMap<String, String>();
		mParams = params;
		mHeaders = new HashMap<String, String>();
		if (headers != null)
			mHeaders = headers;
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return mParams;
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		// TODO Auto-generated method stub
		return mHeaders;
	}
}
