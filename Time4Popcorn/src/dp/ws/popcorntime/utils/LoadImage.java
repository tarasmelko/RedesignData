package dp.ws.popcorntime.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import dp.ws.popcorntime.R;

public class LoadImage {

	ImageLoader mImageLoader;
	ExecutorService executorService;
	Context mContext;
	Builder mOptionsBuilderCacheAlpha;
	Builder mOptionsBuilderCacheRound;

	public ImageLoader getLoader() {
		return mImageLoader;
	}

	public LoadImage(Context context) {
		mContext = context;
		executorService = Executors.newFixedThreadPool(5);

		mOptionsBuilderCacheAlpha = new Builder()
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.resetViewBeforeLoading(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.showImageOnFail(R.drawable.ic_launcher)
				.considerExifParams(true).cacheInMemory(true).cacheOnDisk(true)
				.bitmapConfig(Bitmap.Config.RGB_565).cacheOnDisk(true);

		mOptionsBuilderCacheRound = new Builder()
				.showImageForEmptyUri(R.drawable.ic_launcher)
				.resetViewBeforeLoading(true)
				.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
				.showImageOnFail(R.drawable.ic_launcher).cacheOnDisk(true)
				.considerExifParams(true).cacheInMemory(true)
				.bitmapConfig(Bitmap.Config.RGB_565).cacheOnDisk(true);

		mImageLoader = ImageLoader.getInstance();
	}

	public void loadImageAlphaCache(String url, ImageView imageView) {
		mOptionsBuilderCacheAlpha.displayer(new FadeInBitmapDisplayer(800));
		mImageLoader.displayImage(url, imageView,
				mOptionsBuilderCacheAlpha.build());
	}

	public void loadImageRoundedCache(String url, ImageView imageView,
			int radius) {
		mOptionsBuilderCacheRound.displayer(new RoundedBitmapDisplayer(radius));
		mImageLoader.displayImage(url, imageView,
				mOptionsBuilderCacheRound.build());
	}

	public static Bitmap makePortraitBitmap(Bitmap bm) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		if (width > height) {
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			Bitmap rotated = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), matrix, true);

			return rotated;
		} else
			return bm;
	}

}
