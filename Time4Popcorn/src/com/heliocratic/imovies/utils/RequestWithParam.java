package com.heliocratic.imovies.utils;

import java.util.Map;

import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public class RequestWithParam extends JsonObjectRequest {

	public RequestWithParam(Map<String, String> headers,
			JSONObject requestBody, Map<String, String> params, int method,
			String url, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		super(method, url, requestBody, listener, errorListener);

	}

}
