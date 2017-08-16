package com.cvte;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
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
		ImageView imageView = viewHolder.imageView;
		imageLoader.displayImage(filmList.get(position % filmList.size()), imageView, options);
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

}