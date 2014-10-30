package dp.ws.popcorntime.ui;

import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.utils.Preference;
import dp.ws.popcorntime.utils.WebRequest;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setTheme(R.style.Theme_Popcorn_Classic);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_activity);

		String possibleEmail = "";
		Pattern emailPattern = Patterns.EMAIL_ADDRESS;
		Account[] accounts = AccountManager.get(this).getAccounts();
		for (Account account : accounts) {
			if (emailPattern.matcher(account.name).matches()) {
				possibleEmail = account.name;
			}
		}

		Button loginEmail = (Button) findViewById(R.id.login_button);
		final EditText email = (EditText) findViewById(R.id.email);
		final EditText password = (EditText) findViewById(R.id.password);
		if (possibleEmail != null)
			email.setText(possibleEmail);
		loginEmail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (TextUtils.isEmpty(email.getText().toString())
						|| TextUtils.isEmpty(password.getText().toString())) {
					Toast.makeText(LoginActivity.this, "Input your data",
							Toast.LENGTH_LONG).show();
					return;
				} else {
					if (!isValidEmail(email.getText().toString())) {
						Toast.makeText(LoginActivity.this, "Wrong email",
								Toast.LENGTH_LONG).show();
					} else {
						loginWithEmail(email.getText().toString(), password
								.getText().toString());
					}
				}

			}
		});

	}

	public final static boolean isValidEmail(CharSequence target) {
		return !TextUtils.isEmpty(target)
				&& android.util.Patterns.EMAIL_ADDRESS.matcher(target)
						.matches();
	}

	private void loginWithEmail(final String email, final String password) {
		WebRequest request = new WebRequest(this);
		showProgress();
		request.loginWithEmail(email, password,
				new com.android.volley.Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						stopProgress();
						Log.e("RESPONSE", "resp" + response.toString());
						if (response != null) {
							saveData();
							Preference.saveUserEmail(email);
							Preference.saveUserPassword(password);
						}
					}

				}, new com.android.volley.Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("RESPONSE", "error" + error.toString());
						stopProgress();
					}
				});
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	private void saveData() {
		Preference.saveUserRegistered("true");
		Intent intent = new Intent(LoginActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	public void showProgress() {
		findViewById(R.id.login_shadow_iv).setVisibility(View.VISIBLE);
		findViewById(R.id.login_pb).setVisibility(View.VISIBLE);
	}

	public void stopProgress() {
		findViewById(R.id.login_shadow_iv).setVisibility(View.GONE);
		findViewById(R.id.login_pb).setVisibility(View.GONE);
	}

}
