package com.jokin;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.jokin.widget.gallery.CGalleryAdapterView;
import com.jokin.widget.gallery.FancyGallery;
import com.jokin.widget.gallery.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity {
    DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImageLoader(getApplicationContext());

        setContentView(R.layout.activity_main);
        init(savedInstanceState);
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
                .showStubImage(R.drawable.img0)
                .showImageForEmptyUri(R.drawable.img0)
                .showImageOnFail(R.drawable.img0).cacheInMemory(true)
                .cacheOnDisc(true).bitmapConfig(Bitmap.Config.RGB_565).build();
    }

    private FancyGallery mFancyGallery;
    private ImageAdapter mAdapter;

    public void init(Bundle savedInstanceState) {
        mAdapter = new ImageAdapter(this, ListInfo.getfilmInfo(), options, ImageLoader.getInstance());
        mFancyGallery = (FancyGallery) findViewById(R.id.fancyCoverFlow);
        // mFancyGallery = new FancyCoverFlow(this);
        // LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        //
        // this.addView(gallery, parms);
        // this.setBackgroundColor(Color.BLACK);


        // item之间的间隙可以近似认为是imageview的宽度与缩放比例的乘积的一半
        mFancyGallery.setSpacing(-80);
        mFancyGallery.setAdapter(mAdapter);

        mFancyGallery.setOnMoveListener(new CGalleryAdapterView.OnMoveListener() {
            @Override
            public void onMoveFinish(CGalleryAdapterView<?> parent, View view, int position, long id) {
                // Message msg = handler.obtainMessage(MSG_UPDATE);
                // handler.sendMessage(msg);
                // mFancyGallery.moveToNext(2000, 10);

                Log.d("", "moveFinish");
                // mFancyGallery.moveToNext(time);
                //         if (time > 50) {
                //             time /= 1.2;
                //         }
                // mFancyGallery.startFling();
            }
        });

        mFancyGallery.setOnItemSelectedListener(new CGalleryAdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(CGalleryAdapterView<?> parent, View view, int position, long id) {
                int len = mAdapter.getData().size();
                Log.d("Gallery", "1 cur index:"+position);

                // 使得所有的图像在[0.5, 1.5)*len之间，无限循环切换
                boolean refresh = false;
                // int first = len / 2, last = first + len;
                // while (position < first) {
                //     position += len;
                //     refresh = true;
                // }
                // while (position >= last) {
                //     position -= len;
                //     refresh = true;
                // }
                // if (refresh) {
                //     mFancyGallery.setSelection(position);
                // }

                // mFancyGallery.setSelection(position);
                // mAdapter.notifyDataSetChanged();
                cur_index = position;
                Log.d("Gallery", "2 cur index:"+cur_index);
            }

            @Override
            public void onNothingSelected(CGalleryAdapterView<?> parent) {

            }

            // @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                cur_index = position;
                Log.d("Gallery", "cur index:"+cur_index);

                int len = mAdapter.getData().size();

                // 使得所有的图像在[0.5, 1.5)*len之间，无限循环切换
                int first = len / 2, last = first + len;
                while (position < first)
                    position += len;
                while (position >= last)
                    position -= len;

                mFancyGallery.setSelection(position);
                mAdapter.notifyDataSetChanged();

                //记录选择的图像索引
                // if(cur_index >= len) cur_index -= len;
            }

            // @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 点击事件
        mFancyGallery.setOnItemClickListener(new CGalleryAdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(CGalleryAdapterView<?> parent, View view, int position, long id) {

            }

            // @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
            }
        });

        findViewById(R.id.startBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mFancyGallery.moveToNext(start);
                // startPlay();
                // stop = false;
                // Message msg = handler.obtainMessage(MSG_UPDATE);
                // handler.sendMessage(msg);
                Log.d("CGallery", "width:"+ mFancyGallery.getWidth());
                mFancyGallery.startFling(-100);
            }
        });

        findViewById(R.id.stopBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stopPlay();
                stop = true;
                reset();
                mFancyGallery.stop();
            }
        });

        findViewById(R.id.startFlipBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFancyGallery.startFling(-100);
            }
        });


        // 开启自动轮播
        count_drawble = mAdapter.getCount();
        // startPlay();
        // mFancyGallery.startFling();
        mFancyGallery.setSelection(Integer.MAX_VALUE/2);

        mFancyGallery.setBackgroundColor(Color.GRAY);

        // TranslateAnimation translateAnimation = new TranslateAnimation(
        //         Animation.RELATIVE_TO_SELF, 0f,
        //         Animation.RELATIVE_TO_SELF, 1f,
        //         Animation.RELATIVE_TO_SELF, 0f,
        //         Animation.RELATIVE_TO_SELF, 0f);
        // translateAnimation.setDuration(15000);
        // mFancyGallery.startAnimation(translateAnimation);
    }

    /**
     * 开始轮播图切换
     */
    private void startPlay() {
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new AutoPlayTask(), 1, 400,
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


    private int cur_index = 0;
    private int count_drawble;
    private static int MSG_UPDATE = 1;

    // 定时任务
    private ScheduledExecutorService scheduledExecutorService;
    private int start = 10;
    private int time = 800;
    private int sleep = 80;
    private boolean stop = false;


    private void reset() {
        start = 10;
        time = 1000;
        sleep = 80;
    }

    // 通过handler来更新主界面
    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE) {
                // mFancyGallery.setSelection(cur_index, true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // mFancyGallery.moveToNext(time);

                        //     while (!stop) {
                        //         int rand = new Random().nextInt(14);
                        //         Log.d("CGallery", String.format("rand:%d", rand));
                        //     mFancyGallery.moveToNext(start);
                        if (start <= 1000) {
                            start += 10;
                        }
                        //         if (time >= 10) {
                        //             time /= 2;
                        //         }
                        //         if (sleep >= 10) {
                        //             sleep -= 1;
                        //         }
                        //         try {
                        //             Thread.sleep(sleep);
                        //         } catch (InterruptedException e) {
                        //             break;
                        //         }
                        //     }
                    }
                }).start();

            }
        }
    };
}
