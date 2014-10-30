package dp.ws.popcorntime.utils;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

public class WebRequest {

	public static final String ACTION_SIGNIN = "http://igoogleapps.com/signup_api.php?key=dt3dBjv1pVz2LTI6Arf1zTnw";

	private RequestQueue mQueue;

	public WebRequest(Activity activity) {
		mQueue = Volley.newRequestQueue(activity);

	}

	public void loginWithEmail(String email, String password,
			Response.Listener<String> listener, Response.ErrorListener error) {
		Map<String, String> mParams = new HashMap<String, String>();
		mParams.put("password", password);
		mParams.put("email", email);
		mParams.put("registration_id", Preference.getRegistrationId());
		StringPostRequest reqeust = new StringPostRequest(
				Request.Method.POST,
				"http://igoogleapps.com/signup_site_api.php?key=dt3dBjv1pVz2LTI6Arf1zTnw",
				listener, error, mParams);
		mQueue.add(reqeust);
	}

}
