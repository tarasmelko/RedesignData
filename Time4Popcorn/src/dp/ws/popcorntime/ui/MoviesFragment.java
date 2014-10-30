package dp.ws.popcorntime.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

import dp.ws.popcorntime.R;
import dp.ws.popcorntime.controller.MoviesGridAdapter;
import dp.ws.popcorntime.model.videodata.Movie;
import dp.ws.popcorntime.utils.Parser;
import dp.ws.popcorntime.utils.Preference;
import dp.ws.popcorntime.utils.WebRequest;

public class MoviesFragment extends Fragment {

	PullToRefreshGridView moviesGrid;
	List<Movie> mData;
	MoviesGridAdapter adapter;
	View mView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.movies_fragment, container, false);

		moviesGrid = (PullToRefreshGridView) mView
				.findViewById(R.id.fragment_movie_gv);

		mData = new ArrayList<Movie>();
		moviesGrid.setMode(PullToRefreshBase.Mode.BOTH);
		moviesGrid.setOnRefreshListener(new OnRefreshListener<GridView>() {

			@Override
			public void onRefresh(PullToRefreshBase<GridView> refreshView) {
				getDataAgain();
			}
		});

		getDataAgain();
		return mView;
	}

	public void showVideoProgress() {
		mView.findViewById(R.id.main_shadow_iv).setVisibility(View.VISIBLE);
		mView.findViewById(R.id.main_pb).setVisibility(View.VISIBLE);
	}

	public void stopVideoProgress() {
		mView.findViewById(R.id.main_shadow_iv).setVisibility(View.GONE);
		mView.findViewById(R.id.main_pb).setVisibility(View.GONE);
	}

	private void getDataAgain() {
		showVideoProgress();

		WebRequest request = new WebRequest(getActivity());
		request.loginWithEmail(Preference.getUserEmail(),
				Preference.getUserPassword(),
				new com.android.volley.Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						moviesGrid.onRefreshComplete();
						stopVideoProgress();
						if (response != null) {
							Preference.saveUserFilms(response);
							try {
								mData = Parser.feeds(response);
							} catch (Exception e) {
								Toast.makeText(getActivity(), "Server error",
										Toast.LENGTH_SHORT).show();
							}
							adapter = new MoviesGridAdapter(getActivity(),
									mData);
							moviesGrid.setAdapter(adapter);
						}
					}
				}, new com.android.volley.Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						moviesGrid.onRefreshComplete();
						stopVideoProgress();
					}
				});
	}
}
