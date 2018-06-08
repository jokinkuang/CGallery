package com.cvte;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cvte.widget.gallery.EcoGallery;
import com.cvte.widget.gallery.FancyCoverFlowAdapter;
import com.cvte.widget.gallery.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;

/**
 * @author LittleLiByte
 * 
 */
public class ImageAdapter extends FancyCoverFlowAdapter {
	private static final String TAG = "ImageAdapter";

	private Context context;
	private List<String> filmList;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public ImageAdapter(Context context, List<String> filmList,
			DisplayImageOptions options, ImageLoader imageLoader) {
		this.context = context;
		this.filmList = filmList;
		this.options = options;
		this.imageLoader = imageLoader;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return Integer.MAX_VALUE;	// 2147483648 够1w张照片循环了。
		// return filmList.size();
	}
	//
	// 中心作为0点：Integer.MAX_VALUE／2
	//

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return filmList.get(position % filmList.size());
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position % filmList.size();
	}

	public List<String> getData() {
        return filmList;
    }

    private static class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}

	@Override
	public View getCoverFlowItem(int position, View reusableView,
			ViewGroup parent) {

		ViewHolder viewHolder;
		if (reusableView == null) {
			Log.d(TAG, String.format("[MISS] position %d", position));
            reusableView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
			reusableView.setLayoutParams(new EcoGallery.LayoutParams(240, 240));

			viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView) reusableView.findViewById(R.id.imgv);
			viewHolder.textView = (TextView) reusableView.findViewById(R.id.tv);
			reusableView.setTag(viewHolder);
		} else {
			Log.d(TAG, String.format("[HIT] view %d position %d", reusableView.hashCode(), position));
			viewHolder = (ViewHolder) reusableView.getTag();
		}

		// ps.电影海报宽高比例一般为3：4
		// 异步加载图片
		final ImageView imageView = viewHolder.imageView;
		imageLoader.displayImage(filmList.get(position % filmList.size()), imageView, options, new ImageLoadingListener() {
			@Override
			public void onLoadingStarted(String s, View view) {

			}

			@Override
			public void onLoadingFailed(String s, View view, FailReason failReason) {

			}

			@Override
			public void onLoadingComplete(String s, View view, Bitmap bitmap) {
				Bitmap blurBitmap = Bitmap.createBitmap(bitmap);
				// imageView.setImageBitmap(blurBitmap(bitmap, blurBitmap, 0.0001f, context));
				blur(bitmap, imageView);
			}

			@Override
			public void onLoadingCancelled(String s, View view) {

			}
		});
		imageView.setScaleType(ScaleType.CENTER_CROP);

		TextView textView = viewHolder.textView;
		textView.setBackgroundColor(Color.BLUE);
		textView.setTextColor(Color.YELLOW);
		textView.setTextSize(18);
		textView.setText(String.valueOf(position));

		return reusableView;


        // ImageView imageView = new ImageView(context);
        // imageView.setImageResource(pics[index % pics.length]);
        //
        // imageView.setLayoutParams(new Gallery.LayoutParams(W, H));
        // imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        // return imageView;
	}



	/**
	 * 通过调用系统高斯模糊api的方法模糊
	 *
	 * @param bitmap    source bitmap
	 * @param outBitmap out bitmap
	 * @param radius    0 < radius <= 25
	 * @param context   context
	 * @return out bitmap
	 */
	public static Bitmap blurBitmap(Bitmap bitmap, Bitmap outBitmap, float radius, Context context) {
		//Let's create an empty bitmap with the same size of the bitmap we want to blur
		outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

		//Instantiate a new Renderscript
		RenderScript rs = RenderScript.create(context);

		//Create an Intrinsic Blur Script using the Renderscript
		ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

		//Create the Allocations (in/out) with the Renderscript and the in/out bitmaps
		Allocation allIn = Allocation.createFromBitmap(rs, bitmap);
		Allocation allOut = Allocation.createFromBitmap(rs, outBitmap);

		//Set the radius of the blur
		blurScript.setRadius(radius);

		//Perform the Renderscript
		blurScript.setInput(allIn);
		blurScript.forEach(allOut);

		//Copy the final bitmap created by the out Allocation to the outBitmap
		allOut.copyTo(outBitmap);

		//recycle the original bitmap
		//        bitmap.recycle();

		//After finishing everything, we destroy the Renderscript.
		rs.destroy();

		return outBitmap;
	}
	/**
	 * 模糊的具体实现
	 *
	 * @param inputBitmap 要模糊的 bitmap
	 * @param targetView 需要被模糊背景的 View
	 */
	public void blur(Bitmap inputBitmap, View targetView) {
		// 创建一个和目标View(需要背景被模糊的View)宽高一样的空的 outputBitmap
		Bitmap outputBitmap = Bitmap.createBitmap((int) (240),
				(int) (240), Bitmap.Config.ARGB_8888);
		// 将 outputBitmap 关联在 canvas 上
		Canvas canvas = new Canvas(outputBitmap);
		// 画布移动到目标 View 在父布局中的位置
		canvas.translate(-targetView.getLeft(), -targetView.getTop());
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		// 将要模糊的 inputBitmap 绘制到 outputBitmap 上
		// 因为刚才做了 translate 操作，这样就可以裁剪到目标 View 在父布局内的那一块背景到 outputBitmap 上
		canvas.drawBitmap(inputBitmap, 0, 0, paint);

		// ----接下来做模糊 outputBitmap 处理操作----

		// 创建 RenderScript
		RenderScript rs = RenderScript.create(context);
		Allocation input = Allocation.createFromBitmap(rs, outputBitmap);
		Allocation output = Allocation.createTyped(rs, input.getType());
		// 使用 ScriptIntrinsicBlur 类来模糊图片
		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
				rs, Element.U8_4(rs));
		// 设置模糊半径 ( 取值范围为( 0.0f , 25f ] ，半径越大，模糊效果也越大)
		blur.setRadius(25f);
		blur.setInput(input);
		// 模糊计算
		blur.forEach(output);
		// 模糊 outputBitmap
		output.copyTo(outputBitmap);
		// 将模糊后的 outputBitmap 设为目标 View 的背景
		targetView.setBackground(new BitmapDrawable(context.getResources(), outputBitmap));
		rs.destroy();
	}

	// 马赛克
	public static Bitmap mosaic(Bitmap bitmap, int radius) {
		if (radius == 0) return bitmap;
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
		final int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int x = j % radius;
				int y = i % radius;
				pixels[i * width + j] = pixels[(i - y) * width + j - x];
			}
		}
		outBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return outBitmap;
	}

	//
	public static Bitmap boxBlur(Bitmap bitmap, int radius) {
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		final int[] pixels = new int[width * height];
		final int[] outPixels = new int[width * height];
		final Bitmap outBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		//遍历bitmap每一个像素
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				//取半径为radius的矩形区域，并处理边界情况
				final int left = i - radius < 0 ? 0 : i - radius;
				final int top = j - radius < 0 ? 0 : j - radius;
				final int right = i + radius > width ? width : i + radius;
				final int bottom = j + radius > height ? height : j + radius;

				//矩形区域总像素
				final int count = (right - left) * (bottom - top);

				//分别求出矩形区域内rgb的总值
				int r = 0, g = 0, b = 0;
				for (int m = left; m < right; m++) {
					for (int n = top; n < bottom; n++) {
						final int pixel = pixels[n * width + m];
						r += Color.red(pixel);
						g += Color.green(pixel);
						b += Color.blue(pixel);
					}
				}
				//设置新的像素为矩形区域内像素的均值
				outPixels[j * width + i] = Color.rgb(r / count, g / count, b / count);
			}
		}
		outBitmap.setPixels(outPixels, 0, width, 0, 0, width, height);
		// bitmap.recycle();
		return outBitmap;
	}
}