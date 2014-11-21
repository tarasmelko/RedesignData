package com.heliocratic.imovies.ui;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.heliocratic.imovies.R;
import com.heliocratic.imovies.controller.FavoritesAdapter;
import com.heliocratic.imovies.database.tables.Favorites;
import com.heliocratic.imovies.ui.locale.LocaleFragment;

public class GridFavoritesFragment extends LocaleFragment implements LoaderCallbacks<Cursor> {

	private GridView videoGrid;
	private FavoritesAdapter mFavoritesAdapter;
	private String favoritesIsEmptyText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFavoritesAdapter = new FavoritesAdapter(getActivity(), null, false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_grid_video, container, false);
		populateView(view);
		getLoaderManager().initLoader(0, null, GridFavoritesFragment.this);
		return view;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		// change orientation
		ViewGroup container = (ViewGroup) getView();
		container.removeAllViewsInLayout();
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_grid_video, container);
		populateView(view);
	}

	private void populateView(View view) {
		videoGrid = (GridView) view.findViewById(R.id.videos_content_view);
		videoGrid.setAdapter(mFavoritesAdapter);

		updateLocaleText();
	}

	@Override
	public void updateLocaleText() {
		super.updateLocaleText();
		favoritesIsEmptyText = getString(R.string.favorites_is_empty);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Favorites.CONTENT_URI, null, null, null, Favorites._ID + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor == null || cursor.getCount() == 0) {
			Toast.makeText(getActivity(), favoritesIsEmptyText, Toast.LENGTH_SHORT).show();
		}
		mFavoritesAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mFavoritesAdapter.swapCursor(null);
	}
}
