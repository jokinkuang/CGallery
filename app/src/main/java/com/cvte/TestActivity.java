package com.cvte;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.cvte.widget.gallery.EcoGalleryAdapterView;
import com.cvte.widget.gallery.FancyCoverFlow;
import com.cvte.widget.gallery.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestActivity extends Activity {
    DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader(getApplicationContext());

        setContentView(R.layout.activity_test);
        init(savedInstanceState);
    }

    public void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you
        // may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                // .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);

        // 配置option
        options = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.img0)
                .showImageForEmptyUri(R.drawable.img0)
                .showImageOnFail(R.drawable.img0).cacheInMemory(true)
                .cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    private FancyCoverFlow fancyCoverFlow;
    private ImageAdapter adapter;


    private int cur_index = 0;
    private int count_drawble;
    private static int MSG_UPDATE = 1;
    // 定时任务
    private ScheduledExecutorService scheduledExecutorService;

    // 通过handler来更新主界面
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE) {
                // fancyCoverFlow.setSelection(cur_index, true);
                fancyCoverFlow.moveToNext();
            }
        }
    };

    public void init(Bundle savedInstanceState) {
        adapter = new ImageAdapter(this, ListInfo.getfilmInfo(), options, ImageLoader.getInstance());

        fancyCoverFlow = (FancyCoverFlow) findViewById(R.id.fancyCoverFlow);
        // fancyCoverFlow = new FancyCoverFlow(this);
        // LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //
        // this.addView(gallery, parms);
        // this.setBackgroundColor(Color.BLACK);


        // item之间的间隙可以近似认为是imageview的宽度与缩放比例的乘积的一半
        fancyCoverFlow.setSpacing(-70);
        fancyCoverFlow.setAdapter(adapter);

        fancyCoverFlow.setOnMoveListener(new EcoGalleryAdapterView.OnMoveListener() {
            @Override
            public void onMoveFinish(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                fancyCoverFlow.moveToNext();
                Log.d("", "moveFinish");
                // fancyCoverFlow.startFling();
            }
        });

        fancyCoverFlow.setOnItemSelectedListener(new EcoGalleryAdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                int len = adapter.getData().size();

                // 使得所有的图像在[0.5, 1.5)*len之间，无限循环切换
                // int first = len / 2, last = first + len;
                // while (position < first)
                //     position += len;
                // while (position >= last)
                //     position -= len;

                // fancyCoverFlow.setSelection(position);
                // adapter.notifyDataSetChanged();
                cur_index = position;
                Log.d("Gallery", "cur index:"+cur_index);
            }

            @Override
            public void onNothingSelected(EcoGalleryAdapterView<?> parent) {

            }

            // @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                cur_index = position;
                Log.d("Gallery", "cur index:"+cur_index);

                int len = adapter.getData().size();

                // 使得所有的图像在[0.5, 1.5)*len之间，无限循环切换
                int first = len / 2, last = first + len;
                while (position < first)
                    position += len;
                while (position >= last)
                    position -= len;

                fancyCoverFlow.setSelection(position);
                adapter.notifyDataSetChanged();

                //记录选择的图像索引
                // if(cur_index >= len) cur_index -= len;
            }

            // @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 点击事件
        fancyCoverFlow.setOnItemClickListener(new EcoGalleryAdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(EcoGalleryAdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(TestActivity.this,
                        ListInfo.getNameInfo().get(position % ListInfo.getfilmInfo().size()),
                        Toast.LENGTH_SHORT).show();

            }

            // @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(TestActivity.this,
                        ListInfo.getNameInfo().get(position % ListInfo.getfilmInfo().size()),
                        Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.startBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fancyCoverFlow.moveToNext();
            }
        });

        findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fancyCoverFlow.stop();
            }
        });

        findViewById(R.id.startFlipBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fancyCoverFlow.startFling();
            }
        });


        // 开启自动轮播
        count_drawble = adapter.getCount();
        // startPlay();
        // fancyCoverFlow.startFling();


    }

    /**
     * 开始轮播图切换
     */
    private void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new AutoPlayTask(), 1, 64,
                TimeUnit.MICROSECONDS);
    }

    /**
     * 停止轮播图切换
     */
    private void stopPlay() {
        scheduledExecutorService.shutdown();
    }

    /**
     * 执行轮播图切换任务
     *
     */
    private class AutoPlayTask implements Runnable {

        @Override
        public void run() {

            cur_index = cur_index % count_drawble; // 图片区间[0,count_drawable)
            Message msg = handler.obtainMessage(MSG_UPDATE);
            handler.sendMessage(msg);
            cur_index++;
        }
    }
}
