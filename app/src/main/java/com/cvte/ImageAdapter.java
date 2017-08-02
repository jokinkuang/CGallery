package com.cvte;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.cvte.widget.gallery.EcoGallery;
import com.cvte.widget.gallery.FancyCoverFlowAdapter;
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
		return filmList.size() * 2;
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

	@Override
	public View getCoverFlowItem(int position, View reusableView,
			ViewGroup parent) {
		ImageView imageView = (ImageView) reusableView;

		if (imageView == null) {
			imageView = new ImageView(context);
		}
		// ps.电影海报宽高比例一般为3：4
		imageView.setLayoutParams(new EcoGallery.LayoutParams(180, 240));
		// 异步加载图片
		imageLoader.displayImage(filmList.get(position % filmList.size()), imageView, options);
        imageView.setScaleType(ScaleType.CENTER_CROP);
		return imageView;


        // ImageView imageView = new ImageView(context);
        // imageView.setImageResource(pics[index % pics.length]);
        //
        // imageView.setLayoutParams(new Gallery.LayoutParams(W, H));
        // imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        // return imageView;
	}

}