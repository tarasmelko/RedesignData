package dp.ws.popcorntime.torrent;

import java.util.Locale;

import android.text.TextUtils;

public class TorrentUtils {

	public static long getSize(String sizeString) {
		if (TextUtils.isEmpty(sizeString)) {
			return 0;
		}
		sizeString = sizeString.toLowerCase(Locale.ENGLISH);

		int factor = 1;
		if (sizeString.endsWith("kb")) {
			sizeString = sizeString.substring(0, sizeString.length() - 2);
			factor = 1000;
		} else if (sizeString.endsWith("mb")) {
			sizeString = sizeString.substring(0, sizeString.length() - 2);
			factor = 1000000;
		} else if (sizeString.endsWith("gb")) {
			sizeString = sizeString.substring(0, sizeString.length() - 2);
			factor = 1000000000;
		} else if (sizeString.endsWith("b")) {
			sizeString = sizeString.substring(0, sizeString.length() - 1);
		}
		try {
			return (long) (Double.parseDouble(sizeString) * factor);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
