package com.heliocratic.imovies.utils;

public interface PaymentForm {
	public String getCardNumber();

	public String getCvc();

	public Integer getExpMonth();

	public Integer getExpYear();
}
