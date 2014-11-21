package com.heliocratic.imovies.model.videoinfo.tvshow;

import java.util.ArrayList;

import com.heliocratic.imovies.model.videoinfo.Torrent;

import android.os.Parcel;
import android.os.Parcelable;

public class Episode implements Parcelable {

	public String title;
	public String description;
	public ArrayList<Torrent> torrents = new ArrayList<Torrent>();

	public Episode() {

	}

	private Episode(Parcel parcel) {
		title = parcel.readString();
		description = parcel.readString();
		parcel.readTypedList(torrents, Torrent.CREATOR);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(description);
		dest.writeTypedList(torrents);
	}

	public static final Parcelable.Creator<Episode> CREATOR = new Parcelable.Creator<Episode>() {

		public Episode createFromParcel(Parcel in) {
			return new Episode(in);
		}

		public Episode[] newArray(int size) {
			return new Episode[size];
		}
	};
}