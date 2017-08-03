package com.cvte;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
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
		return filmList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return filmList.get(position);
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
			reusableView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_item, parent, false);
			reusableView.setLayoutParams(new EcoGallery.LayoutParams(180, 240));

			viewHolder = new ViewHolder();
			viewHolder.imageView = (ImageView) reusableView.findViewById(R.id.imgv);
			viewHolder.textView = (TextView) reusableView.findViewById(R.id.tv);
			reusableView.setTag(viewHolder);
		} else {
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