package dp.ws.popcorntime.ui;

import java.util.HashMap;
import java.util.regex.Pattern;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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

public class LoginActivity extends Activity implements OnClickListener {

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

		initListeners();

	}

	private void initListeners() {
		findViewById(R.id.login_italy).setOnClickListener(LoginActivity.this);
		findViewById(R.id.login_usa).setOnClickListener(LoginActivity.this);
		findViewById(R.id.login_germany).setOnClickListener(LoginActivity.this);
		findViewById(R.id.login_france).setOnClickListener(LoginActivity.this);
		findViewById(R.id.login_india).setOnClickListener(LoginActivity.this);
		findViewById(R.id.login_china).setOnClickListener(LoginActivity.this);
		findViewById(R.id.login_spain).setOnClickListener(LoginActivity.this);

		findViewById(R.id.login_usa).setTag(R.id.login_usa, true);
		Preference.saveUSA(true);
		findViewById(R.id.login_italy).setTag(R.id.login_italy, false);
		findViewById(R.id.login_germany).setTag(R.id.login_germany, false);
		findViewById(R.id.login_france).setTag(R.id.login_france, false);
		findViewById(R.id.login_india).setTag(R.id.login_india, false);
		findViewById(R.id.login_china).setTag(R.id.login_china, false);
		findViewById(R.id.login_spain).setTag(R.id.login_spain, false);

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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_usa:
			if ((boolean) findViewById(R.id.login_usa).getTag(R.id.login_usa) == true) {
				findViewById(R.id.login_usa).setBackgroundColor(
						Color.TRANSPARENT);
				findViewById(R.id.login_usa).setTag(R.id.login_usa, false);
				Preference.saveUSA(false);
			} else {
				findViewById(R.id.login_usa).setBackground(
						getResources().getDrawable(
								R.drawable.drawer_switch_selected_selector));
				findViewById(R.id.login_usa).setTag(R.id.login_usa, true);
				Preference.saveUSA(true);
			}

			break;
		case R.id.login_italy:
			if ((boolean) findViewById(R.id.login_italy).getTag(
					R.id.login_italy) == true) {
				findViewById(R.id.login_italy).setBackgroundColor(
						Color.TRANSPARENT);
				findViewById(R.id.login_italy).setTag(R.id.login_italy, false);
				Preference.saveItaly(false);
			} else {
				findViewById(R.id.login_italy).setBackground(
						getResources().getDrawable(
								R.drawable.drawer_switch_selected_selector));
				findViewById(R.id.login_italy).setTag(R.id.login_italy, true);
				Preference.saveItaly(true);
			}

			break;

		case R.id.login_germany:
			if ((boolean) findViewById(R.id.login_germany).getTag(
					R.id.login_germany) == true) {
				findViewById(R.id.login_germany).setBackgroundColor(
						Color.TRANSPARENT);
				findViewById(R.id.login_germany).setTag(R.id.login_germany,
						false);
				Preference.saveGermany(false);
			} else {
				findViewById(R.id.login_germany).setBackground(
						getResources().getDrawable(
								R.drawable.drawer_switch_selected_selector));
				findViewById(R.id.login_germany).setTag(R.id.login_germany,
						true);
				Preference.saveGermany(true);
			}

			break;
		case R.id.login_france:
			if ((boolean) findViewById(R.id.login_france).getTag(
					R.id.login_france) == true) {
				findViewById(R.id.login_france).setBackgroundColor(
						Color.TRANSPARENT);
				findViewById(R.id.login_france)
						.setTag(R.id.login_france, false);
				Preference.saveFrance(false);
			} else {
				findViewById(R.id.login_france).setBackground(
						getResources().getDrawable(
								R.drawable.drawer_switch_selected_selector));
				findViewById(R.id.login_france).setTag(R.id.login_france, true);
				Preference.saveFrance(true);
			}

			break;
		case R.id.login_spain:
			if ((boolean) findViewById(R.id.login_spain).getTag(
					R.id.login_spain) == true) {
				findViewById(R.id.login_spain).setBackgroundColor(
						Color.TRANSPARENT);
				findViewById(R.id.login_spain).setTag(R.id.login_spain, false);
				Preference.saveSpain(false);
			} else {
				findViewById(R.id.login_spain).setBackground(
						getResources().getDrawable(
								R.drawable.drawer_switch_selected_selector));
				findViewById(R.id.login_spain).setTag(R.id.login_spain, true);
				Preference.saveSpain(true);
			}

			break;
		case R.id.login_china:
			if ((boolean) findViewById(R.id.login_china).getTag(
					R.id.login_china) == true) {
				findViewById(R.id.login_china).setBackgroundColor(
						Color.TRANSPARENT);
				findViewById(R.id.login_china).setTag(R.id.login_china, false);
				Preference.saveChina(false);
			} else {
				findViewById(R.id.login_china).setBackground(
						getResources().getDrawable(
								R.drawable.drawer_switch_selected_selector));
				findViewById(R.id.login_china).setTag(R.id.login_china, true);
				Preference.saveChina(true);
			}

			break;
		case R.id.login_india:
			if ((boolean) findViewById(R.id.login_india).getTag(
					R.id.login_india) == true) {
				findViewById(R.id.login_india).setBackgroundColor(
						Color.TRANSPARENT);
				findViewById(R.id.login_india).setTag(R.id.login_india, false);
				Preference.saveIndia(false);
			} else {
				findViewById(R.id.login_india).setBackground(
						getResources().getDrawable(
								R.drawable.drawer_switch_selected_selector));
				findViewById(R.id.login_india).setTag(R.id.login_india, true);
				Preference.saveIndia(true);
			}

			break;
		}

	}
}
