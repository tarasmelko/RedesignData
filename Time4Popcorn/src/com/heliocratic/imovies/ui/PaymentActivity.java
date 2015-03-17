package com.heliocratic.imovies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.heliocratic.imovies.R;
import com.heliocratic.imovies.utils.ErrorDialogFragment;
import com.heliocratic.imovies.utils.PaymentForm;
import com.heliocratic.imovies.utils.ProgressDialogFragment;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

public class PaymentActivity extends FragmentActivity {

	/*
	 * Change this to your publishable key.
	 * 
	 * You can get your key here: https://manage.stripe.com/account/apikeys
	 */
	public static final String PUBLISHABLE_KEY = "pk_live_9nOSEWInpNZmFZ8VaW7SSI1c";

	private ProgressDialogFragment progressFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.payment_activity);

		progressFragment = ProgressDialogFragment
				.newInstance(R.string.progressMessage);
	}

	public void saveCreditCard(PaymentForm form) {

		Card card = new Card(form.getCardNumber(), form.getExpMonth(),
				form.getExpYear(), form.getCvc());

		boolean validation = card.validateCard();
		if (validation) {
			startProgress();
			new Stripe().createToken(card, PUBLISHABLE_KEY,
					new TokenCallback() {
						public void onSuccess(Token token) {
							Intent data = new Intent();
							data.putExtra("TOKEN", token.getId());
							setResult(RESULT_OK, data);
							finishProgress();
							finish();
						}

						public void onError(Exception error) {
							handleError(error.getLocalizedMessage());
							finishProgress();
						}
					});
		} else if (!card.validateNumber()) {
			handleError("The card number that you entered is invalid");
		} else if (!card.validateExpiryDate()) {
			handleError("The expiration date that you entered is invalid");
		} else if (!card.validateCVC()) {
			handleError("The CVC code that you entered is invalid");
		} else {
			handleError("The card details that you entered are invalid");
		}
	}

	private void startProgress() {
		progressFragment.show(getSupportFragmentManager(), "progress");
	}

	private void finishProgress() {
		progressFragment.dismiss();
	}

	private void handleError(String error) {
		DialogFragment fragment = ErrorDialogFragment.newInstance(
				R.string.validationErrors, error);
		fragment.show(getSupportFragmentManager(), "error");
	}

}
