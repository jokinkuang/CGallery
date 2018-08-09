package com.jokin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.jokin.widget.gallery.CGalleryAdapterView;
import com.jokin.widget.gallery.FancyGallery;
import com.jokin.widget.gallery.ListLooper;
import com.jokin.widget.gallery.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private Context mContext;
    private DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_main);

        initImageLoader(getApplicationContext());
        initGallery();
    }

    public void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().clearDiscCache();
        ImageLoader.getInstance().clearMemoryCache();
        // 配置option
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.img_stub)
                .showImageForEmptyUri(R.drawable.img_stub)
                .showImageOnFail(R.drawable.img_stub).cacheInMemory(true)
                .cacheOnDisc(true).bitmapConfig(Bitmap.Config.ARGB_8888).build();
    }

    //////////////////////////////////////

    private FancyGallery mFancyGallery;
    private ImageAdapter mAdapter;

    public void initGallery() {
        mAdapter = new ImageAdapter(this, ListInfo.getfilmInfo(), options, ImageLoader.getInstance());
        mFancyGallery = (FancyGallery) findViewById(R.id.fancyCoverFlow);

        // item之间的间隙可以近似认为是imageview的宽度与缩放比例的乘积的一半
        mFancyGallery.setSpacing(-120);
        mFancyGallery.setUnselectedScale(0.55f);
        mFancyGallery.setUnselectedAlpha(0.55f);
        mFancyGallery.setAdapter(mAdapter);
        loopMode();

        mFancyGallery.setOnMoveListener(new CGalleryAdapterView.OnMoveListener() {
            @Override
            public void onMoveFinish(CGalleryAdapterView<?> parent, View view, int position, long id) {
            }
        });

        mFancyGallery.setOnItemSelectedListener(new CGalleryAdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(CGalleryAdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(CGalleryAdapterView<?> parent) {

            }
        });

        // 点击事件
        mFancyGallery.setOnItemClickListener(new CGalleryAdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(CGalleryAdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(mContext, "Click with pos:"+position+",id:"+id, Toast.LENGTH_SHORT).show();;
            }
        });

        findViewById(R.id.startBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFancyGallery.startFling(-100);
            }
        });

        findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFancyGallery.stop();
            }
        });

        findViewById(R.id.loopModeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loopMode();
            }
        });

        findViewById(R.id.normalModeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               normalMode();
            }
        });

        findViewById(R.id.disableTouchBtn).setOnClickListener(new View.OnClickListener() {
            private boolean enable = true;
            @Override
            public void onClick(View v) {
                if (enable) {
                    mFancyGallery.disableTouch();
                    enable = false;
                } else {
                    mFancyGallery.enableTouch();
                    enable = true;
                }
            }
        });

        findViewById(R.id.textBtn).setOnClickListener(new View.OnClickListener() {
            private boolean enable;
            @Override
            public void onClick(View v) {
                enable = ! enable;
                mAdapter.setTextEnable(enable);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loopMode() {
        mAdapter.setStyle(ListLooper.StyleLoop);
        mAdapter.notifyDataSetChanged();
        mFancyGallery.setSelection(mAdapter.getCount()/2);

    }

    private void normalMode() {
        mAdapter.setStyle(ListLooper.StyleNormal);
        mAdapter.notifyDataSetChanged();
        mFancyGallery.setSelection(mAdapter.getCount()/2);
    }

    ///////////////////////////////

    private List<Integer> getDrawableList() {
        List<Integer> drawables = new ArrayList<>();
        for (int i = 1; i < 8; ++i) {
            drawables.add(getDrawableId("img"+i));
        }
        return drawables;
    }

    private int getDrawableId(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}
