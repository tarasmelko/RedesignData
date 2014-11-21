package com.heliocratic.imovies.ui.base;

public interface ContentLoadListener {
	public void showLoading();

	public void showError();

	public void showContent();

	public void retryLoad();
}